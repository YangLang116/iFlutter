package com.xtu.plugin.flutter.store.asset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AssetStorageEntity {

    public final List<String> assetList = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetStorageEntity that = (AssetStorageEntity) o;
        return Objects.equals(assetList, that.assetList);
    }

    @Override
    public String toString() {
        return "AssetStorageEntity{" +
                "assetList=" + assetList +
                '}';
    }
}
