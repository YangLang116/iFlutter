package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * 采用DartFmt命令格式化，避免iFlutter依赖Dart Plugin
 */
public class DartUtils {

    private static String flutterPath;

    public static String getFlutterPath(@NotNull Project project) {
        if (!StringUtils.isEmpty(flutterPath)) return flutterPath;
        String projectPath = project.getBasePath();
        File configFile = new File(projectPath, "android" + File.separator + "local.properties");
        if (!configFile.exists()) return null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            return flutterPath = properties.getProperty("flutter.sdk");
        } catch (Exception e) {
            LogUtils.error("DartUtils getFlutterPath: " + e.getMessage());
            return null;
        } finally {
            CloseUtils.close(inputStream);
        }
    }

    //格式化dart代码
    public static void format(Project project, String filePath) {
        String flutterPath = getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return;
        File dartFmtFile = new File(flutterPath, "bin/cache/dart-sdk/bin/dartfmt".replace("/", File.separator));
        String cmd = String.format(Locale.ROOT, "%s --overwrite %s", dartFmtFile.getAbsolutePath(), filePath);
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            int result = process.waitFor();
            LogUtils.info("DartUtils format: result " + result);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.error("DartUtils format: " + e.getMessage());
        }
    }
}
