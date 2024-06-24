package com.xtu.plugin.flutter.utils;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PsiUtils {

    @SuppressWarnings("unchecked")
    @Nullable
    public static <T> T findFirstChildByType(@NotNull PsiElement parentPsi, @NotNull Class<T> classType) {
        PsiElement[] children = parentPsi.getChildren();
        for (PsiElement psiChild : children) {
            if (classType.isInstance(psiChild)) return (T) psiChild;
        }
        return null;
    }


    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
}
