package com.xtu.plugin.flutter.utils;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class CommandUtils {

    public static CommandResult executeSync(String command, File workDir) {
        return executeSync(command, workDir, -1);
    }

    public static CommandResult executeSync(String command, File workDir, int maxTimeOut) {
        InputStream resultStream = null;
        InputStream errorStream = null;
        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        try {
            Process process = Runtime.getRuntime().exec(command, null, workDir);
            fillBuffer(resultBuffer, resultStream = process.getInputStream());
            fillBuffer(errorBuffer, errorStream = process.getErrorStream());
            if (maxTimeOut > 0) {
                process.waitFor(maxTimeOut, TimeUnit.MINUTES);
            } else {
                process.waitFor();
            }
            if (errorBuffer.length() > 0) return new CommandResult(CommandResult.FAIL, errorBuffer.toString());
            return new CommandResult(CommandResult.SUCCESS, resultBuffer.toString());
        } catch (Exception e) {
            LogUtils.error("CommandUtils executeSync: " + e.getMessage());
            return new CommandResult(CommandResult.FAIL, e.getMessage());
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

    public static class CommandResult {

        public static final int SUCCESS = 0;
        public static final int FAIL = -1;

        public final int code;
        public final String result;

        public CommandResult(int code, String result) {
            this.code = code;
            this.result = result;
        }

        @Override
        public String toString() {
            return "CommandResult{" +
                    "code=" + code +
                    ", result='" + result + '\'' +
                    '}';
        }
    }
}
