package com.xtu.plugin.flutter.action.analysis.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.xtu.plugin.flutter.action.analysis.ui.AnalysisResultDialog;
import com.xtu.plugin.flutter.utils.CommandUtils;
import com.xtu.plugin.flutter.utils.DartUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DepAnalysisTask extends Task.Backgroundable {

    private final File projectRootDir;

    public DepAnalysisTask(Project project, File projectRootDir) {
        super(project, "analysis project dependencies", false);
        this.projectRootDir = projectRootDir;
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
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) {
            show(project, "must set flutter.sdk in local.properties");
            return;
        }
        String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
        File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
        String command = executorFile.getAbsolutePath() + " pub deps";
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, projectRootDir);
        show(project, commandResult.result);
    }

    private void show(Project project, String message) {
        ApplicationManager.getApplication().invokeLater(() -> new AnalysisResultDialog(project, message,
                "input dependence name",
                null).show());
    }
}
