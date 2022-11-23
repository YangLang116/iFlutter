package com.xtu.plugin.flutter.utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PathUtils {

    //兼容windows
    public static boolean isChildPath(@NotNull String parentPath, @NotNull String childPath) {
        File parentFile = new File(parentPath);
        String parentAbsPath = parentFile.getAbsolutePath();
        File childFile = new File(childPath);
        String childAbsPath = childFile.getAbsolutePath();
        return childAbsPath.startsWith(parentAbsPath);
    }
}
