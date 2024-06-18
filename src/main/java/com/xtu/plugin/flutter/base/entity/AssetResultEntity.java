package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class AssetResultEntity {

    public final String projectName;
    public final String projectVersion;
    public final List<String> assetList;
    public final List<String> fontList;

    public AssetResultEntity(@NotNull String projectName,
                             @NotNull String projectVersion,
                             @NotNull List<String> assetList,
                             @NotNull List<String> fontList) {
        this.projectName = projectName;
        this.projectVersion = projectVersion;
        this.assetList = assetList;
        this.fontList = fontList;
    }
}
