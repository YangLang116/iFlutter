package com.xtu.plugin.flutter.window.res.ui;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public interface ResManagerListener {

    void reloadResList(@NotNull List<File> changeFileList);

    void deleteList(@NotNull List<File> deleteList);
}
