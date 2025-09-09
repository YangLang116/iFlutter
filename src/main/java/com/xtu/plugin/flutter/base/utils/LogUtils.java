package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public class LogUtils {

    private static final Logger LOG = Logger.getInstance("iFlutter -> ");

    public static void info(@NotNull String message) {
        LOG.info(message);
    }

    public static void error(@NotNull String entryPoint, @NotNull Exception exception) {
        LOG.error(entryPoint + " : " + exception.getMessage());
    }
}
