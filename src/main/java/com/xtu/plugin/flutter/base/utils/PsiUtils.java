package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class PsiUtils {

    @Nullable
    public static <T> T findFirstChildByType(@NotNull PsiElement parentPsi, @NotNull Class<T> classType) {
        PsiElement[] children = parentPsi.getChildren();
        for (PsiElement psiChild : children) {
            if (classType.isInstance(psiChild)) return (T) psiChild;
        }
        return null;
    }

    @Nullable
    public static <T> T findFirstChild(@NotNull PsiElement parentPsi,
                                       @NotNull Class<T> classType,
                                       String prefix) {
        PsiElement[] children = parentPsi.getChildren();
        for (PsiElement psiChild : children) {
            if (!classType.isInstance(psiChild)) continue;
            String psiChildText = psiChild.getText().trim();
            if (psiChildText.startsWith(prefix)) return (T) psiChild;
        }
        return null;
    }

    @NotNull
    public static <T> List<T> findChildren(@NotNull PsiElement parentPsi,
                                           @NotNull Class<T> classType,
                                           String prefix) {
        List<T> resultList = new ArrayList<>();
        PsiElement[] children = parentPsi.getChildren();
        for (PsiElement psiChild : children) {
            if (!classType.isInstance(psiChild)) continue;
            String psiChildText = psiChild.getText().trim();
            if (psiChildText.startsWith(prefix)) resultList.add((T) psiChild);
        }
        return resultList;
    }

    public static void savePsiFile(@NotNull Project project, @NotNull PsiFile file) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);
        if (document != null) {
            //sync psi - document
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            //sync psi - vfs
            FileDocumentManager.getInstance().saveDocument(document);
        }
    }
}
