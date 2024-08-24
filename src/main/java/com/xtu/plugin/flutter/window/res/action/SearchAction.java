package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class SearchAction extends AnAction {

    private final IResRootPanel rootPanel;

    public SearchAction(@NotNull IResRootPanel rootPanel) {
        super(PluginIcons.SEARCH);
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.rootPanel.searchRes();
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}