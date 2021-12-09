package com.xtu.plugin.flutter.action.generate.res;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.YamlUtils;
import org.jetbrains.annotations.NotNull;

public class GenerateResAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (!PluginUtils.isFlutterProject(project)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        YamlUtils.loadFromYaml(project, (yaml, data, assetList) -> DartRFileGenerator.getInstance().generate(project, assetList));
    }
}
