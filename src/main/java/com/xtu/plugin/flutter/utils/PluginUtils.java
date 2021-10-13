package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class PluginUtils {

    public static List<String> getSupportAssetFoldName() {
        return Arrays.asList("assets", "images");
    }

    public static boolean isFlutterProject(Project project) {
        if (project == null) return false;
        File file = new File(project.getBasePath(), "pubspec.yaml");
        return file.exists();
    }
}
