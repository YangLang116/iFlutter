package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.diagnostic.Logger;

public class LogUtils {

    private static final Logger LOG = Logger.getInstance("iFlutter -> ");

    public static void info(String message) {
        LOG.info(message);
    }

    public static void error(String entryPoint, Exception exception) {
        LOG.error(entryPoint + " : " + exception.getMessage());
    }

}
