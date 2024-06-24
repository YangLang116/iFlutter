package com.xtu.plugin.flutter.window.res.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.TinyUtils;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ResCompressAction extends AnAction {

    private final File imageFile;
    private final IResRootPanel rootPanel;

    public ResCompressAction(@NotNull File imageFile, @NotNull IResRootPanel rootPanel) {
        super("Compress Image", null, PluginIcons.COMPRESS);
        this.imageFile = imageFile;
        this.rootPanel = rootPanel;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        final List<File> targetList = List.of(imageFile);
        TinyUtils.compressImage(project, targetList, (successList) -> {
            if (successList.isEmpty()) return;
            rootPanel.reloadItems(successList);
        });
    }
}
