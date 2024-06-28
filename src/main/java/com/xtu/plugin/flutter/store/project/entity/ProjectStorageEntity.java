package com.xtu.plugin.flutter.store.project.entity;

import com.intellij.util.xmlb.annotations.Transient;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;

import java.util.*;

public class ProjectStorageEntity {

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
    //是否开启图片尺寸监听
    public boolean enableSizeMonitor = true;
    //最大图片
    public int maxPicSize = 500;
    //最大图片宽
    public int maxPicWidth = 1280;
    //最大图片高
    public int maxPicHeight = 2400;
    //是否带包名生成资源文件
    public boolean registerResWithPackage = false;
    //是否生成数据不可修改的fromJson
    public boolean isUnModifiableFromJson = false;
    //是否开启TinyPng压缩
    public boolean autoTinyImage = false;

    public ProjectStorageEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectStorageEntity that = (ProjectStorageEntity) o;
        return flutter2Enable == that.flutter2Enable
                && resCheckEnable == that.resCheckEnable
                && Objects.equals(resDir, that.resDir)
                && Objects.equals(ignoreResExtension, that.ignoreResExtension)
                && Objects.equals(httpEntityList, that.httpEntityList)
                && foldRegisterEnable == that.foldRegisterEnable
                && enableSizeMonitor == that.enableSizeMonitor
                && maxPicSize == that.maxPicSize
                && maxPicWidth == that.maxPicWidth
                && registerResWithPackage == that.registerResWithPackage
                && maxPicHeight == that.maxPicHeight
                && isUnModifiableFromJson == that.isUnModifiableFromJson
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
                ", enableSizeMonitor=" + enableSizeMonitor +
                ", maxPicSize=" + maxPicSize +
                ", maxPicWidth=" + maxPicWidth +
                ", maxPicHeight=" + maxPicHeight +
                ", registerResWithPackage=" + registerResWithPackage +
                ", isUnModifiableFromJson=" + isUnModifiableFromJson +
                ", autoTinyImage=" + autoTinyImage +
                '}';
    }
}
