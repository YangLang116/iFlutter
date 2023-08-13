package com.xtu.plugin.flutter.advice;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.upgrader.NetworkManager;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdviceManager {

    private static final String APP_KEY = "iFlutter";
    private static final String sURL = "https://iflutter.toolu.cn/api/advice";
    private final Gson gson = new Gson();

    private AdviceManager() {
    }

    private final static AdviceManager sInstance = new AdviceManager();

    public static AdviceManager getInstance() {
        return sInstance;
    }

    public void submitAdvice(@NotNull Project project, @NotNull String title, @NotNull String content) {
        final Map<String, String> params = new HashMap<>();
        params.put("app_key", APP_KEY + "_" + PluginUtils.getVersion());
        params.put("title", title);
        params.put("content", content);
        NetworkManager.getInstance().post(sURL, gson.toJson(params), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                ToastUtil.make(project, MessageType.INFO, "thank you for submitting ~");
            }
        });
    }

}
