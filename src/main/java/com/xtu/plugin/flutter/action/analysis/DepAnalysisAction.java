package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.analysis.task.DepAnalysisTask;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import org.jetbrains.annotations.NotNull;

public class DepAnalysisAction extends FlutterProjectAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        new DepAnalysisTask(project).queue();
    }
}
