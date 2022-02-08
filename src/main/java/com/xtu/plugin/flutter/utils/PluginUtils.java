package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.service.StorageService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class PluginUtils {

    @Nullable
    public static String getProjectPath(Project project) {
        if (project == null) return null;
        String projectPath = project.getBasePath();
        if (StringUtils.isEmpty(projectPath)) return null;
        if (projectPath.endsWith("/")) {
            projectPath = projectPath.substring(0, projectPath.length() - 1);
        }
        return projectPath;
    }

    public static boolean isNotFlutterProject(Project project) {
        String projectPath = getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return true;
        File file = new File(projectPath, PubspecUtils.getFileName());
        return !file.exists();
    }

    public static List<String> supportAssetFoldName(@NotNull Project project) {
        return StorageService.getInstance(project).getState().resDir;
    }
}
