package com.xtu.plugin.flutter.action.pub.speed.entity;

import java.io.File;

///插件信息
public class AndroidPluginInfo {
    public String name;
    public File androidDirectory;

    public AndroidPluginInfo(String name, File androidDirectory) {
        this.name = name;
        this.androidDirectory = androidDirectory;
    }
}