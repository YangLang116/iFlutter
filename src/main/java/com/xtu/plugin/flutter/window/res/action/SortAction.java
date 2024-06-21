package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import com.xtu.plugin.flutter.window.res.sort.SortHelper;
import com.xtu.plugin.flutter.window.res.sort.SortType;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SortAction extends AnAction {

    private SortType sortType;
    private final IResRootPanel rootPanel;

    public SortAction(@NotNull SortType sortType, @NotNull IResRootPanel rootPanel) {
        super(SortHelper.getSortIcon(sortType));
        this.sortType = sortType;
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        SortType newSortType = SortHelper.selectSortType(project);
        if (newSortType == null || newSortType == this.sortType) return;
        this.sortType = newSortType;
        this.rootPanel.sortRes(newSortType);
        Icon icon = SortHelper.getSortIcon(newSortType);
        e.getPresentation().setIcon(icon);
    }
}