package org.openrewrite.java.cleanup;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class MethodNotAccessingInstanceDataShouldBeStaticTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new MethodNotAccessingInstanceDataShouldBeStatic());
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        private String getSomething() {
                                            return staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        private static String getSomething() {
                                            return staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        public final String getSomething() {
                                            return staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        public static String getSomething() {
                                            return staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        private String getSomething() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static String staticVariable = "something";
                                        private String instanceVariable = "anything";

                                        public final String getSomething() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private String getSomething() {
                                            return staticMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static String getSomething() {
                                            return staticMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        public final String getSomething() {
                                            return staticMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        public static String getSomething() {
                                            return staticMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private String getSomething() {
                                            return instanceMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        public final String getSomething() {
                                            return instanceMethod();
                                        }

                                        private static String staticMethod() {
                                            return "something";
                                        }

                                        public void instanceMethod() {
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (staticVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            if (staticVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (staticVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            if (staticVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (instanceVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (instanceVariable == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (staticMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            if (staticMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (staticMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            if (staticMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (instanceMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (instanceMethod() == 3) {
                                                return 44;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (staticVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (staticVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (staticVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (staticVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (instanceVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (false) {
                                                return 44;
                                            } else if (instanceVariable == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (staticMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (staticMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (staticMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (staticMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (instanceMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInElseIfCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else if (instanceMethod() == 3) {
                                                return 55;
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return instanceVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return instanceVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return instanceMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInElseBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            if (false) {
                                                return 44;
                                            } else {
                                                return instanceMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            while (staticVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            while (staticVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            while (staticVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            while (staticVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            while (instanceVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            while (instanceVariable == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            while (staticMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            while (staticMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            while (staticMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            while (staticMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            while (instanceMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            while (instanceMethod() == 3) {
                                                String l = "";
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            while (true) {
                                                int l = staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            while (true) {
                                                int l = staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            while (true) {
                                                int l = staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            while (true) {
                                                int l = staticVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            while (true) {
                                                int l = instanceVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            while (true) {
                                                int l = instanceVariable;
                                            }
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            while (true) {
                                                int l = staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            while (true) {
                                                int l = staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            while (true) {
                                                int l = staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            while (true) {
                                                int l = staticMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            while (true) {
                                                int l = instanceMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            while (true) {
                                                int l = instanceMethod();
                                            }
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            do  {
                                                String l = "";
                                            } while (staticVariable == 3);
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            do  {
                                                String l = "";
                                            } while (staticVariable == 3);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            do  {
                                                String l = "";
                                            } while (staticVariable == 3);
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            do  {
                                                String l = "";
                                            } while (staticVariable == 3);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            do  {
                                                String l = "";
                                            } while (instanceVariable == 3);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            do  {
                                                String l = "";
                                            } while (instanceVariable == 3);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (staticMethod() == 3);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (staticMethod() == 3);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (staticMethod() == 3);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (staticMethod() == 3);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (instanceMethod() == 3);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInDoWhileCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            do  {
                                                String l = "";
                                            } while (instanceMethod() == 3);
                                            return 22;
                                        }

                                         private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            do {
                                                int l = staticVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            do {
                                                int l = staticVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            do {
                                                int l = staticVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            do {
                                                int l = staticVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            do {
                                                int l = instanceVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            do {
                                                int l = instanceVariable;
                                            } while (true);
                                            return 22;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            do {
                                                int l = staticMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            do {
                                                int l = staticMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            do {
                                                int l = staticMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            do {
                                                int l = staticMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            do {
                                                int l = instanceMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInDoWhileBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            do {
                                                int l = instanceMethod();
                                            } while (true);
                                            return 22;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return staticVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            return staticVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return staticVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            return staticVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return instanceVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return instanceVariable == 3 ? 2 : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return staticMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            return staticMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return staticMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            return staticMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return instanceMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInTernaryOperatorCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return instanceMethod() == 3 ? 2 : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return 4 == 3 ? 2 : staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            return 4 == 3 ? 2 : staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return 4 == 3 ? 2 : staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            return 4 == 3 ? 2 : staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return 4 == 3 ? 2 : instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return 4 == 3 ? 2 : instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return 4 == 3 ? 2 : staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            return 4 == 3 ? 2 : staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return 4 == 3 ? 2 : staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            return 4 == 3 ? 2 : staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return 4 == 3 ? 2 : instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInTernaryOperatorFalsePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return 4 == 3 ? 2 : instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return 3 == 3 ? staticVariable : 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static int getValue() {
                                            return 3 == 3 ? staticVariable : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return 3 == 3 ? staticVariable : 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static int getValue() {
                                            return 3 == 3 ? staticVariable : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private int getValue() {
                                            return 3 == 3 ? instanceVariable : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final int getValue() {
                                            return 3 == 3 ? instanceVariable : 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return 3 == 3 ? staticMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static int getSomething() {
                                            return 3 == 3 ? staticMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return 3 == 3 ? staticMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static int getSomething() {
                                            return 3 == 3 ? staticMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private int getSomething() {
                                            return 3 == 3 ? instanceMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInTernaryOperatorTruePart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final int getSomething() {
                                            return 3 == 3 ? instanceMethod() : 1;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInVariableAssignment() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            staticVariable = 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                            staticVariable = 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInVariableAssignment() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            staticVariable = 1;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                            staticVariable = 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInVariableAssignment() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            instanceVariable = 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInVariableAssignment() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            instanceVariable = 1;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                           int i = 1;
                                           i = staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                           int i = 1;
                                           i = staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                           int i = 1;
                                           i = staticVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                           int i = 1;
                                           i = staticVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                           int i = 1;
                                           i = instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                           int i = 1;
                                           i = instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                           int i = 1;
                                           i = staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                           int i = 1;
                                           i = staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                           int i = 1;
                                           i = staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                           int i = 1;
                                           i = staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                           int i = 1;
                                           i = instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInVariableAssignmentExpressionPart() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            int i = 1;
                                            i = instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            switch (staticVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                            switch (staticVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            switch (staticVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                            switch (staticVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            switch (instanceVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            switch (instanceVariable) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            switch (staticMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                            switch (staticMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            switch (staticMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                            switch (staticMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            switch (instanceMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInSwitchCondition() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            switch (instanceMethod()) {
                                                case 1:
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    instanceVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    instanceVariable++;
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    staticMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    instanceMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInSwitchBody() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue(int arg) {
                                            switch (arg) {
                                                case 1:
                                                    instanceMethod();
                                                    break;
                                                default:
                                                    throw new RuntimeException("");
                                            }
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceVariableInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            Integer i = new Integer(staticVariable);
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                            Integer i = new Integer(staticVariable);
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceVariableInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            Integer i = new Integer(staticVariable);
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                            Integer i = new Integer(staticVariable);
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            Integer i = new Integer(instanceVariable);
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            Integer i = new Integer(instanceVariable);
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToPrivateMethodNotUsingInstanceMethodInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            Integer i = new Integer(staticMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private static void setValue() {
                                            Integer i = new Integer(staticMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void addsStaticToFinalMethodNotUsingInstanceMethodInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            Integer i = new Integer(staticMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """,
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public static void setValue() {
                                            Integer i = new Integer(staticMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            Integer i = new Integer(instanceMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodInNewClassDeclaration() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            Integer i = new Integer(instanceMethod());
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceAndNotInstanceMethods() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            staticMethod();
                                            instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceAndNotInstanceMethods() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            staticMethod();
                                            instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceAndNotInstanceVariables() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            staticVariable++;
                                            instanceVariable++;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceAndNotInstanceVariables() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            staticVariable++;
                                            instanceVariable++;
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceMethodAndNotInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            staticVariable++;
                                            instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceMethodAndNotInstanceVariable() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            staticVariable++;
                                            instanceMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToPrivateMethodUsingInstanceVariableAndNotInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private static int staticVariable = 1;
                                        private int instanceVariable = 2;

                                        private void setValue() {
                                            instanceVariable++;
                                            staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }

    @Test
    void notAddingStaticToFinalMethodUsingInstanceVariableAndNotInstanceMethod() {
        rewriteRun(
                java(
                        """
                                    class A {
                                        private int instanceVariable = 2;

                                        public final void setValue() {
                                            instanceVariable++;
                                            staticMethod();
                                        }

                                        private static int staticMethod() {
                                            return 3;
                                        }

                                        public int instanceMethod() {
                                            return instanceVariable;
                                        }
                                    }
                                """
                )
        );
    }
}
