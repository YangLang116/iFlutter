package com.xtu.plugin.flutter.window.res.sort;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.dialog.ComboBoxDialog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;

public class SortHelper {
    private static final String TAG = "SortHelper";
    private static final String sTitle = "Select Sort Type";
    private static final List<String> sortNameList = Arrays.asList(
            "AZ",
            "File Size",
            "Update Time");
    private static final List<SortType> sortTypeList = Arrays.asList(
            SortType.SORT_AZ,
            SortType.SORT_SIZE,
            SortType.SORT_TIME);

    public static void sort(@NotNull List<File> fileList, @NotNull SortType sortType) {
        Comparator<File> comparator = switch (sortType) {
            case SORT_AZ -> Comparator.comparing(File::getName);
            case SORT_SIZE -> Comparator.comparingLong(File::length).reversed();
            default -> Comparator.comparingLong(File::lastModified).reversed();
        };
        fileList.sort(comparator);
    }

    @Nullable
    public static SortType selectSortType(@NotNull Project project) {
        ComboBoxDialog selectDialog = new ComboBoxDialog(project, sTitle, sortNameList, TAG);
        boolean isOK = selectDialog.showAndGet();
        if (!isOK) return null;
        int selectIndex = selectDialog.getSelectIndex();
        return sortTypeList.get(selectIndex);
    }

    public static Icon getSortIcon(@NotNull SortType sortType) {
        return switch (sortType) {
            case SORT_AZ -> AllIcons.ObjectBrowser.Sorted;
            case SORT_SIZE -> AllIcons.ObjectBrowser.SortedByUsage;
            default -> AllIcons.ObjectBrowser.SortByType;
        };
    }

}
