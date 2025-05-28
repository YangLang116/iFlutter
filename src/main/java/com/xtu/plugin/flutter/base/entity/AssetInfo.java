package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssetInfo {

    public final AssetInfoMeta meta;
    public final List<String> assetList;
    public final List<String> fontList;

    public AssetInfo(@NotNull AssetInfoMeta meta,
                     @NotNull List<String> assetList,
                     @NotNull List<String> fontList) {
        this.meta = meta;
        this.assetList = assetList;
        this.fontList = fontList;
    }
}
