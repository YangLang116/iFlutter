package com.xtu.plugin.flutter.action.mock.manager.dispatcher;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.service.HttpEntity;
import com.xtu.plugin.flutter.service.StorageService;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class HttpDispatcher extends Dispatcher {

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
                            .setHeader("Content-Type", "application/json; charset=utf-8")
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