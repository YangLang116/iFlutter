package com.xtu.plugin.flutter.action.anchor.yaml;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.xtu.plugin.flutter.action.BaseDependencyAction;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class YamlPackageAnchorAction extends BaseDependencyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        Pair<String, YAMLKeyValue> packageInfo = getPackageInfo(e);
        if (packageInfo == null) return;
        String packageName = packageInfo.getFirst();
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return;
        PsiFile projectPsi = PsiManager.getInstance(project).findFile(projectDir);
        if (projectPsi == null) return;


    }
}
