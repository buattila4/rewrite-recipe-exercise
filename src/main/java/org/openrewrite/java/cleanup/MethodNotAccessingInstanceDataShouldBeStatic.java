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

                    if (!processBody(md.getBody().getStatements(), localVariables, inputVariables, variablesToCheck,
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
            } else if (s instanceof J.ForLoop) {
                final J.ForLoop fl = (J.ForLoop) s;
                final Set<String> loopVariables = new HashSet<>();
                final J.ForLoop.Control control = fl.getControl();

                // TODO finish for loop

                if (!processBody(((J.Block) fl.getBody()).getStatements(), localVariables, inputVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.WhileLoop) {
                final J.WhileLoop whl = (J.WhileLoop) s;
                if (!processExpression(whl.getCondition().getTree(), inputVariables, localVariables,
                        variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
                if (!processBody(((J.Block) whl.getBody()).getStatements(), localVariables, inputVariables,
                        variablesToCheck,
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

            /*
             * TODO cover the following cases
             *  - for each loop
             *  - try catch block
             *  - do while block
             *  - ternary operator
             *  - switch block
             * */
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
            if (!inputVariables.contains(i.getSimpleName()) && !localVariables.contains(i.getSimpleName())) {
                variablesToCheck.add(i.getSimpleName());
            }
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
        }
        return true;
    }

    private boolean processMethodInvocation(final J.MethodInvocation mi, final Set<String> inputVariables,
                                            final Set<String> localVariables, final Set<String> variablesToCheck,
                                            final Set<String> methodsToCheck) {
        if (mi.getSelect() instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) mi.getSelect();
            if (!processIdentifier(i, inputVariables, localVariables, variablesToCheck)) {
                return false;
            }
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
                if (!processIdentifier(i, inputVariables, localVariables, variablesToCheck)) {
                    return false;
                }
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
            }
        }

        return true;
    }

    private boolean processBinary(final J.Binary b, final Set<String> inputVariables,
                                  final Set<String> localVariables, final Set<String> variablesToCheck,
                                  final Set<String> methodsToCheck) {
        if (b.getLeft() instanceof J.Identifier) {
            J.Identifier i = (J.Identifier) b.getLeft();
            if (!processIdentifier(i, inputVariables, localVariables, variablesToCheck)) {
                return false;
            }
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
            if (!processIdentifier(i, inputVariables, localVariables, variablesToCheck)) {
                return false;
            }
        } else if (b.getRight() instanceof J.MethodInvocation) {
            J.MethodInvocation mi = (J.MethodInvocation) b.getRight();
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        }

        return true;
    }

    private boolean processIdentifier(final J.Identifier i, final Set<String> inputVariables,
                                      final Set<String> localVariables, final Set<String> variablesToCheck) {
        if (inputVariables.contains(i.getSimpleName()) || localVariables.contains(i.getSimpleName())) {
            return false;
        } else {
            variablesToCheck.add(i.getSimpleName());
        }
        return true;
    }

    private boolean processIfStatement(final J.If ifS, final Set<String> inputVariables,
                                       final Set<String> localVariables, final Set<String> variablesToCheck,
                                       final Set<String> methodsToCheck) {
        if (!processExpression(ifS.getIfCondition().getTree(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }
        if (!processBody(((J.Block) ifS.getThenPart()).getStatements(), localVariables, inputVariables,
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
                return processBody(eS.getStatements(), localVariables, inputVariables,
                        variablesToCheck,
                        methodsToCheck);
            }
        }

        return true;
    }
}