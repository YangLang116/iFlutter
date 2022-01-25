package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.analysis.task.DuplicateResAnalysisTask;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class DuplicateResAnalysisAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) {
            e.getPresentation().setVisible(false);
            return;
        }
        if (PluginUtils.isNotFlutterProject(project)) {
            e.getPresentation().setVisible(false);
            return;
        }
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || !virtualFile.isDirectory()) {
            e.getPresentation().setVisible(false);
            return;
        }
        String selectDirPath = virtualFile.getPath();
        List<String> assetFoldNameList = PluginUtils.supportAssetFoldName(project);
        for (String foldName : assetFoldNameList) {
            if (selectDirPath.equals(projectPath + File.separator + foldName)) {
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }

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
