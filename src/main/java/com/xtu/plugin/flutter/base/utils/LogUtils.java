package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.xtu.plugin.flutter.advice.AdviceManager;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    private static final Logger LOG = Logger.getInstance("iFlutter -> ");

    public static void info(@NotNull String message) {
        LOG.info(message);
    }

    public static void error(@NotNull String entryPoint, @NotNull Exception exception) {
        LOG.error(entryPoint + " : " + exception.getMessage());
        String content = String.format("message:\n%s\n\nerrorStack:\n%s", exception.getMessage(), getStackTrace(exception));
        AdviceManager.getInstance().submitAdvice(null, "error catch", content);
    }

    private static String getStackTrace(@NotNull Exception e) {
        StringWriter strWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(strWriter, true));
        return strWriter.toString();
    }

}
