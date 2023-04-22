package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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

    public static void openFile(@NotNull Project project, @NotNull File file) {
        VirtualFile needOpenFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (needOpenFile == null) return;
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        editorManager.openFile(needOpenFile, true);
    }
}
