package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResCopyPathAction extends AnAction {

    private final File imageFile;

    public ResCopyPathAction(@NotNull File imageFile) {
        super("Copy Path", null, PluginIcons.COPY);
        this.imageFile = imageFile;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        String projectPath = PluginUtils.getProjectPath(project);
        String assetPath = AssetUtils.getAssetPath(projectPath, imageFile);
        PluginUtils.copyToClipboard(project, assetPath, "copy path success");
    }
}
