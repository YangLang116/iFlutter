package com.xtu.plugin.flutter.window.res.core;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.util.Alarm;
import com.intellij.util.messages.MessageBusConnection;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.FileUtils;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.window.res.ResToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 资源面板刷新控制器。
 *
 * <p>防抖优化：{@code scanResList()} 不再每次文件事件立刻触发全量扫描，
 * 而是通过 {@link Alarm} 在 300ms 内合并多次请求，只执行一次实际扫描。
 * 解决批量文件操作（如 git checkout、多文件粘贴）时面板被频繁重建的问题。</p>
 */
public class ResToolWindowRefresher extends ResToolWindowContentObserver implements ToolWindowManagerListener {

    private static final List<String> SUPPORT_IMAGE_FORMAT = Arrays.asList("jpg", "jpeg", "png", "webp", "svg");
    private static final int SCAN_DEBOUNCE_MS = 300;

    private final IResRootPanel rootPanel;
    private final Alarm scanAlarm;
    private boolean lastVisibleState;

    public ResToolWindowRefresher(@NotNull Project project, @NotNull IResRootPanel rootPanel) {
        super(project, SUPPORT_IMAGE_FORMAT);
        this.rootPanel = rootPanel;
        // project 是 Disposable，Alarm 随项目关闭自动释放
        this.scanAlarm = new Alarm(project);
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

    /**
     * 请求刷新资源列表。多次调用在 300ms 内只执行一次实际扫描。
     */
    @Override
    public void scanResList() {
        scanAlarm.cancelAllRequests();
        scanAlarm.addRequest(this::doScanResList, SCAN_DEBOUNCE_MS);
    }

    private void doScanResList() {
        Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final List<File> resList = new ArrayList<>();
            List<File> allAssetFile = AssetUtils.getAllAssetFile(project);
            for (File file : allAssetFile) {
                String extension = FileUtils.getExtension(file);
                if (StringUtils.isEmpty(extension)) continue;
                if (SUPPORT_IMAGE_FORMAT.contains(extension.toLowerCase())) resList.add(file);
            }
            application.invokeLater(() -> rootPanel.loadResList(resList));
        });
    }

    private void clearScanResList() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(rootPanel::cleanResList);
    }
}
