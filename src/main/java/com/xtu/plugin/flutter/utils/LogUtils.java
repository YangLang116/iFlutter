package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.diagnostic.Logger;

public class LogUtils {

    private static Logger getLogger() {
        return Logger.getInstance("iFlutter -> ");
    }

    public static void info(String message) {
        System.out.println(message);
        getLogger().info(message);
    }

    public static void error(String message) {
        System.out.println(message);
        getLogger().error(message);
    }

}
