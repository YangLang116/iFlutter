package com.xtu.plugin.flutter.action.mock.manager;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.mock.manager.dispatcher.HttpDispatcher;
import com.xtu.plugin.flutter.base.utils.HttpUtils;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import okhttp3.mockwebserver.MockWebServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Locale;

public final class HttpMockManager implements Disposable {

    private String hostIP;
    private final Project project;
    private final MockWebServer webServer;

    public HttpMockManager(@NotNull Project project) {
        this.project = project;
        this.webServer = new MockWebServer();
        this.webServer.setDispatcher(new HttpDispatcher(project));
    }

    public static HttpMockManager getService(@NotNull Project project) {
        return project.getService(HttpMockManager.class);
    }

    public void activeServer() {
        LogUtils.info("HttpMockManager activeServer");
        if (!PluginUtils.isFlutterProject(project)) return;
        this.hostIP = HttpUtils.getLocalIP();
        try {
            InetAddress inetAddress = InetAddress.getByName(this.hostIP);
            this.webServer.start(inetAddress, 0);
        } catch (Exception e) {
            LogUtils.error("HttpMockManager activeServerIfNeed", e);
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private void releaseServer() {
        LogUtils.info("HttpMockManager releaseServer");
        try {
            webServer.shutdown();
        } catch (IOException e) {
            LogUtils.error("HttpMockManager releaseServer", e);
        }
    }

    @SuppressWarnings("HttpUrlsUsage")
    public String getBaseUrl() {
        return String.format(Locale.US, "http://%s:%d", hostIP, webServer.getPort());
    }

    public String getUrl(String path) {
        return getBaseUrl() + path;
    }

    @Override
    public void dispose() {
        releaseServer();
    }
}