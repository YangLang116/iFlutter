package com.xtu.plugin.flutter.action.image;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.image.task.OptimizeImageFoldTask;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//整理images目录下的图片
public class ImageFoldingAction extends AnAction {

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
        List<String> imageOptimizeFoldName = PluginUtils.supportAssetFoldName(project);
        for (String foldName : imageOptimizeFoldName) {
            if (selectDirPath.equals(projectPath + "/" + foldName)) {
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualFile == null) return;
        ApplicationManager.getApplication().runWriteAction(new OptimizeImageFoldTask(project, virtualFile));
    }
}
