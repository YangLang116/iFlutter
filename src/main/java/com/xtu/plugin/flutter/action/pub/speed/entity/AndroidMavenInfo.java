package com.xtu.plugin.flutter.action.pub.speed.entity;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AndroidMavenInfo {

    public final List<String> mavenList;
    public final PsiElement repositories;

    public AndroidMavenInfo(@NotNull List<String> mavenList, @NotNull PsiElement repositories) {
        this.mavenList = mavenList;
        this.repositories = repositories;
    }
}
