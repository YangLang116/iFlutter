package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ResOpenFileAction extends AnAction {

    private final File imageFile;

    public ResOpenFileAction(@NotNull File imageFile) {
        super("Open In File Browser", null, PluginIcons.OPEN);
        this.imageFile = imageFile;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        PluginUtils.openFileInBrowser(imageFile);
    }
}
