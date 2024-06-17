package com.xtu.plugin.flutter.action.usage;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.PluginUtils;

import org.jetbrains.annotations.NotNull;

public class UsageAction extends AnAction {

    @SuppressWarnings("HttpUrlsUsage")
    private static final String URL_DOCUMENT = "http://iflutter.toolu.cn";

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.open(URL_DOCUMENT);
    }
}
