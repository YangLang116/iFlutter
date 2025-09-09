package com.xtu.plugin.flutter.action.convert;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.convert.task.ConvertDependencyToLocalTask;
import com.xtu.plugin.flutter.action.convert.utils.DepUtils;
import com.xtu.plugin.flutter.base.action.YamlDependencyAction;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;

public class ConvertDepToLocalAction extends YamlDependencyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        Pair<String, YAMLKeyValue> packageInfo = getPackageInfo(e);
        if (packageInfo == null) return;
        File saveDir = DepUtils.selectSaveDir(project);
        if (saveDir == null) return;
        new ConvertDependencyToLocalTask(project, packageInfo.getFirst(), packageInfo.getSecond(), saveDir).queue();
    }
}
