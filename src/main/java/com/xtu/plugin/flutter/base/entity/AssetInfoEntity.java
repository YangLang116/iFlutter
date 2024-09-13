package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssetInfoEntity {

    public final AssetInfoMetaEntity meta;
    public final List<String> assetList;
    public final List<String> fontList;

    public AssetInfoEntity(@NotNull AssetInfoMetaEntity meta,
                           @NotNull List<String> assetList,
                           @NotNull List<String> fontList) {
        this.meta = meta;
        this.assetList = assetList;
        this.fontList = fontList;
    }
}
