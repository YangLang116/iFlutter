package com.xtu.plugin.flutter.window.res.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.adapter.BulkFileAdapter;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public abstract class ResFileChangedAdapter extends BulkFileAdapter {

    private final List<String> supportExtends;

    public ResFileChangedAdapter(@NotNull Project project, @NotNull List<String> supportExtends) {
        super(project);
        this.supportExtends = supportExtends;
    }

    @Override
    public void onFileAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
        scanResListIfNeed(project, addFile);
    }

    @Override
    public void onFileMove(@NotNull Project project, @NotNull File oldFile, @NotNull VirtualFile newFile) {
        String oldAssetPath = AssetUtils.getAssetFilePath(project, oldFile);
        boolean isOldAssetPic = CollectionUtils.endsWith(oldAssetPath, supportExtends);
        String newAssetPath = AssetUtils.getAssetFilePath(project, newFile);
        boolean isNewAssetPic = CollectionUtils.endsWith(newAssetPath, supportExtends);
        if (!isOldAssetPic && isNewAssetPic) scanResList(); //add
        if (isOldAssetPic && !isNewAssetPic) scanResList(); //delete
    }

    @Override
    public void onFileDeleted(@NotNull Project project, @NotNull VirtualFile deleteFile) {
        scanResListIfNeed(project, deleteFile);
    }

    @Override
    public void onFileNameChanged(@NotNull Project project, @NotNull VirtualFile file,
                                  @NotNull String oldName, @NotNull String newName) {
        scanResListIfNeed(project, file);
    }

    private void scanResListIfNeed(@NotNull Project project, @NotNull VirtualFile file) {
        String assetPath = AssetUtils.getAssetFilePath(project, file);
        if (CollectionUtils.endsWith(assetPath, supportExtends)) scanResList();
    }

    abstract void scanResList();
}
