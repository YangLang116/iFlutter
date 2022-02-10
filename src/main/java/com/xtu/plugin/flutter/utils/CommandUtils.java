package com.xtu.plugin.flutter.utils;

import java.io.*;

public class CommandUtils {

    public static String executeSync(String command, File workDir) {
        InputStream resultStream = null;
        InputStream errorStream = null;
        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        try {
            Process process = Runtime.getRuntime().exec(command, null, workDir);
            fillBuffer(resultBuffer, resultStream = process.getInputStream());
            fillBuffer(errorBuffer, errorStream = process.getErrorStream());
            process.waitFor();
            if (errorBuffer.length() > 0) return errorBuffer.toString();
            return resultBuffer.toString();
        } catch (Exception e) {
            LogUtils.error("CommandUtils executeSync: " + e.getMessage());
            return e.getMessage();
        } finally {
            CloseUtils.close(resultStream);
            CloseUtils.close(errorStream);
        }
    }

    private static void fillBuffer(StringBuffer resultBuffer, InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            resultBuffer.append(line).append("\n");
        }
    }
}
