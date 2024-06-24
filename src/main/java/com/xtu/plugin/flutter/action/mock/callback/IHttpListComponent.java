package com.xtu.plugin.flutter.action.mock.callback;

import com.xtu.plugin.flutter.store.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IHttpListComponent {

    @NotNull
    List<HttpEntity> getHttpConfigList();

    void modifyHttpConfig(@Nullable HttpEntity httpEntity);

    void deleteHttpConfig(@NotNull HttpEntity httpEntity);

    void addHttpConfig(@NotNull HttpEntity httpEntity);
}
