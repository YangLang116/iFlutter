package com.xtu.plugin.flutter.store.ide.entity;

import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidRepoHelper;

import java.util.Objects;

public class IdeStorageEntity {

    //翻译api key
    public String apiKey = "20220420001182709";
    //翻译api secret
    public String apiSecret = "Jq7NWiluWPYwKoILFQ1V";
    //TinyPng api key
    public String tinyApiKey = "";
    //仓库镜像地址
    public String mirrorRepoStr = AndroidRepoHelper.getDefaultRepoStr();

    public IdeStorageEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdeStorageEntity that = (IdeStorageEntity) o;
        return Objects.equals(apiKey, that.apiKey)
                && Objects.equals(apiSecret, that.apiSecret)
                && Objects.equals(tinyApiKey, that.tinyApiKey)
                && Objects.equals(mirrorRepoStr, that.mirrorRepoStr);
    }

    @Override
    public String toString() {
        return "IdeStorageEntity{" +
                "apiKey='" + apiKey + '\'' +
                ", apiSecret='" + apiSecret + '\'' +
                ", tinyApiKey='" + tinyApiKey + '\'' +
                ", mirrorRepoStr='" + mirrorRepoStr + '\'' +
                '}';
    }
}
