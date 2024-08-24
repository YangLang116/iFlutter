package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResCopyRefAction extends AnAction {

    private final File imageFile;

    public ResCopyRefAction(@NotNull File imageFile) {
        super("Copy Reference");
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
        if (StringUtils.isEmpty(assetPath)) return;
        String foldName = assetPath.substring(0, assetPath.indexOf("/"));
        String variantName = DartRFileGenerator.getResName(assetPath);
        String className = DartRFileGenerator.getClassName(foldName);
        String reference = className + "." + variantName;
        PluginUtils.copyToClipboard(project, reference, "copy reference success");
    }
}
