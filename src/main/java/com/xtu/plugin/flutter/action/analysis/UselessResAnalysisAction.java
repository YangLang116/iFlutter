package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.BaseResourceAction;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.jetbrains.annotations.NotNull;

public class UselessResAnalysisAction extends BaseResourceAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ToastUtil.make(project, MessageType.INFO, "测试");
    }
}
