package com.xtu.plugin.flutter.window.res.core;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.adapter.AssetFileChangedObserver;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public abstract class ResToolWindowContentObserver extends AssetFileChangedObserver {

    private final List<String> supportImageExtends;

    public ResToolWindowContentObserver(@NotNull Project project, @NotNull List<String> supportImageExtends) {
        super(project);
        this.supportImageExtends = supportImageExtends;
    }

    @Override
    public void onAssetAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
        scanResListIfNeed(project, addFile);
    }

    @Override
    public void onAssetMove(@NotNull Project project, @NotNull File oldFile, @NotNull VirtualFile newFile) {
        String oldAssetPath = AssetUtils.getAssetFilePath(project, oldFile);
        boolean isOldAssetPic = CollectionUtils.endsWith(oldAssetPath, supportImageExtends);
        String newAssetPath = AssetUtils.getAssetFilePath(project, newFile);
        boolean isNewAssetPic = CollectionUtils.endsWith(newAssetPath, supportImageExtends);
        if (!isOldAssetPic && isNewAssetPic) scanResList(); //add
        if (isOldAssetPic && !isNewAssetPic) scanResList(); //delete
    }

    @Override
    public void onAssetDeleted(@NotNull Project project, @NotNull VirtualFile deleteFile) {
        scanResListIfNeed(project, deleteFile);
    }

    @Override
    public void onAssetContentChanged(@NotNull Project project, @NotNull VirtualFile file) {
        scanResListIfNeed(project, file);
    }

    @Override
    public void onAssetNameChanged(@NotNull Project project, @NotNull VirtualFile file,
                                   @NotNull String oldName, @NotNull String newName) {
        scanResListIfNeed(project, file);
    }

    private void scanResListIfNeed(@NotNull Project project, @NotNull VirtualFile file) {
        String assetPath = AssetUtils.getAssetFilePath(project, file);
        if (CollectionUtils.endsWith(assetPath, supportImageExtends)) {
            scanResList();
        }
    }

    @Override
    public void branchHasChanged(@NotNull String s) {
        super.branchHasChanged(s);
        scanResList();
    }

    abstract void scanResList();
}
