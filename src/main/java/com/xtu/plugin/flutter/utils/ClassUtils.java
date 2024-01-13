package com.xtu.plugin.flutter.utils;

public class ClassUtils {

    public static String splashName(String name) {
        if (name == null) return null;
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0, j = name.length(); i < j; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i != 0) {
                resultSb.append("_");
            }
            resultSb.append(Character.toLowerCase(c));
        }
        return resultSb.toString();
    }

    public static String getClassName(String str) {
        String name = toCamelCase(str);
        if (name == null) return null;
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String toCamelCase(String name) {
        if (name == null) return null;
        int len = name.length();
        StringBuilder sb = new StringBuilder();
        boolean needUpCase = false;
        for (int i = 0; i < len; i++) {
            char c = name.charAt(i);
            if (c == '_' || c == '-') {
                needUpCase = true;
                continue;
            }
            if (needUpCase) {
                sb.append(Character.toUpperCase(c));
                needUpCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

