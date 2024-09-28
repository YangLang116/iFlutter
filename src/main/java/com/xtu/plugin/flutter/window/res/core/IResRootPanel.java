package com.xtu.plugin.flutter.window.res.core;

import com.intellij.ide.ui.UISettings;
import com.xtu.plugin.flutter.window.res.sort.SortType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.List;

public interface IResRootPanel {

    void loadResList(@NotNull List<File> resList);

    void reloadItems(@NotNull List<File> changeFileList);

    void cleanResList();

    void locateRes(@NotNull String resName);

    void searchRes();

    void compressRes();

    void sortRes(@NotNull SortType sort);

    JComponent asComponent();

    void uiSettingsChanged(@NotNull UISettings uiSettings);
}