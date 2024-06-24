package com.xtu.plugin.flutter.advice;

import com.google.gson.Gson;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.SystemInfo;
import com.xtu.plugin.flutter.base.net.NetworkManager;
import com.xtu.plugin.flutter.utils.CloseUtils;
import com.xtu.plugin.flutter.utils.ToastUtils;
import com.xtu.plugin.flutter.utils.VersionUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdviceManager {

    private static final String APP_KEY = "iFlutter";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String sURL = "http://iflutter.toolu.cn/api/advice";
    private final Gson gson = new Gson();

    private AdviceManager() {
    }

    private final static AdviceManager sInstance = new AdviceManager();

    public static AdviceManager getInstance() {
        return sInstance;
    }

    public void submitAdvice(@Nullable Project project, @NotNull String title, @NotNull String content) {
        ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
        final Map<String, String> params = new HashMap<>();
        params.put("title", title);
        params.put("content", content);
        params.put("app_key", APP_KEY);
        params.put("version", VersionUtils.getPluginVersion());
        params.put("os", SystemInfo.getOsNameAndVersion());
        params.put("ide", appInfo.getFullApplicationName());
        params.put("build", appInfo.getBuild().asString());
        NetworkManager.getInstance().post(sURL, gson.toJson(params), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if (project != null) ToastUtils.make(project, MessageType.ERROR, e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                CloseUtils.close(response);
                if (project != null) ToastUtils.make(project, MessageType.INFO, "thank you for submitting ~");
            }
        });
    }

}
