package com.xtu.plugin.flutter.base.component;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.scale.JBUIScale;
import com.xtu.plugin.flutter.utils.ImageUtils;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class ImageComponent extends JLabel {

    private final int paddingSize;
    private ImageUtils.ImageInfo imageInfo;

    public ImageComponent(int size, int paddingSize) {
        this.paddingSize = paddingSize;
        setPreferredSize(new Dimension(size, size));
        setBorder(new RoundedLineBorder(JBColor.border(), JBUIScale.scale(4), JBUIScale.scale(2)));
    }

    public void loadImage(@NotNull File assetImage, int size, @NotNull OnLoadImageListener listener) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final ImageUtils.ImageInfo info = ImageUtils.loadThumbnail(assetImage, PluginIcons.FAIL_PIC, size);
            flushImage(info, listener);
        });
    }

    private void flushImage(@NotNull ImageUtils.ImageInfo info, @NotNull OnLoadImageListener listener) {
        ApplicationManager.getApplication().invokeLater(() -> {
            listener.loadFinish(info.width, info.height);
            imageInfo = info;
            revalidate();
            repaint();
        });
    }

    @Override
    public void paint(Graphics g) {
        if (imageInfo == null) return;
        int imgWidth = imageInfo.dWidth;
        int imgHeight = imageInfo.dHeight;
        int areaWidth = getWidth() - 2 * paddingSize;
        int areaHeight = getHeight() - 2 * paddingSize;
        if (imgWidth > areaWidth || imgHeight > areaHeight) {
            double radioW = imgWidth * 1.0 / areaWidth;
            double radioH = imgHeight * 1.0 / areaHeight;
            double radio = Math.max(radioW, radioH);
            imgWidth = (int) Math.floor(imgWidth / radio);
            imgHeight = (int) Math.floor(imgHeight / radio);
        }
        int startX = paddingSize + (areaWidth - imgWidth) / 2;
        int startY = paddingSize + (areaHeight - imgHeight) / 2;
        int endX = startX + imgWidth;
        int endY = startY + imgHeight;
        g.drawImage(imageInfo.image,
                startX, startY, endX, endY,
                0, 0, imageInfo.dWidth, imageInfo.dHeight,
                null);
    }

    public void dispose() {
        if (this.imageInfo == null) return;
        this.imageInfo.image.flush();
        this.imageInfo = null;
    }

    public interface OnLoadImageListener {
        void loadFinish(int width, int height);
    }
}
