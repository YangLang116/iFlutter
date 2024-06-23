package com.xtu.plugin.flutter.window.res.sort;

import icons.PluginIcons;

import javax.swing.*;
import java.io.File;
import java.util.Comparator;

public enum SortType {
    //按照字母排序
    SORT_NAME("By File Name", PluginIcons.SORT_AZ, Comparator.comparing(File::getName)),
    //按照文件大小排序
    SORT_SIZE("By File Size", PluginIcons.SORT_SIZE, Comparator.comparingLong(File::length).reversed()),
    //按更新时间排序
    SORT_TIME("By Update Time", PluginIcons.SORT_TIME, Comparator.comparingLong(File::lastModified).reversed());

    public final String name;
    public final Icon icon;
    public final Comparator<File> comparator;

    SortType(String name, Icon icon, Comparator<File> comparator) {
        this.name = name;
        this.icon = icon;
        this.comparator = comparator;
    }
}
