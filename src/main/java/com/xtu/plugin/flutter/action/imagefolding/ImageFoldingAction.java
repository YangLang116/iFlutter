package com.xtu.plugin.flutter.action.imagefolding;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.imagefolding.task.OptimizeImageFoldTask;
import com.xtu.plugin.flutter.utils.FileUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//整理images目录下的图片
public class ImageFoldingAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (!PluginUtils.isFlutterProject(project)) {
            e.getPresentation().setEnabled(false);
            return;
        }
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            e.getPresentation().setEnabled(false);
            return;
        }
        if (FileUtils.isChildFile(virtualFile.getPath(), "lib")) {
            e.getPresentation().setEnabled(false);
            return;
        }
        assert project != null;
        List<String> supportOptimizeFoldName = PluginUtils.supportImageOptimizeFoldName(project);
        if (!supportOptimizeFoldName.contains(virtualFile.getName())) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualFile == null) return;
        ApplicationManager.getApplication().runWriteAction(new OptimizeImageFoldTask(project, virtualFile));
    }
}
