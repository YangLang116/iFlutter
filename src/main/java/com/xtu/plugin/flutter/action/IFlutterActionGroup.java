package com.xtu.plugin.flutter.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class IFlutterActionGroup extends DefaultActionGroup {

    public IFlutterActionGroup() {
        //noinspection DialogTitleCapitalization
        super("iFlutter", "Flutter Development Kit", PluginIcons.LOGO);
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }
}
