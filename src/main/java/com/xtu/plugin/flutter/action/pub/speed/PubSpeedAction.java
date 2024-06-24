package com.xtu.plugin.flutter.action.pub.speed;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidGradleParser;
import com.xtu.plugin.flutter.base.dialog.ComboBoxDialog;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PubSpeedAction extends AnAction {

    private static final String sInjectTitle = "Select Injection Type";
    private static final List<String> sInjectTypeList = Arrays.asList(
            "inject all",
            "inject to project",
            "inject to plugin",
            "inject to flutter script"
    );

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
        ComboBoxDialog typeDialog = new ComboBoxDialog(project, sInjectTitle, sInjectTypeList, getClass().getSimpleName());
        boolean isOk = typeDialog.showAndGet();
        if (!isOk) return;
        int selectIndex = typeDialog.getSelectIndex();
        AndroidGradleParser.start(project,
                canInject(selectIndex, 1),
                canInject(selectIndex, 2),
                canInject(selectIndex, 3));
    }

    private boolean canInject(int selectIndex, int where) {
        return selectIndex == where || selectIndex == 0;
    }
}
