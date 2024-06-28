package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.store.ide.IdeStorageService;
import com.xtu.plugin.flutter.store.ide.entity.IdeStorageEntity;
import com.xtu.plugin.flutter.utils.StringUtils;
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

    @Override
    public void update(@NotNull AnActionEvent e) {
        IdeStorageEntity ideStorage = IdeStorageService.getStorage();
        boolean hasTinyKey = !StringUtils.isEmpty(ideStorage.tinyApiKey);
        e.getPresentation().setEnabled(hasTinyKey);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}