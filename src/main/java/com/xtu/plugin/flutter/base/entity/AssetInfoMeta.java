package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

public class AssetInfoMeta {

    public final String projectName;
    public final String projectVersion;

    public AssetInfoMeta(@NotNull String projectName, @NotNull String projectVersion) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
    }
}
