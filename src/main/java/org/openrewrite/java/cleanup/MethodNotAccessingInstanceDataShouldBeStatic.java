package org.openrewrite.java.cleanup;

import org.openrewrite.*;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.*;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Markers;

import java.util.*;

import static java.util.Collections.emptyList;


public class MethodNotAccessingInstanceDataShouldBeStatic extends Recipe {
    @Override
    public String getDisplayName() {
        return "Methods not accessing instance data should be static";
    }

    @Override
    public String getDescription() {
        return "Private or final methods not accessing instance data should be static.";
    }

    @Override
    public Set<String> getTags() {
        return Collections.singleton("RSPEC-2325");
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>() {

            @Override
            public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDecl, ExecutionContext ctx) {
                J.MethodDeclaration md = super.visitMethodDeclaration(methodDecl, ctx);

                if (!md.hasModifier(J.Modifier.Type.Static) && (md.hasModifier(
                        J.Modifier.Type.Private) || md.hasModifier(J.Modifier.Type.Final))) {
                    final Set<String> localVariables = new HashSet<>();
                    final Set<String> inputVariables = new HashSet<>();
                    final Set<String> variablesToCheck = new HashSet<>();
                    final Set<String> methodsToCheck = new HashSet<>();

                    for (Statement s : md.getParameters()) {
                        if (s instanceof J.VariableDeclarations) {
                            final J.VariableDeclarations vd = (J.VariableDeclarations) s;
                            for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                                inputVariables.add(v.getSimpleName());
                            }
                        }
                    }

                    if (!processBody(md.getBody().getStatements(), inputVariables, localVariables, variablesToCheck,
                            methodsToCheck)) {
                        return md;
                    }

                    if (!variablesToCheck.isEmpty() || !methodsToCheck.isEmpty()) {
                        final Set<String> instanceVariables = new HashSet<>();
                        final Set<String> instanceMethods = new HashSet<>();

                        final Cursor parent = getCursor().getParent().getParent().getParent();
                        collectInstanceDataFromOuterClass(parent, instanceVariables, instanceMethods);

                        for (String v : variablesToCheck) {
                            if (instanceVariables.contains(v)) {
                                return md;
                            }
                        }

                        for (String m : methodsToCheck) {
                            if (instanceMethods.contains(m)) {
                                return md;
                            }
                        }
                    }

                    if (md.hasModifier(J.Modifier.Type.Final)) {
                        md = md.withModifiers(
                                ListUtils.map(md.getModifiers(), mod -> mod.getType() == J.Modifier.Type.Final ?
                                        mod.withType(J.Modifier.Type.Static) : mod));
                    } else {
                        List<J.Modifier> mod = Arrays.asList(
                                new J.Modifier(Tree.randomId(), Space.build(" ", emptyList()), Markers.EMPTY,
                                        J.Modifier.Type.Static, emptyList()));
                        md = md.withModifiers(ListUtils.insertAll(md.getModifiers(), 1, mod));
                    }
                }

                return md;
            }
        };
    }

    private boolean processBody(final List<Statement> statements, final Set<String> inputVariables,
                                final Set<String> localVariables, final Set<String> variablesToCheck,
                                final Set<String> methodsToCheck) {
        for (Statement s : statements) {
            if (s instanceof J.VariableDeclarations) {
                final J.VariableDeclarations vd = (J.VariableDeclarations) s;
                for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                    localVariables.add(v.getSimpleName());

                    if (!processExpression(v.getInitializer(), inputVariables, localVariables, variablesToCheck,
                            methodsToCheck)) {
                        return false;
                    }
                }
            } else if (s instanceof J.MethodInvocation) {
                final J.MethodInvocation mi = (J.MethodInvocation) s;
                if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Assignment) {
                final J.Assignment a = (J.Assignment) s;
                final J.Identifier v = (J.Identifier) a.getVariable();

                if (!inputVariables.contains(v.getSimpleName()) && !localVariables.contains(v.getSimpleName())) {
                    variablesToCheck.add(v.getSimpleName());
                }

                if (!processExpression(a.getAssignment(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.If) {
                final J.If ifS = (J.If) s;
                if (!processIfStatement(ifS, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Try) {
                final J.Try tryB = (J.Try) s;
                if (!processTryCatchBlock(tryB, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.ForEachLoop) {
                final J.ForEachLoop feLoop = (J.ForEachLoop) s;
                if (!processForEachLoop(feLoop, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.ForLoop) {
                final J.ForLoop fl = (J.ForLoop) s;
                if (!processForLoop(fl, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.WhileLoop) {
                final J.WhileLoop whl = (J.WhileLoop) s;
                if (!processExpression(whl.getCondition().getTree(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
                if (!processBody(((J.Block) whl.getBody()).getStatements(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.DoWhileLoop) {
                final J.DoWhileLoop dwLoop = (J.DoWhileLoop) s;

                if (!processBody(((J.Block) dwLoop.getBody()).getStatements(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }

                if (!processExpression(dwLoop.getWhileCondition().getTree(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Switch) {
                final J.Switch sw = (J.Switch) s;

                if (!processExpression(sw.getSelector().getTree(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }

                if (!processBody(sw.getCases().getStatements(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Case) {
                final J.Case ca = (J.Case) s;

                if (ca.getStatements() != null) {
                    if (!processBody(ca.getStatements(), inputVariables, localVariables, variablesToCheck,
                            methodsToCheck)) {
                        return false;
                    }
                }
            } else if (s instanceof J.Throw) {
                final J.Throw th = (J.Throw) s;
                if (!processExpression(th.getException(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Unary) {
                final J.Unary u = (J.Unary) s;
                if (!processExpression(u.getExpression(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Return) {
                final J.Return r = (J.Return) s;
                if (!processExpression(r.getExpression(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean processExpression(final Expression exp, final Set<String> inputVariables,
                                      final Set<String> localVariables, final Set<String> variablesToCheck,
                                      final Set<String> methodsToCheck) {
        if (exp instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) exp;
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) exp;
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (exp instanceof J.Binary) {
            J.Binary b = (J.Binary) exp;
            if (!processBinary(b, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.NewClass) {
            J.NewClass nc = (J.NewClass) exp;
            if (!processArguments(nc.getArguments(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.Ternary) {
            J.Ternary t = (J.Ternary) exp;

            if (!processExpression(t.getCondition(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }

            if (!processExpression(t.getFalsePart(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }

            if (!processExpression(t.getTruePart(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        }

        return true;
    }

    private boolean processMethodInvocation(final J.MethodInvocation mi, final Set<String> inputVariables,
                                            final Set<String> localVariables, final Set<String> variablesToCheck,
                                            final Set<String> methodsToCheck) {
        if (mi.getSelect() instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) mi.getSelect();
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (mi.getSelect() == null) {
            methodsToCheck.add(mi.getSimpleName());
        } else if (mi.getSelect() instanceof J.MethodInvocation) {
            if (!processMethodInvocation((J.MethodInvocation) mi.getSelect(), inputVariables, localVariables,
                    variablesToCheck, methodsToCheck)) {
                return false;
            }
        }

        if (!processArguments(mi.getArguments(), inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
            return false;
        }

        return true;
    }

    private boolean processArguments(final List<Expression> a, final Set<String> inputVariables,
                                     final Set<String> localVariables, final Set<String> variablesToCheck,
                                     final Set<String> methodsToCheck) {
        for (Expression exp : a) {
            if (exp instanceof J.Identifier) {
                J.Identifier i = (J.Identifier) exp;
                processIdentifier(i, inputVariables, localVariables, variablesToCheck);
            } else if (exp instanceof J.MethodInvocation) {
                J.MethodInvocation vmi = (J.MethodInvocation) exp;
                if (!processMethodInvocation(vmi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (exp instanceof J.Binary) {
                J.Binary b = (J.Binary) exp;
                if (!processBinary(b, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (exp instanceof J.Unary) {
                final J.Unary u = (J.Unary) exp;
                if (!processExpression(u.getExpression(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean processBinary(final J.Binary b, final Set<String> inputVariables,
                                  final Set<String> localVariables, final Set<String> variablesToCheck,
                                  final Set<String> methodsToCheck) {
        if (b.getLeft() instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) b.getLeft();
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (b.getLeft() instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) b.getLeft();
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (b.getLeft() instanceof J.Binary) {
            J.Binary lb = (J.Binary) b.getLeft();
            if (!processBinary(lb, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        }

        if (b.getRight() instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) b.getRight();
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (b.getRight() instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) b.getRight();
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        }

        return true;
    }

    private void processIdentifier(final J.Identifier i, final Set<String> inputVariables,
                                   final Set<String> localVariables, final Set<String> variablesToCheck) {
        if (!inputVariables.contains(i.getSimpleName()) && !localVariables.contains(i.getSimpleName())) {
            variablesToCheck.add(i.getSimpleName());
        }
    }

    private boolean processIfStatement(final J.If ifS, final Set<String> inputVariables,
                                       final Set<String> localVariables, final Set<String> variablesToCheck,
                                       final Set<String> methodsToCheck) {
        if (!processExpression(ifS.getIfCondition().getTree(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }
        if (!processBody(((J.Block) ifS.getThenPart()).getStatements(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        if (ifS.getElsePart() != null) {
            if (ifS.getElsePart().getBody() instanceof J.If) {
                final J.If eIfS = (J.If) ifS.getElsePart().getBody();
                return processIfStatement(eIfS, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck);
            } else if (ifS.getElsePart().getBody() instanceof J.Block) {
                final J.Block eS = (J.Block) ifS.getElsePart().getBody();
                return processBody(eS.getStatements(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck);
            }
        }

        return true;
    }

    private void collectInstanceDataFromOuterClass(final Cursor parent,
                                                   final Set<String> instanceVariables,
                                                   final Set<String> instanceMethods) {
        if (parent.getValue() instanceof J.ClassDeclaration) {
            final J.ClassDeclaration parentClass = parent.getValue();

            for (Statement s : parentClass.getBody().getStatements()) {
                if (s instanceof J.VariableDeclarations) {
                    final J.VariableDeclarations vd = (J.VariableDeclarations) s;

                    if (!vd.hasModifier(J.Modifier.Type.Static)) {
                        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                            instanceVariables.add(v.getSimpleName());
                        }
                    }
                } else if (s instanceof J.MethodDeclaration) {
                    final J.MethodDeclaration md = (J.MethodDeclaration) s;
                    if (!md.hasModifier(J.Modifier.Type.Static)) {
                        instanceMethods.add(md.getSimpleName());
                    }
                }
            }

            if (parent.getParent() != null && parent.getParent().getParent() != null && parent.getParent().getParent()
                    .getParent() != null) {
                collectInstanceDataFromOuterClass(parent.getParent().getParent().getParent(), instanceVariables,
                        instanceMethods);
            }

            if (parentClass.getExtends() != null) {
                JavaType.FullyQualified parentFq = TypeUtils.asFullyQualified(parentClass.getExtends().getType());
                if (parentFq == null) {
                    return;
                }

                for (JavaType.Method method : parentFq.getMethods()) {
                    System.out.println(method);
                    // TODO check if method is not static and ad it to the instanceMethods
                }


                for (JavaType.Variable member : parentFq.getMembers()) {
                    System.out.println(member);
                    // TODO check if member is not static and add it to the instanceVariables
                }
            }
        }
    }

    private boolean processTryCatchBlock(final J.Try tryB, final Set<String> inputVariables,
                                         final Set<String> localVariables, final Set<String> variablesToCheck,
                                         final Set<String> methodsToCheck) {
        if (!processBody(tryB.getBody().getStatements(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        for (J.Try.Catch c : tryB.getCatches()) {
            final Set<String> parameters = new HashSet<>();

            final J.VariableDeclarations vd = c.getParameter().getTree();
            for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                parameters.add(v.getSimpleName());
            }
            final Set<String> combined = new HashSet<>();
            combined.addAll(localVariables);
            combined.addAll(parameters);

            if (!processBody(c.getBody().getStatements(), combined, localVariables,
                    variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        }

        if (tryB.getFinally() != null) {
            if (!processBody(tryB.getFinally().getStatements(), inputVariables, localVariables,
                    variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        }
        return true;
    }

    private boolean processForEachLoop(final J.ForEachLoop feLoop, final Set<String> inputVariables,
                                       final Set<String> localVariables, final Set<String> variablesToCheck,
                                       final Set<String> methodsToCheck) {
        final Set<String> loopVariables = new HashSet<>();

        final J.VariableDeclarations vd = feLoop.getControl().getVariable();
        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
            loopVariables.add(v.getSimpleName());
        }

        final Set<String> combined = new HashSet<>();
        combined.addAll(localVariables);
        combined.addAll(loopVariables);

        processIdentifier((J.Identifier) feLoop.getControl().getIterable(), combined, localVariables,
                variablesToCheck);

        if (!processBody(((J.Block) feLoop.getBody()).getStatements(), combined, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        return true;
    }

    private boolean processForLoop(final J.ForLoop fl, final Set<String> inputVariables,
                                   final Set<String> localVariables, final Set<String> variablesToCheck,
                                   final Set<String> methodsToCheck) {
        final Set<String> loopVariables = new HashSet<>();
        final J.ForLoop.Control control = fl.getControl();

        for (Statement flStatement : control.getInit()) {
            if (flStatement instanceof J.VariableDeclarations) {
                J.VariableDeclarations vd = (J.VariableDeclarations) flStatement;

                for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                    loopVariables.add(v.getSimpleName());
                }
            }
        }

        final Set<String> combined = new HashSet<>();
        combined.addAll(inputVariables);
        combined.addAll(loopVariables);

        if (!processExpression(control.getCondition(), combined, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        if (!processBody(control.getUpdate(), combined, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        if (!processBody(((J.Block) fl.getBody()).getStatements(), combined, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        return true;
    }
}
