package com.xtu.plugin.flutter.upgrader;

public class VersionInfo {

    public String version;
    public String title;
    public String subtitle;
    public String content;

    @Override
    public String toString() {
        return "VersionInfo{" +
                "version='" + version + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
