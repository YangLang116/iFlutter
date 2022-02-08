package com.xtu.plugin.flutter.action.mock.manager;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.service.HttpEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.HttpUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;

public final class HttpMockManager {

    private final Project project;
    private final MockWebServer webServer;
    private String hostIP = "localhost";

    public HttpMockManager(@NotNull Project project) {
        this.project = project;
        this.webServer = new MockWebServer();
        this.webServer.setDispatcher(new HttpDispatcher(project));
    }

    public static HttpMockManager getService(@NotNull Project project) {
        return project.getService(HttpMockManager.class);
    }

    public void activeServer() {
        try {
            InetAddress inetAddress = Inet4Address.getLocalHost();
            String localIp = HttpUtils.getLocalIP();
            if (StringUtils.isNotEmpty(localIp)) {
                hostIP = localIp;
                inetAddress = InetAddress.getByName(localIp);
            }
            this.webServer.start(inetAddress, 0);
        } catch (Exception e) {
            LogUtils.error("HttpMockManager activeServerIfNeed: " + e.getMessage());
            ToastUtil.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    public void releaseServer() {
        try {
            webServer.shutdown();
        } catch (IOException e) {
            LogUtils.error("HttpMockManager releaseServer: " + e.getMessage());
        }
    }

    public String getBaseUrl() {
        return String.format(Locale.US, "http://%s:%d", hostIP, webServer.getPort());
    }

    public String getUrl(String path) {
        return getBaseUrl() + path;
    }

    private static class HttpDispatcher extends Dispatcher {

        private final Project project;

        public HttpDispatcher(Project project) {
            this.project = project;
        }

        @NotNull
        @Override
        public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
            String path = recordedRequest.getPath();
            String method = recordedRequest.getMethod();
            List<HttpEntity> httpEntityList = StorageService.getInstance(project).getState().httpEntityList;
            for (HttpEntity httpEntity : httpEntityList) {
                if (StringUtils.equals(httpEntity.path, path)) {
                    if (StringUtils.equalsIgnoreCase(httpEntity.method, method)) {
                        return new MockResponse()
                                .setResponseCode(200)
                                .setBody(httpEntity.response);
                    } else {
                        return new MockResponse()
                                .setResponseCode(400)
                                .setBody("Request Method not Support");
                    }
                }
            }
            return new MockResponse().setResponseCode(404);
        }
    }
}