package com.xtu.plugin.flutter.window.res.helper;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.tinify.Tinify;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.configuration.SettingsConfiguration;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.io.File;

public class ResMenuHelper {

    public static JPopupMenu createMenu(@NotNull Project project,
                                        @NotNull File imageFile,
                                        @NotNull OnReloadListener onReloadListener) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createAction("Copy Reference", e -> copyReference(project, imageFile)));
        menu.add(createAction("Compress Image", e -> compressImage(project, imageFile, onReloadListener)));
        menu.add(createAction("Open In File Browser", e -> RevealFileAction.openFile(imageFile)));
        return menu;
    }

    private static void compressImage(@NotNull Project project,
                                      @NotNull File imageFile,
                                      @NotNull OnReloadListener onReloadListener) {
        final String tinyKey = StorageService.getInstance(project).getState().tinyApiKey;
        if (StringUtils.isEmpty(tinyKey)) {
            ToastUtil.make(project, MessageType.INFO, "add api key for TinyPng");
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfiguration.class);
            return;
        }
        final Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            try {
                Tinify.setKey(tinyKey);
                String imageFilePath = imageFile.getAbsolutePath();
                Tinify.fromFile(imageFilePath).toFile(imageFilePath);
                ToastUtil.make(project, MessageType.INFO, "compress image success");
                application.invokeLater(() -> onReloadListener.reload(imageFile));
            } catch (Exception e) {
                LogUtils.error("ResMenuHelper compressImage: " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, "compress image fail: " + e.getMessage());
            }
        });
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

    private static JMenuItem createAction(@NotNull String title, @NotNull ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        return menuItem;
    }

    public interface OnReloadListener {

        void reload(@NotNull File changeFile);

    }
}
