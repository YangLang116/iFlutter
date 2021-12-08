package com.xtu.plugin.flutter.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StorageEntity {

    //资源变化检测目录，用于实时生成R文件
    public List<String> resDir = Arrays.asList("assets", "images");
    //图片优化检测目录，用于根据图片名进行目录分类
    public List<String> imageDir = Collections.singletonList("images");

    public StorageEntity() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorageEntity that = (StorageEntity) o;
        return resDir.equals(that.resDir) && imageDir.equals(that.imageDir);
    }

    @Override
    public String toString() {
        return "StorageEntity{" +
                "resDir=" + resDir +
                ", imageDir=" + imageDir +
                '}';
    }
}
