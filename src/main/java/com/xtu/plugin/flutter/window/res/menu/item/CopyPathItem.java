package com.xtu.plugin.flutter.window.res.menu.item;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import com.xtu.plugin.flutter.window.res.ui.ResManagerListener;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;

public class CopyPathItem extends AbstractItem {

    public CopyPathItem(@NotNull Project project,
                        @NotNull File imageFile,
                        @NotNull ResManagerListener listener) {
        super("Copy Path", project, imageFile, listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String projectPath = PluginUtils.getProjectPath(project);
        String assetPath = AssetUtils.getAssetPath(projectPath, imageFile);
        if (StringUtils.isEmpty(assetPath)) return;
        StringSelection content = new StringSelection(assetPath);
        CopyPasteManager.getInstance().setContents(content);
        ToastUtil.make(project, MessageType.INFO, "copy path success");
    }
}
