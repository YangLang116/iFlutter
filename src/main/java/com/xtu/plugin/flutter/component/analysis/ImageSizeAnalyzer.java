package com.xtu.plugin.flutter.component.analysis;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.ImageUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ImageSizeAnalyzer {

    private final Project project;

    public ImageSizeAnalyzer(@NotNull Project project) {
        this.project = project;
    }

    public void onPsiFileAdd(PsiFile psiFile) {
        if (!AssetUtils.isAssetFile(psiFile)) return;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;
        if (!ImageUtils.isImageFile(virtualFile)) return;
        analysisImageSize(virtualFile);
    }

    private void analysisImageSize(@NotNull VirtualFile imageVirtualFile) {
        final String filePath = imageVirtualFile.getPath();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final File imageFile = new File(filePath);
            int imageSize = ImageUtils.getImageSize(imageFile);
            if (imageSize > 100) {
                showWarnDialog(String.format("%s 大小超标:\n标准: %d k\n当前: %d k", imageFile.getName(), 100, imageSize));
                return;
            }
            ImageUtils.PicDimension imageDimension = ImageUtils.getImageDimension(imageFile);
            if (imageDimension != null && (imageDimension.width > 100 || imageDimension.height > 100)) {
                showWarnDialog(String.format("%s 尺寸超标:\n标准: %d x %d\n当前: %d x %d",
                        imageFile.getName(),
                        100, 100,
                        imageDimension.width, imageDimension.height));
            }
        });
    }

    private void showWarnDialog(@NotNull String tip) {
        ApplicationManager.getApplication().invokeAndWait(() ->
                Messages.showWarningDialog(project, tip, "新增不规范的图片"));
    }
}
