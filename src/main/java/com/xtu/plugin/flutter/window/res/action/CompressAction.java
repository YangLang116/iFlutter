package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class CompressAction extends AnAction {

    private final IResRootPanel rootPanel;

    public CompressAction(@NotNull IResRootPanel rootPanel) {
        super(PluginIcons.COMPRESS);
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        this.rootPanel.compressRes();
    }
}