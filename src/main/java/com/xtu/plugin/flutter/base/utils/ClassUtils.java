package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.NotNull;

public class ClassUtils {

    public static String splashName(@NotNull String name) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0, j = name.length(); i < j; i++) {
            char c = name.charAt(i);
            if (i != 0 && Character.isUpperCase(c) && i + 1 < j && Character.isLowerCase(name.charAt(i + 1))) {
                resultSb.append("_");
            }
            resultSb.append(Character.toLowerCase(c));
        }
        return resultSb.toString();
    }

    public static String getClassName(@NotNull String str) {
        String name = toCamelCase(str);
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String toCamelCase(@NotNull String name) {
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

    @NotNull
    public static String getFieldName(@NotNull String name) {
        return name.toUpperCase().replace("-", "_").replace(" ", "_");
    }
}

