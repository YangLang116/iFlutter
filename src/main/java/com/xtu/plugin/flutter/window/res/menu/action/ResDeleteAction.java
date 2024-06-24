package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResDeleteAction extends AnAction {

    private final File imageFile;

    public ResDeleteAction(@NotNull File imageFile) {
        super("Delete", null, PluginIcons.DELETE);
        this.imageFile = imageFile;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        VirtualFile virtualFile = localFileSystem.refreshAndFindFileByIoFile(imageFile);
        if (virtualFile == null) return;
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile == null || !psiFile.isValid()) return;
        LocalHistoryAction a = LocalHistory.getInstance().startAction(IdeBundle.message("progress.deleting"));
        try {
            DeleteHandler.deletePsiElement(new PsiElement[]{psiFile}, project);
        } finally {
            a.finish();
        }
    }
}
