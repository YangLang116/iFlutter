package com.xtu.plugin.flutter.upgrader;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

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
}
