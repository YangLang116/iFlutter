package com.xtu.plugin.flutter.window.res.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import com.xtu.plugin.flutter.window.res.ui.SortType;

import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class SortAction extends AnAction {

    private SortType sortType;
    private final ResManagerRootPanel rootPanel;

    public SortAction(@NotNull SortType sortType, @NotNull ResManagerRootPanel rootPanel) {
        super(getDisplayIcon(sortType));
        this.sortType = sortType;
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        SortType newSortType = getNextSortType(this.sortType);
        Presentation presentation = e.getPresentation();
        presentation.setIcon(getDisplayIcon(newSortType));
        this.rootPanel.sort(newSortType);
        this.sortType = newSortType;
    }

    private static SortType getNextSortType(@NotNull SortType sortType) {
        switch (sortType) {
            case SORT_TIME:
                return SortType.SORT_SIZE;
            case SORT_SIZE:
                return SortType.SORT_AZ;
            default:
                return SortType.SORT_TIME;
        }
    }

    private static Icon getDisplayIcon(@NotNull SortType sortType) {
        switch (sortType) {
            case SORT_AZ:
                return AllIcons.ObjectBrowser.Sorted;
            case SORT_SIZE:
                return AllIcons.ObjectBrowser.SortedByUsage;
            default:
                return AllIcons.ObjectBrowser.SortByType;
        }
    }
}