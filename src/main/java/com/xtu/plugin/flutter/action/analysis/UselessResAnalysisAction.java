package com.xtu.plugin.flutter.action.analysis;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.BaseResourceAction;
import com.xtu.plugin.flutter.action.analysis.task.UselessResAnalysisTask;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class UselessResAnalysisAction extends BaseResourceAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        new UselessResAnalysisTask(project, projectPath).queue();
    }
}
