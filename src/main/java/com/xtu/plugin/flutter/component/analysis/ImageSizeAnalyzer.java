package com.xtu.plugin.flutter.component.analysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.entity.ImageSize;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.ImageUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Locale;

public class ImageSizeAnalyzer {

    private final Project project;

    public ImageSizeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    public void analysis(@NotNull VirtualFile virtualFile) {
        if (!AssetUtils.isAssetFile(project, virtualFile)) return;
        if (!canAnalysis(virtualFile)) return;
        doAnalysis(virtualFile);
    }

    private boolean canAnalysis(@NotNull VirtualFile file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    private void doAnalysis(@NotNull VirtualFile file) {
        final String filePath = file.getPath();
        final ProjectStorageEntity storageEntity = ProjectStorageService.getStorage(project);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final File imageFile = new File(filePath);
            String sizeTip = getIllegalSizeTip(imageFile, storageEntity.maxPicSize);
            String dimensionTip = getIllegalDimensionTip(imageFile, storageEntity.maxPicWidth, storageEntity.maxPicHeight);
            if (sizeTip == null && dimensionTip == null) return;
            showWarnDialog(imageFile, sizeTip, dimensionTip);
        });
    }

    private String getIllegalSizeTip(@NotNull File imageFile, int maxPicSize) {
        int imageSize = ImageUtils.getImageSize(imageFile);
        if (imageSize < maxPicSize) return null;
        return String.format("Size limit: %dk, current: %dk", maxPicSize, imageSize);
    }

    private String getIllegalDimensionTip(@NotNull File imageFile, int maxPicWidth, int maxPicHeight) {
        ImageSize size = ImageUtils.getImageDimension(imageFile);
        if (size == null) return null;
        int cWidth = size.width;
        int cHeight = size.height;
        if (cWidth < maxPicWidth && cHeight < maxPicHeight) return null;
        return String.format("Dimension limit: %dx%d, current: %dx%d", maxPicWidth, maxPicHeight, cWidth, cHeight);
    }

    private void showWarnDialog(@NotNull File imageFile, @Nullable String sizeTip, @Nullable String dimensionTip) {
        final StringBuilder tipBuilder = new StringBuilder()
                .append("FileName: ").append(imageFile.getName()).append("\n\n");
        if (sizeTip != null) {
            tipBuilder.append(sizeTip).append("\n");
        }
        if (dimensionTip != null) {
            tipBuilder.append(dimensionTip).append("\n");
        }
        final String tip = tipBuilder.toString();
        ApplicationManager.getApplication().invokeLater(() ->
                Messages.showWarningDialog(project, tip, "Add Non-Standard Images"));
    }
}
