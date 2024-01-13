package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.analysis.task.DepAnalysisTask;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class DepAnalysisAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        new DepAnalysisTask(project).queue();
    }
}
