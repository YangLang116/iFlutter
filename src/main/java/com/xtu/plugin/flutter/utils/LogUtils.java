package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.xtu.plugin.flutter.advice.AdviceManager;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtils {

    private static final Logger LOG = Logger.getInstance("iFlutter -> ");

    public static void info(String message) {
        LOG.info(message);
    }

    public static void error(String entryPoint, Exception exception) {
        LOG.error(entryPoint + " : " + exception.getMessage());
        String content = new StringBuilder()
                .append("message: ").append(exception.getMessage()).append("\n")
                .append("stackTrace: ").append(getStackTrace(exception))
                .toString();
        AdviceManager.getInstance().submitAdvice(null, "error catch", content);
    }

    private static String getStackTrace(Exception e) {
        StringWriter strWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(strWriter, true));
        return strWriter.toString();
    }

}
