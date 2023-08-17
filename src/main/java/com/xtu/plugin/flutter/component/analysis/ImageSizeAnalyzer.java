package com.xtu.plugin.flutter.component.analysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.component.analysis.utils.ImageAnalysisUtils;
import com.xtu.plugin.flutter.store.StorageEntity;
import com.xtu.plugin.flutter.store.StorageService;
import com.xtu.plugin.flutter.utils.AssetUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImageSizeAnalyzer {

    private final Project project;

    public ImageSizeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    public void onPsiFileAdd(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (!AssetUtils.isAssetFile(project, virtualFile)) return;
        if (!ImageAnalysisUtils.isImageFile(virtualFile)) return;
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
            int imageSize = ImageAnalysisUtils.getImageSize(imageFile);
            if (imageSize > maxPicSize) {
                showWarnDialog(String.format("%s Size limit:\n\nmax: %d k\ncurrent: %d k",
                        imageFile.getName(), maxPicSize, imageSize));
                return;
            }
            ImageAnalysisUtils.PicDimension imageDimension = ImageAnalysisUtils.getImageDimension(imageFile);
            if (imageDimension != null &&
                    (imageDimension.width > maxPicWidth || imageDimension.height > maxPicHeight)) {
                showWarnDialog(String.format("%s Dimension limit:\n\nmax: %d x %d\ncurrent: %d x %d",
                        imageFile.getName(),
                        maxPicWidth, maxPicHeight,
                        imageDimension.width, imageDimension.height));
            }
        });
    }

    private void showWarnDialog(@NotNull String tip) {
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showWarningDialog(project, tip, "Add non-standard images"));
    }
}
