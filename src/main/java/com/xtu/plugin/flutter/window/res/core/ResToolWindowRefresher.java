package com.xtu.plugin.flutter.window.res.core;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.util.messages.MessageBusConnection;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.FileUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import com.xtu.plugin.flutter.window.res.ResToolWindowFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResToolWindowRefresher extends ResFileChangedObserver implements ToolWindowManagerListener {

    private static final List<String> SUPPORT_IMAGE_FORMAT = Arrays.asList("jpg", "jpeg", "png", "webp", "svg");

    private final IResRootPanel rootPanel;
    private boolean lastVisibleState;

    public ResToolWindowRefresher(@NotNull Project project, @NotNull IResRootPanel rootPanel) {
        super(project, SUPPORT_IMAGE_FORMAT);
        this.rootPanel = rootPanel;
    }

    public void init() {
        bindFileChangedEvent();
        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(ToolWindowManagerListener.TOPIC, this);
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager wm) {
        ToolWindow toolWindow = wm.getToolWindow(ResToolWindowFactory.WindowID);
        if (toolWindow == null) return;
        boolean isVisible = toolWindow.isVisible();
        if (isVisible == this.lastVisibleState) return;
        this.lastVisibleState = isVisible;
        if (isVisible) {
            LogUtils.info("ResManagerToolWindowListener scanResList");
            scanResList();
        } else {
            LogUtils.info("ResManagerToolWindowListener clearScanResList");
            clearScanResList();
        }
    }

    @Override
    public void scanResList() {
        Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final List<File> resList = new ArrayList<>();
            List<File> allAssetFile = AssetUtils.getAllAssetFile(project);
            for (File file : allAssetFile) {
                String extension = FileUtils.getExtension(file);
                if (StringUtils.isEmpty(extension)) continue;
                if (SUPPORT_IMAGE_FORMAT.contains(extension)) resList.add(file);
            }
            application.invokeLater(() -> rootPanel.loadResList(resList));
        });
    }

    private void clearScanResList() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(rootPanel::cleanResList);
    }
}
