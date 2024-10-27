package com.xtu.plugin.flutter.store.project.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.util.xmlb.Converter;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HttpEntityConverter extends Converter<List<HttpEntity>> {

    private final Gson gson = new Gson();

    @Nullable
    @Override
    public List<HttpEntity> fromString(@NotNull String s) {
        return gson.fromJson(s, new TypeToken<>() {
        });
    }

    @Nullable
    @Override
    public String toString(List<HttpEntity> list) {
        return gson.toJson(list);
    }
}
