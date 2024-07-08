package com.xtu.plugin.flutter.action.mock;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.mock.ui.HttpListDialog;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import org.jetbrains.annotations.NotNull;

public class HttpMockAction extends FlutterProjectAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        HttpListDialog httpListDialog = new HttpListDialog(project);
        httpListDialog.show();
    }
}
