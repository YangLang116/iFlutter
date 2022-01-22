package com.xtu.plugin.flutter.action.mock;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.mock.ui.HttpListDialog;
import org.jetbrains.annotations.NotNull;

public class HttpMockAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Project project = e.getProject();
        e.getPresentation().setVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        HttpListDialog httpListDialog = new HttpListDialog(project);
        httpListDialog.show();
    }
}
