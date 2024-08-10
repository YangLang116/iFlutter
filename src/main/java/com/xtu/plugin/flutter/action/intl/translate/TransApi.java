package com.xtu.plugin.flutter.action.intl.translate;

import com.xtu.plugin.flutter.base.utils.Md5Utils;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TransApi {

    private static final String TRANS_API_HOST = "https://fanyi-api.baidu.com/api/trans/vip/translate";

    private final String appId;
    private final String appSecretKey;
    private final OkHttpClient okHttpClient;

    public TransApi(@NotNull String appId, @NotNull String appSecretKey) {
        this.appId = appId;
        this.appSecretKey = appSecretKey;
        this.okHttpClient = initClient();
    }

    private OkHttpClient initClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appId);
        String salt = String.valueOf(System.currentTimeMillis());  // 随机数
        params.put("salt", salt);
        String src = appId + query + salt + appSecretKey; // 加密前的原文
        params.put("sign", Md5Utils.md5(src));
        return params;
    }

    public void getTransResult(String query,
                               String from,
                               String to,
                               Callback callback) {
        Map<String, String> params = buildParams(query, from, to);
        FormBody.Builder requestBodyBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            requestBodyBuilder.add(entry.getKey(), entry.getValue());
        }
        Request request = new Request.Builder()
                .url(TRANS_API_HOST)
                .post(requestBodyBuilder.build())
                .build();
        this.okHttpClient.newCall(request).enqueue(callback);
    }
}
