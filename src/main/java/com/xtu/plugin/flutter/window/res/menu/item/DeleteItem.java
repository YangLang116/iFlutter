package com.xtu.plugin.flutter.window.res.menu.item;

import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.util.DeleteHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.xtu.plugin.flutter.window.res.ui.ResManagerListener;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.io.File;

public class DeleteItem extends AbstractItem {

    public DeleteItem(@NotNull Project project,
                      @NotNull File imageFile,
                      @NotNull ResManagerListener listener) {
        super("Delete", project, imageFile, listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
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
