package com.xtu.plugin.flutter.action.image;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.BaseResourceAction;
import com.xtu.plugin.flutter.action.image.task.OptimizeImageFoldTask;
import org.jetbrains.annotations.NotNull;

//整理images目录下的图片
public class ImageFoldingAction extends BaseResourceAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualFile == null) return;
        ApplicationManager.getApplication().runWriteAction(new OptimizeImageFoldTask(project, virtualFile));
    }
}
