package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.awt.RelativePoint;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import com.xtu.plugin.flutter.window.res.sort.SortType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SortAction extends AnAction {

    private SortType sortType;
    private final IResRootPanel rootPanel;

    public SortAction(@NotNull SortType sortType, @NotNull IResRootPanel rootPanel) {
        super(sortType.icon);
        this.sortType = sortType;
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JComponent anchorComponent = rootPanel.asComponent();
        RelativePoint relativePoint = new RelativePoint(anchorComponent, new Point(anchorComponent.getWidth() / 2, 10));
        JBPopupFactory.getInstance()
                .createPopupChooserBuilder(
                        Arrays.asList(SortType.SORT_TIME, SortType.SORT_NAME, SortType.SORT_SIZE)
                )
                .setTitle("Select Sort Type")
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
                .setRenderer(new ColoredListCellRenderer<>() {
                    @Override
                    protected void customizeCellRenderer(@NotNull JList<? extends SortType> jList, SortType sortType, int i, boolean b, boolean b1) {
                        setIcon(sortType.icon);
                        append(sortType.name);
                    }
                })
                .setItemChosenCallback(sortType -> {
                    e.getPresentation().setIcon(sortType.icon);
                    refreshSortType(sortType);
                })
                .createPopup()
                .show(relativePoint);
    }

    private void refreshSortType(@Nonnull SortType newSortType) {
        if (newSortType == this.sortType) return;
        this.sortType = newSortType;
        this.rootPanel.sortRes(newSortType);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}