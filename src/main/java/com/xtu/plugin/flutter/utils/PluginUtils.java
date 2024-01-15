package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.flutter.FlutterUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PluginUtils {

    private static final Map<String, Boolean> sIsFlutterProject = new HashMap<>();

    @Nullable
    public static String getProjectPath(@Nullable Project project) {
        if (project == null) return null;
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return null;
        return projectDir.getPath();
    }

    public static boolean isFlutterProject(@Nullable Project project) {
        if (project == null) return false;
        String projectPath = project.getBasePath();
        if (sIsFlutterProject.containsKey(projectPath)) {
            return sIsFlutterProject.get(projectPath);
        }
        VirtualFile rootPubSpecFile = PubSpecUtils.getRootPubSpecFile(project);
        if (rootPubSpecFile == null) return false;
        boolean isFlutterProject = FlutterUtil.isPubspecDeclaringFlutter(rootPubSpecFile);
        sIsFlutterProject.put(projectPath, isFlutterProject);
        return isFlutterProject;
    }

    public static void openFile(@NotNull Project project, @NotNull File file) {
        VirtualFile needOpenFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (needOpenFile == null) return;
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        editorManager.openFile(needOpenFile, true);
    }
}
