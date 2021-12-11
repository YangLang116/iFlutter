package com.xtu.plugin.flutter.action.mock.manager;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.service.HttpEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.HttpUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Locale;

public class HttpMockManager {

    private static volatile HttpMockManager sInstance;

    private Project project;
    private MockWebServer webServer;

    private HttpMockManager(Project project) {
        this.project = project;
    }

    public static HttpMockManager getInstance(Project project) {
        if (sInstance == null) {
            synchronized (HttpMockManager.class) {
                if (sInstance == null) {
                    sInstance = new HttpMockManager(project);
                }
            }
        }
        return sInstance;
    }

    public void activeServerIfNeed() {
        if (webServer != null) return;
        try {
            MockWebServer server = new MockWebServer();
            server.setDispatcher(new HttpDispatcher(project));
            String localIp = HttpUtils.getHostIp();
            server.start(InetAddress.getByName(localIp), 0);
            webServer = server;
        } catch (Exception e) {
            LogUtils.error("HttpMockManager activeServerIfNeed: " + e.getMessage());
        }
    }

    public String getBaseUrl() {
        activeServerIfNeed();
        if (webServer == null) return null;
        return String.format(Locale.US, "http://%s:%d", webServer.getHostName(), webServer.getPort());
    }

    public String getUrl(String path) {
        activeServerIfNeed();
        if (webServer == null) return null;
        return webServer.url(path).toString();
    }

    public void release() {
        if (webServer != null) {
            try {
                webServer.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.project = null;
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
