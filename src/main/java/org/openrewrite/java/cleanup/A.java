package org.openrewrite.java.cleanup;

import org.openrewrite.internal.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class A extends J {
    public int a = 1;

    public static String k() {
        return "";
    }

    public String k(String j) {
        return klm();
    }
}

class J {
    public String c = "ssd";

    public String klm() {
        return "défjg ";
    }
}