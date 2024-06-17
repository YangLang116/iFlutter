package com.xtu.plugin.flutter.store;

import com.intellij.util.xmlb.annotations.Transient;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidRepoHelper;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StorageEntity {

    //资源变化检测目录，用于实时生成R文件
    public List<String> resDir = Arrays.asList("assets", "images");
    //根据资源文件后缀，屏蔽R中某些字段的生成
    public List<String> ignoreResExtension = Collections.emptyList();
    //是否打开flutter2.0
    public boolean flutter2Enable = true;
    //是否打开资源变更检查
    public boolean resCheckEnable = true;
    //HTTP Mock 配置
    public List<HttpEntity> httpEntityList = new ArrayList<>();
    //资源是否以目录形式注册
    public boolean foldRegisterEnable = false;
    //最新版本信息
    @Transient
    public Map<String, PackageInfo> packageInfoMap = new HashMap<>();
    //翻译api key
    public String apiKey = "20220420001182709";
    //翻译api secret
    public String apiSecret = "Jq7NWiluWPYwKoILFQ1V";
    //最大图片
    public int maxPicSize = 500;
    //最大图片宽高
    public int maxPicWidth = 1280;
    public int maxPicHeight = 2400;
    public boolean enableSizeMonitor = true;
    //仓库镜像地址
    public String mirrorRepoStr = AndroidRepoHelper.getDefaultRepoStr();
    //是否带包名生成资源文件
    public boolean registerResWithPackage = false;
    //是否生成数据不可修改的fromJson
    public boolean isUnModifiableFromJson = false;
    //tiny api key
    public boolean autoTinyImage = true;
    public String tinyApiKey = "";

    public StorageEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageEntity that = (StorageEntity) o;
        return flutter2Enable == that.flutter2Enable
                && resCheckEnable == that.resCheckEnable
                && Objects.equals(resDir, that.resDir)
                && Objects.equals(ignoreResExtension, that.ignoreResExtension)
                && Objects.equals(httpEntityList, that.httpEntityList)
                && foldRegisterEnable == that.foldRegisterEnable
                && Objects.equals(apiKey, that.apiKey)
                && Objects.equals(apiSecret, that.apiSecret)
                && enableSizeMonitor == that.enableSizeMonitor
                && maxPicSize == that.maxPicSize
                && maxPicWidth == that.maxPicWidth
                && registerResWithPackage == that.registerResWithPackage
                && maxPicHeight == that.maxPicHeight
                && isUnModifiableFromJson == that.isUnModifiableFromJson
                && Objects.equals(mirrorRepoStr, that.mirrorRepoStr)
                && Objects.equals(tinyApiKey, that.tinyApiKey)
                && autoTinyImage == that.autoTinyImage;
    }

    @Override
    public String toString() {
        return "StorageEntity{" +
                "resDir=" + resDir +
                ", ignoreResExtension=" + ignoreResExtension +
                ", flutter2Enable=" + flutter2Enable +
                ", resCheckEnable=" + resCheckEnable +
                ", httpEntityList=" + httpEntityList +
                ", foldRegisterEnable=" + foldRegisterEnable +
                ", packageInfoMap=" + packageInfoMap +
                ", apiKey=" + apiKey +
                ", apiSecret=" + apiSecret +
                ", enableSizeMonitor=" + enableSizeMonitor +
                ", maxPicSize=" + maxPicSize +
                ", maxPicWidth=" + maxPicWidth +
                ", maxPicHeight=" + maxPicHeight +
                ", mirrorRepoStr=" + mirrorRepoStr +
                ", registerResWithPackage=" + registerResWithPackage +
                ", isUnModifiableFromJson=" + isUnModifiableFromJson +
                ", tinyApiKey=" + tinyApiKey +
                ", autoTinyImage=" + autoTinyImage +
                '}';
    }
}
