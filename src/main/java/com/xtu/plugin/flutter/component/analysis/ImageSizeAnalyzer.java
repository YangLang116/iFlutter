package com.xtu.plugin.flutter.component.analysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.ImageUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImageSizeAnalyzer {

    private final Project project;

    public ImageSizeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    public void onPsiFileAdd(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (!AssetUtils.isAssetFile(project, virtualFile)) return;
        if (!ImageUtils.isImageFile(virtualFile)) return;
        analysisImageSize(virtualFile);
    }

    private void analysisImageSize(@NotNull VirtualFile imageVirtualFile) {
        final String filePath = imageVirtualFile.getPath();
        StorageService storageService = StorageService.getInstance(project);
        StorageEntity storageEntity = storageService.getState();
        final int maxPicSize = storageEntity.maxPicSize;
        final int maxPicWidth = storageEntity.maxPicWidth;
        final int maxPicHeight = storageEntity.maxPicHeight;
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final File imageFile = new File(filePath);
            int imageSize = ImageUtils.getImageSize(imageFile);
            if (imageSize > maxPicSize) {
                showWarnDialog(String.format("%s 大小超标:\n标准: %d k\n当前: %d k", imageFile.getName(), maxPicSize, imageSize));
                return;
            }
            ImageUtils.PicDimension imageDimension = ImageUtils.getImageDimension(imageFile);
            if (imageDimension != null && (imageDimension.width > maxPicWidth || imageDimension.height > maxPicHeight)) {
                showWarnDialog(String.format("%s 尺寸超标:\n标准: %d x %d\n当前: %d x %d",
                        imageFile.getName(),
                        maxPicWidth, maxPicHeight,
                        imageDimension.width, imageDimension.height));
            }
        });
    }

    private void showWarnDialog(@NotNull String tip) {
        ApplicationManager.getApplication().invokeAndWait(() ->
                Messages.showWarningDialog(project, tip, "新增不规范的图片"));
    }
}
