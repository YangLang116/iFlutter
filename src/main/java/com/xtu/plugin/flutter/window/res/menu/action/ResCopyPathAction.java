package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
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
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
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
