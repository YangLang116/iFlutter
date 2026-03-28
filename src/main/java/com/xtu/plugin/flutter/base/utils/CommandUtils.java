package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        // Build command as array to correctly handle paths with spaces
        List<String> command = new ArrayList<>();
        command.add(executorFile.getAbsolutePath());
        command.addAll(Arrays.asList(commandArgs.split("\\s+")));
        return CommandUtils.executeSyncInternal(command.toArray(new String[0]), new File(projectPath), maxTimeOut);
    }

    public static CommandResult executeSync(String command, File workDir, int maxTimeOut) {
        // Split on whitespace for simple commands without quoted paths
        String[] parts = command.split("\\s+");
        return executeSyncInternal(parts, workDir, maxTimeOut);
    }

    private static CommandResult executeSyncInternal(String[] command, File workDir, int maxTimeOut) {
        StringBuffer resultBuffer = new StringBuffer();
        StringBuffer errorBuffer = new StringBuffer();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(workDir);
            Process process = processBuilder.start();
            // Read stdout and stderr in parallel to prevent deadlock
            Thread stdoutThread = new Thread(() -> {
                try { fillBuffer(resultBuffer, process.getInputStream()); }
                catch (IOException ignored) {}
            });
            Thread stderrThread = new Thread(() -> {
                try { fillBuffer(errorBuffer, process.getErrorStream()); }
                catch (IOException ignored) {}
            });
            stdoutThread.start();
            stderrThread.start();
            if (maxTimeOut > 0) {
                process.waitFor(maxTimeOut, TimeUnit.MINUTES);
            } else {
                process.waitFor();
            }
            stdoutThread.join();
            stderrThread.join();
            if (!resultBuffer.isEmpty()) {
                return new CommandResult(CommandResult.SUCCESS, resultBuffer.toString());
            } else {
                return new CommandResult(CommandResult.FAIL, errorBuffer.toString());
            }
        } catch (Exception e) {
            return new CommandResult(CommandResult.FAIL, e.getMessage());
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
