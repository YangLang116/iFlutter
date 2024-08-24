package com.xtu.plugin.flutter.action.pub.speed.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.action.pub.speed.menu.action.RepoDeleteAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RepoMenuGroup extends ActionGroup {

    private final String repo;
    private final DefaultListModel<String> model;

    public RepoMenuGroup(@NotNull String repo, @NotNull DefaultListModel<String> model) {
        this.repo = repo;
        this.model = model;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    @NotNull
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        List<AnAction> actionList = new ArrayList<>();
        actionList.add(new RepoDeleteAction(repo, model));
        return actionList.toArray(new AnAction[0]);
    }
}
