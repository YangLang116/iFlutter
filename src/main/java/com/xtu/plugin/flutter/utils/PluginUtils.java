package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PluginUtils {

    @Nullable
    public static String getProjectPath(@Nullable Project project) {
        if (project == null) return null;
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return null;
        return projectDir.getPath();
    }

    public static boolean isFlutterProject(@Nullable Project project) {
        String projectPath = getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        File file = new File(projectPath, PubSpecUtils.getFileName());
        return file.exists();
    }

    public static void openFile(@NotNull Project project, @NotNull File file) {
        VirtualFile needOpenFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (needOpenFile == null) return;
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        editorManager.openFile(needOpenFile, true);
    }
}
