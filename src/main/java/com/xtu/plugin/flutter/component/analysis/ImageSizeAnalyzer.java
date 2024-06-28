package com.xtu.plugin.flutter.component.analysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.component.analysis.utils.ImageAnalysisUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import com.xtu.plugin.flutter.utils.AssetUtils;
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
        if (!ImageAnalysisUtils.canAnalysis(virtualFile)) return;
        doAnalysis(virtualFile);
    }

    private void doAnalysis(@NotNull VirtualFile file) {
        final String filePath = file.getPath();
        ProjectStorageService storageService = ProjectStorageService.getInstance(project);
        final ProjectStorageEntity storageEntity = storageService.getState();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final File imageFile = new File(filePath);
            String sizeTip = getIllegalSizeTip(imageFile, storageEntity.maxPicSize);
            String dimensionTip = getIllegalDimensionTip(imageFile, storageEntity.maxPicWidth, storageEntity.maxPicHeight);
            if (sizeTip == null && dimensionTip == null) return;
            showWarnDialog(imageFile, sizeTip, dimensionTip);
        });
    }

    private String getIllegalSizeTip(@NotNull File imageFile, int maxPicSize) {
        int imageSize = ImageAnalysisUtils.getImageSize(imageFile);
        if (imageSize < maxPicSize) return null;
        return String.format(Locale.ROOT, "Size limit: %dk，current: %dk", maxPicSize, imageSize);
    }

    private String getIllegalDimensionTip(@NotNull File imageFile, int maxPicWidth, int maxPicHeight) {
        ImageAnalysisUtils.PicDimension dimension = ImageAnalysisUtils.getImageDimension(imageFile);
        if (dimension == null) return null;
        int cWidth = dimension.width;
        int cHeight = dimension.height;
        if (cWidth < maxPicWidth && cHeight < maxPicHeight) return null;
        return String.format("Dimension limit: %dx%d，current: %dx%d", maxPicWidth, maxPicHeight, cWidth, cHeight);
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
                Messages.showWarningDialog(project, tip, "Add non-standard images"));
    }
}
