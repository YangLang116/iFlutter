package com.xtu.plugin.flutter.window.res.listener;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.xtu.plugin.flutter.utils.FileUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ResManagerToolWindowListener implements ToolWindowManagerListener {

    private static final String ID = "Flutter Resource";
    private static final List<String> SUPPORT_IMAGE_FORMAT = Arrays.asList("jpg", "jpeg", "png", "webp", "svg");

    private boolean isVisible;
    private final Project project;

    public ResManagerToolWindowListener(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
        ToolWindow toolWindow = toolWindowManager.getToolWindow(ID);
        if (toolWindow == null) return;
        boolean visible = toolWindow.isVisible();
        if (visible == this.isVisible) return;
        this.isVisible = visible;
        if (visible) {
            LogUtils.info("ResManagerToolWindowListener scanResList");
            scanResList(toolWindow);
        } else {
            LogUtils.info("ResManagerToolWindowListener clearScanResList");
            clearScanResList(toolWindow);
        }
    }

    private void scanResList(@NotNull ToolWindow toolWindow) {
        final String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        final List<String> supportAssetFoldName = PluginUtils.supportAssetFoldName(project);
        Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final List<File> resList = new ArrayList<>();
            for (String foldName : supportAssetFoldName) {
                File resDirectory = new File(projectPath, foldName);
                FileUtils.scanDirectory(resDirectory, file -> {
                    String extension = FileUtils.getExtension(file);
                    if (StringUtils.isEmpty(extension)) return;
                    boolean support = SUPPORT_IMAGE_FORMAT.contains(extension.toLowerCase(Locale.ROOT));
                    if (!support) return;
                    resList.add(file);
                });
            }
            application.invokeLater(() -> {
                ResManagerRootPanel rootPanel = getResManagerRootPanel(toolWindow);
                if (rootPanel == null) return;
                rootPanel.refreshResList(resList);
            });
        });
    }

    private void clearScanResList(@NotNull ToolWindow toolWindow) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            ResManagerRootPanel rootPanel = getResManagerRootPanel(toolWindow);
            if (rootPanel == null) return;
            rootPanel.cleanResList();
        });
    }

    @Nullable
    private static ResManagerRootPanel getResManagerRootPanel(@NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getContent(0);
        if (content == null) return null;
        return ((ResManagerRootPanel) content.getComponent());
    }
}
