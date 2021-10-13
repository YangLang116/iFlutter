package com.xtu.plugin.flutter.utils;

public class StringUtil {

    public static String upFirstChar(String str) {
        if (str == null || str.length() < 1) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String splashClassName(String className) {
        if (className == null) return null;
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0, j = className.length(); i < j; i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c) && i != 0) {
                resultSb.append("_");
            }
            resultSb.append(Character.toLowerCase(c));
        }
        return resultSb.toString();
    }
}

