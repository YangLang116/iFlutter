package com.xtu.plugin.flutter.base.component;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.scale.JBUIScale;
import com.xtu.plugin.flutter.base.utils.ImageUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class ImageComponent extends JLabel {

    private ImageUtils.ImageInfo imageInfo;

    public ImageComponent(int size) {
        setPreferredSize(new Dimension(size, size));
        setBorder(new RoundedLineBorder(JBColor.border(), JBUIScale.scale(4), JBUIScale.scale(2)));
    }

    public void loadImage(@NotNull File assetImage, int size, @NotNull OnLoadImageListener listener) {
        Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final ImageUtils.ImageInfo info = ImageUtils.loadThumbnail(assetImage, "/icons/load_fail.png", size);
            flushImage(info, listener);
        });
    }

    private void flushImage(@NotNull ImageUtils.ImageInfo info, @NotNull OnLoadImageListener listener) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            listener.loadFinish(info.width, info.height);
            imageInfo = info;
            revalidate();
            repaint();
        }, ModalityState.any());
    }

    @Override
    public void paint(Graphics g) {
        if (imageInfo == null || imageInfo.image == null) return;
        int imgWidth = imageInfo.image.getWidth(null);
        int imgHeight = imageInfo.image.getHeight(null);
        int startX = (getWidth() - imgWidth) / 2;
        int startY = (getHeight() - imgHeight) / 2;
        g.drawImage(imageInfo.image, startX, startY, startX + imgWidth, startY + imgHeight, 0, 0, imgWidth, imgHeight, null);
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
