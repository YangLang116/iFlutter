package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CommandUtils {

    @NotNull
    public static CommandResult executeSync(@NotNull Project project, @NotNull String commandArgs, int maxTimeOut) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) {
            return new CommandResult(CommandResult.FAIL, "project path is null");
        }
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) {
            return new CommandResult(CommandResult.FAIL, "must set flutter.sdk in local.properties");
        }
        String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
        File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
        String command = String.format(Locale.ROOT, "%s %s", executorFile.getAbsolutePath(), commandArgs);
        return CommandUtils.executeSync(command, new File(projectPath), maxTimeOut);
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
            if (errorBuffer.length() > 0) {
                return new CommandResult(CommandResult.FAIL, errorBuffer.toString());
            }
            return new CommandResult(CommandResult.SUCCESS, resultBuffer.toString());
        } catch (Exception e) {
            return new CommandResult(CommandResult.FAIL, e.getMessage());
        } finally {
            CloseUtils.close(resultStream);
            CloseUtils.close(errorStream);
        }
    }

    private static void fillBuffer(StringBuffer resultBuffer, InputStream inputStream) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(streamReader);
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
