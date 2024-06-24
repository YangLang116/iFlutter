package com.xtu.plugin.flutter.action.analysis.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.analysis.ui.AnalysisResultDialog;
import com.xtu.plugin.flutter.utils.CommandUtils;
import org.jetbrains.annotations.NotNull;

public class DepAnalysisTask extends Task.Backgroundable {

    public DepAnalysisTask(Project project) {
        super(project, "analysis project dependencies", false);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        execute();
        indicator.setIndeterminate(false);
        indicator.setFraction(1);
    }

    private void execute() {
        Project project = getProject();
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(project, "pub deps", -1);
        show(project, commandResult.result);
    }

    private void show(Project project, String message) {
        ApplicationManager.getApplication().invokeLater(() -> new AnalysisResultDialog(project, message,
                "input dependence name",
                null).show());
    }
}
