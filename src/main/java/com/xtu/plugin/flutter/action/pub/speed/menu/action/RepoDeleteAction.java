package com.xtu.plugin.flutter.action.pub.speed.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RepoDeleteAction extends AnAction {

    private final String repo;
    private final DefaultListModel<String> model;

    public RepoDeleteAction(@NotNull String repo, @NotNull DefaultListModel<String> model) {
        super("Delete", null, PluginIcons.DELETE);
        this.repo = repo;
        this.model = model;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        model.removeElement(repo);
    }
}
