package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

public class AssetInfoMetaEntity {

    public final String projectName;
    public final String projectVersion;

    public AssetInfoMetaEntity(@NotNull String projectName, @NotNull String projectVersion) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
    }
}
