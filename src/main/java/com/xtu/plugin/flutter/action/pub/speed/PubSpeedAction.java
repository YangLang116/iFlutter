package com.xtu.plugin.flutter.action.pub.speed;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidGradleParser;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class PubSpeedAction extends AnAction {

    private static final String sInjectTitle = "Select injection type";
    private static final String[] sInjectTypeList = new String[]{
            "inject all",
            "inject to flutter script",
            "inject to plugin",
            "inject to project"
    };

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        int selectIndex = Messages.showDialog(project, sInjectTitle, "", sInjectTypeList, -1, null);
        if (selectIndex == -1) return;
        AndroidGradleParser.start(
                project,
                canInject(selectIndex, 3),
                canInject(selectIndex, 2),
                canInject(selectIndex, 1)
        );
    }

    private boolean canInject(int selectIndex, int where) {
        return selectIndex == where || selectIndex == 0;
    }
}
