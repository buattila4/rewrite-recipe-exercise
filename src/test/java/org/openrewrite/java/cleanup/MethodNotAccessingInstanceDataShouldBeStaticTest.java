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
    void addsStaticToPrivateMethodNotUsingInstanceData() {
        rewriteRun(
                java(
                        """
                                    class Utilities {
                                        private static String magicWord = "magic";
                                        
                                        private String getMagicWord() {
                                            return magicWord;
                                        }
                                        
                                        private final String getMagicWord2() {
                                            return magicWord;
                                        }
                                        
                                        public final String getMagicWord3() {
                                            return magicWord;
                                        }
                                    }
                                """,
                        """
                                    class Utilities {
                                        private static String magicWord = "magic";
                                        
                                        private static String getMagicWord() {
                                            return magicWord;
                                        }
                                        
                                        private static String getMagicWord2() {
                                            return magicWord;
                                        }
                                        
                                        public static String getMagicWord3() {
                                            return magicWord;
                                        }
                                    }
                                """
                )
        );
    }
}
