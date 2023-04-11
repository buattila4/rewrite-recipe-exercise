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
    private static final String SUPER_KEYWORD = "super";

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
        return new MakePrivateOrFinalMethodsFinalVisitor();
    }

    private static class MakePrivateOrFinalMethodsFinalVisitor extends JavaIsoVisitor<ExecutionContext> {
        @Override
        public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration methodDecl, ExecutionContext ctx) {
            J.MethodDeclaration md = super.visitMethodDeclaration(methodDecl, ctx);

            if (!md.hasModifier(J.Modifier.Type.Static) && (md.hasModifier(
                    J.Modifier.Type.Private) || md.hasModifier(J.Modifier.Type.Final))) {
                final Set<String> localVariables = new HashSet<>();
                final Set<String> inputVariables = new HashSet<>();
                final Map<String, Set<JavaType.Variable>> variablesToCheck = new HashMap<>();
                final Map<String, Set<JavaType.Method>> methodsToCheck = new HashMap<>();

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
                    final Map<String, Set<JavaType.Variable>> staticVariables = new HashMap<>();
                    final Map<String, Set<JavaType.Method>> staticMethods = new HashMap<>();

                    final Cursor parent = getCursor().getParent().getParent().getParent();
                    collectInstanceDataFromOuterClass(parent, staticVariables, staticMethods);

                    for (Set<JavaType.Variable> variables : variablesToCheck.values()) {
                        for (JavaType.Variable variable : variables) {
                            if (!staticVariables.containsKey(variable.getName()) || !staticVariables.get(
                                            variable.getName())
                                    .contains(variable)) {
                                return md;
                            }
                        }
                    }

                    for (Set<JavaType.Method> methods : methodsToCheck.values()) {
                        for (JavaType.Method method : methods) {
                            if (!staticMethods.containsKey(method.getName()) || !staticMethods.get(method.getName())
                                    .contains(method)) {
                                return md;
                            }
                        }
                    }
                }

                if (md.hasModifier(J.Modifier.Type.Final)) {
                    md = md.withModifiers(
                            ListUtils.map(md.getModifiers(), mod -> mod.getType() == J.Modifier.Type.Final ?
                                    mod.withType(J.Modifier.Type.Static) : mod));
                    // TODO Need to trigger another Recipe to switch from instance to class (instance.doSomething() -> Class.doSomething())
                } else {
                    List<J.Modifier> mod = Arrays.asList(
                            new J.Modifier(Tree.randomId(), Space.build(" ", emptyList()), Markers.EMPTY,
                                    J.Modifier.Type.Static, emptyList()));
                    md = md.withModifiers(ListUtils.insertAll(md.getModifiers(), 1, mod));
                }
            }

            return md;
        }
    }

    private static boolean processBody(final List<Statement> statements, final Set<String> inputVariables,
                                       final Set<String> localVariables,
                                       final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                       final Map<String, Set<JavaType.Method>> methodsToCheck) {
        for (Statement s : statements) {
            if (s instanceof J.VariableDeclarations) {
                final J.VariableDeclarations vd = (J.VariableDeclarations) s;
                if (!processVariableDeclaration(vd, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.MethodInvocation) {
                final J.MethodInvocation mi = (J.MethodInvocation) s;
                if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Assignment) {
                final J.Assignment a = (J.Assignment) s;
                if (!processAssignment(a, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
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
            } else if (s instanceof J.MethodDeclaration) {
                final J.MethodDeclaration md = (J.MethodDeclaration) s;
                if (!processBody(md.getBody().getStatements(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean processAssignment(final J.Assignment a, final Set<String> inputVariables,
                                             final Set<String> localVariables,
                                             final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                             final Map<String, Set<JavaType.Method>> methodsToCheck) {
        final J.Identifier v = (J.Identifier) a.getVariable();

        processIdentifier(v, inputVariables, localVariables, variablesToCheck);

        if (!processExpression(a.getAssignment(), inputVariables, localVariables, variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        return true;
    }

    private static boolean processVariableDeclaration(final J.VariableDeclarations vd, final Set<String> inputVariables,
                                                      final Set<String> localVariables,
                                                      final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                                      final Map<String, Set<JavaType.Method>> methodsToCheck) {
        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
            localVariables.add(v.getSimpleName());

            if (!processExpression(v.getInitializer(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        }

        return true;
    }

    private static boolean processExpression(final Expression exp, final Set<String> inputVariables,
                                             final Set<String> localVariables,
                                             final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                             final Map<String, Set<JavaType.Method>> methodsToCheck) {
        if (exp instanceof J.MethodInvocation) {
            final J.MethodInvocation mi = (J.MethodInvocation) exp;
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.Identifier) {
            final J.Identifier i = (J.Identifier) exp;
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (exp instanceof J.Binary) {
            final J.Binary b = (J.Binary) exp;
            if (!processBinary(b, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.NewClass) {
            final J.NewClass nc = (J.NewClass) exp;
            if (!processArguments(nc.getArguments(), inputVariables, localVariables, variablesToCheck,
                    methodsToCheck)) {
                return false;
            }
        } else if (exp instanceof J.Ternary) {
            final J.Ternary t = (J.Ternary) exp;

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
        } else if (exp instanceof J.FieldAccess) {
            final J.FieldAccess fa = (J.FieldAccess) exp;
            if (fa.getTarget() instanceof J.Identifier) {
                final J.Identifier targetID = (J.Identifier) fa.getTarget();

                if (targetID.getSimpleName().equals(SUPER_KEYWORD)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean processMethodInvocation(final J.MethodInvocation mi, final Set<String> inputVariables,
                                                   final Set<String> localVariables,
                                                   final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                                   final Map<String, Set<JavaType.Method>> methodsToCheck) {

        if (mi.getSelect() == null) {
            final String methodName = mi.getSimpleName();
            final Set<JavaType.Method> methods = methodsToCheck.getOrDefault(methodName, new HashSet<>());
            methods.add(mi.getMethodType());
            methodsToCheck.put(methodName, methods);
        } else if (mi.getSelect() instanceof J.Identifier) {
            final J.Identifier i = (J.Identifier) mi.getSelect();
            if (i.getSimpleName().equals(SUPER_KEYWORD)) {
                return false;
            }
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
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

    private static boolean processArguments(final List<Expression> args, final Set<String> inputVariables,
                                            final Set<String> localVariables,
                                            final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                            final Map<String, Set<JavaType.Method>> methodsToCheck) {
        for (Expression exp : args) {
            if (exp instanceof J.Identifier) {
                final J.Identifier i = (J.Identifier) exp;
                processIdentifier(i, inputVariables, localVariables, variablesToCheck);
            } else if (exp instanceof J.MethodInvocation) {
                final J.MethodInvocation vmi = (J.MethodInvocation) exp;
                if (!processMethodInvocation(vmi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (exp instanceof J.Binary) {
                final J.Binary b = (J.Binary) exp;
                if (!processBinary(b, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (exp instanceof J.Unary) {
                final J.Unary u = (J.Unary) exp;
                if (!processExpression(u.getExpression(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            } else if (exp instanceof J.Lambda) {
                final J.Lambda l = (J.Lambda) exp;

                for (J p : l.getParameters().getParameters()) {
                    if (p instanceof J.VariableDeclarations) {
                        final J.VariableDeclarations vd = (J.VariableDeclarations) p;
                        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                            localVariables.add(v.getSimpleName());
                        }
                    }
                }

                if (l.getBody() instanceof J.Binary) {
                    final J.Binary bi = (J.Binary) l.getBody();
                    if (!processBinary(bi, inputVariables, localVariables, variablesToCheck,
                            methodsToCheck)) {
                        return false;
                    }
                } else if (l.getBody() instanceof J.Block) {
                    final J.Block block = (J.Block) l.getBody();
                    if (!processBody(block.getStatements(), inputVariables, localVariables, variablesToCheck,
                            methodsToCheck)) {
                        return false;
                    }
                }
            } else if (exp instanceof J.NewClass) {
                final J.NewClass nc = (J.NewClass) exp;
                if (!processBody(nc.getBody().getStatements(), inputVariables, localVariables, variablesToCheck,
                        methodsToCheck)) {
                    return false;
                }
            }

        }

        return true;
    }

    private static boolean processBinary(final J.Binary b, final Set<String> inputVariables,
                                         final Set<String> localVariables,
                                         final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                         final Map<String, Set<JavaType.Method>> methodsToCheck) {
        if (b.getLeft() instanceof J.Identifier) {
            final J.Identifier i = (J.Identifier) b.getLeft();
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (b.getLeft() instanceof J.MethodInvocation) {
            final J.MethodInvocation mi = (J.MethodInvocation) b.getLeft();
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (b.getLeft() instanceof J.Binary) {
            final J.Binary lb = (J.Binary) b.getLeft();
            if (!processBinary(lb, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        } else if (b.getLeft() instanceof J.Parentheses) {
            final J.Parentheses p = (J.Parentheses) b.getLeft();

            if (p.getTree() instanceof J.Binary) {
                final J.Binary lb = (J.Binary) p.getTree();
                if (!processBinary(lb, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            }
        }

        if (b.getRight() instanceof J.Identifier) {
            final J.Identifier i = (J.Identifier) b.getRight();
            processIdentifier(i, inputVariables, localVariables, variablesToCheck);
        } else if (b.getRight() instanceof J.MethodInvocation) {
            final J.MethodInvocation mi = (J.MethodInvocation) b.getRight();
            if (!processMethodInvocation(mi, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                return false;
            }
        }

        return true;
    }

    private static void processIdentifier(final J.Identifier i, final Set<String> inputVariables,
                                          final Set<String> localVariables,
                                          final Map<String, Set<JavaType.Variable>> variablesToCheck) {
        if (!inputVariables.contains(i.getSimpleName()) && !localVariables.contains(i.getSimpleName())) {

            final String variableName = i.getSimpleName();
            final Set<JavaType.Variable> variables = variablesToCheck.getOrDefault(variableName,
                    new HashSet<>());
            variables.add(i.getFieldType());
            variablesToCheck.put(variableName, variables);
        }
    }

    private static boolean processIfStatement(final J.If ifS, final Set<String> inputVariables,
                                              final Set<String> localVariables,
                                              final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                              final Map<String, Set<JavaType.Method>> methodsToCheck) {
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

    private static void collectInstanceDataFromOuterClass(final Cursor parent,
                                                          final Map<String, Set<JavaType.Variable>> staticVariables,
                                                          final Map<String, Set<JavaType.Method>> staticMethods) {
        if (parent.getValue() instanceof J.ClassDeclaration) {
            final J.ClassDeclaration parentClass = parent.getValue();

            for (Statement s : parentClass.getBody().getStatements()) {
                if (s instanceof J.VariableDeclarations) {
                    final J.VariableDeclarations vd = (J.VariableDeclarations) s;

                    if (vd.hasModifier(J.Modifier.Type.Static)) {
                        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
                            final String variableName = v.getSimpleName();
                            final Set<JavaType.Variable> variables = staticVariables.getOrDefault(variableName,
                                    new HashSet<>());
                            variables.add(v.getVariableType());
                            staticVariables.put(variableName, variables);
                        }
                    }
                } else if (s instanceof J.MethodDeclaration) {
                    final J.MethodDeclaration md = (J.MethodDeclaration) s;
                    if (md.hasModifier(J.Modifier.Type.Static)) {
                        final String methodName = md.getSimpleName();
                        final Set<JavaType.Method> methods = staticMethods.getOrDefault(methodName, new HashSet<>());
                        methods.add(md.getMethodType());
                        staticMethods.put(methodName, methods);
                    }
                }
            }

            if (parent.getParent() != null && parent.getParent().getParent() != null && parent.getParent().getParent()
                    .getParent() != null) {
                collectInstanceDataFromOuterClass(parent.getParent().getParent().getParent(), staticVariables,
                        staticMethods);
            }

            if (parentClass.getExtends() != null) {
                JavaType.FullyQualified parentFq = TypeUtils.asFullyQualified(parentClass.getExtends().getType());
                if (parentFq == null) {
                    return;
                }

                for (JavaType.Method method : parentFq.getMethods()) {
                    if (method.hasFlags(Flag.Static)) {
                        final String methodName = method.getName();
                        final Set<JavaType.Method> methods = staticMethods.getOrDefault(methodName, new HashSet<>());
                        methods.add(method);
                        staticMethods.put(methodName, methods);
                    }
                }

                for (JavaType.Variable variable : parentFq.getMembers()) {
                    if (variable.hasFlags(Flag.Static)) {
                        final String variableName = variable.getName();
                        final Set<JavaType.Variable> variables = staticVariables.getOrDefault(variableName,
                                new HashSet<>());
                        variables.add(variable);
                        staticVariables.put(variableName, variables);
                    }
                }
            }
        }
    }

    private static boolean processTryCatchBlock(final J.Try tryB, final Set<String> inputVariables,
                                                final Set<String> localVariables,
                                                final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                                final Map<String, Set<JavaType.Method>> methodsToCheck) {
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

    private static boolean processForEachLoop(final J.ForEachLoop feLoop, final Set<String> inputVariables,
                                              final Set<String> localVariables,
                                              final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                              final Map<String, Set<JavaType.Method>> methodsToCheck) {
        final J.VariableDeclarations vd = feLoop.getControl().getVariable();
        for (J.VariableDeclarations.NamedVariable v : vd.getVariables()) {
            localVariables.add(v.getSimpleName());
        }

        processExpression(feLoop.getControl().getIterable(), inputVariables, localVariables, variablesToCheck,
                methodsToCheck);

        if (!processBody(((J.Block) feLoop.getBody()).getStatements(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        return true;
    }

    private static boolean processForLoop(final J.ForLoop fl, final Set<String> inputVariables,
                                          final Set<String> localVariables,
                                          final Map<String, Set<JavaType.Variable>> variablesToCheck,
                                          final Map<String, Set<JavaType.Method>> methodsToCheck) {
        final J.ForLoop.Control control = fl.getControl();

        for (Statement s : control.getInit()) {
            if (s instanceof J.VariableDeclarations) {
                final J.VariableDeclarations vd = (J.VariableDeclarations) s;
                if (!processVariableDeclaration(vd, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            } else if (s instanceof J.Assignment) {
                final J.Assignment a = (J.Assignment) s;
                if (!processAssignment(a, inputVariables, localVariables, variablesToCheck, methodsToCheck)) {
                    return false;
                }
            }
        }

        if (!processExpression(control.getCondition(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        if (!processBody(control.getUpdate(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        if (!processBody(((J.Block) fl.getBody()).getStatements(), inputVariables, localVariables,
                variablesToCheck,
                methodsToCheck)) {
            return false;
        }

        return true;
    }
}
