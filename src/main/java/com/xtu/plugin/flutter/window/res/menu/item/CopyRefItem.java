package com.xtu.plugin.flutter.window.res.menu.item;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import com.xtu.plugin.flutter.window.res.ui.ResManagerListener;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;

public class CopyRefItem extends AbstractItem {

    public CopyRefItem(@NotNull Project project,
                       @NotNull File imageFile,
                       @NotNull ResManagerListener listener) {
        super("Copy Reference", project, imageFile, listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String projectPath = PluginUtils.getProjectPath(project);
        String assetPath = AssetUtils.getAssetPath(projectPath, imageFile);
        if (StringUtils.isEmpty(assetPath)) return;
        String foldName = assetPath.substring(0, assetPath.indexOf("/"));
        String variantName = DartRFileGenerator.getResName(assetPath);
        String className = DartRFileGenerator.getClassName(foldName);
        String reference = className + "." + variantName;
        StringSelection content = new StringSelection(reference);
        CopyPasteManager.getInstance().setContents(content);
        ToastUtil.make(project, MessageType.INFO, "copy reference success");
    }
}
