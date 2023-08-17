package com.xtu.plugin.flutter.action.pub.search;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

//Package 快捷搜索
public class PubSearchAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        String searchText = Messages.showInputDialog(project, "", "Search Package from Pub", null);
        if (searchText == null) return;
        String url = "https://pub.dev/packages?q=" + searchText.trim();
        BrowserUtil.open(url);
    }
}
