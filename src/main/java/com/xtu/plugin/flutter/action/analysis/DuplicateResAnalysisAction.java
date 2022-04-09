package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.BaseResourceAction;
import com.xtu.plugin.flutter.action.analysis.task.DuplicateResAnalysisTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DuplicateResAnalysisAction extends BaseResourceAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualFile == null) return;
        new Task.Backgroundable(project, "check Duplicate Resource", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String directoryPath = virtualFile.getPath();
                new DuplicateResAnalysisTask(project, indicator, new File(directoryPath)).run();
            }
        }.queue();
    }
}
