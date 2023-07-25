package com.xtu.plugin.flutter.window.res.helper;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.TinyUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

public class ResMenuHelper {

    public static JPopupMenu createMenu(@NotNull Project project,
                                        @NotNull File imageFile,
                                        @NotNull TinyUtils.OnReloadListener onReloadListener) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createAction("Copy Reference", e -> copyReference(project, imageFile)));
        menu.add(createAction("Copy Path", e -> copyPath(project, imageFile)));
        if (TinyUtils.isSupport(imageFile)) {
            menu.add(createAction("Compress Image", e ->
                    TinyUtils.compressImage(project, List.of(imageFile), onReloadListener)));
        }
        menu.add(createAction("Open In File Browser", e -> RevealFileAction.openFile(imageFile)));
        return menu;
    }

    private static void copyReference(@NotNull Project project, @NotNull File imageFile) {
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

    private static void copyPath(@NotNull Project project, @NotNull File imageFile) {
        String projectPath = PluginUtils.getProjectPath(project);
        String assetPath = AssetUtils.getAssetPath(projectPath, imageFile);
        if (StringUtils.isEmpty(assetPath)) return;
        StringSelection content = new StringSelection(assetPath);
        CopyPasteManager.getInstance().setContents(content);
        ToastUtil.make(project, MessageType.INFO, "copy path success");
    }

    private static JMenuItem createAction(@NotNull String title, @NotNull ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        return menuItem;
    }
}
