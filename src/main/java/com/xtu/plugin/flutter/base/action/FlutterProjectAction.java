package com.xtu.plugin.flutter.base.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

public abstract class FlutterProjectAction extends AnAction {

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (PluginUtils.isFlutterProject(project)) {
            whenUpdate(e);
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    public void whenUpdate(@NotNull AnActionEvent e) {
        e.getPresentation().setVisible(true);
    }
}
