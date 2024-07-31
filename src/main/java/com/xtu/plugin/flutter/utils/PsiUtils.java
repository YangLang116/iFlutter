package com.xtu.plugin.flutter.utils;

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

    //替换psiFile.replace()处理，避免出现 `because "treeParent" is null`
    public static PsiFile replacePsiFile(@NotNull PsiFile originFile, @NotNull PsiFile newFile) {
        for (PsiElement oldEl : originFile.getChildren()) {
            oldEl.delete();
        }
        for (PsiElement newEl : newFile.getChildren()) {
            originFile.add(newEl);
        }
        return originFile;
    }
}
