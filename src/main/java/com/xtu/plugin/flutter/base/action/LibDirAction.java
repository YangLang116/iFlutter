package com.xtu.plugin.flutter.base.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

public abstract class LibDirAction extends FlutterProjectAction {

    @Override
    public void whenUpdate(@NotNull AnActionEvent e) {
        VirtualFile selectFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (selectFile == null || !selectFile.isDirectory()) {
            e.getPresentation().setVisible(false);
            return;
        }
        Project project = e.getProject();
        String selectFilePath = selectFile.getPath();
        String projectPath = PluginUtils.getProjectPath(project);
        boolean isLibChildPath = selectFilePath.startsWith(projectPath + "/lib");
        e.getPresentation().setVisible(isLibChildPath);
    }
}
