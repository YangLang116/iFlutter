package com.xtu.plugin.flutter.upgrader;

import okhttp3.*;

import java.util.concurrent.TimeUnit;

public class NetworkManager {

    private final OkHttpClient mOkHttpClient;

    private NetworkManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    private static final NetworkManager INSTANCE = new NetworkManager();

    public static NetworkManager getInstance() {
        return INSTANCE;
    }

    public void get(String requestUrl, Callback callback) {
        Request request = new Request.Builder()
                .get()
                .url(requestUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }

    public void post(String requestUrl, String json, Callback callback) {
        RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder().post(jsonBody).url(requestUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(callback);
    }
}
