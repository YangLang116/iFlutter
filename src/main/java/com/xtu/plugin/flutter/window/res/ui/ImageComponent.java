package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.application.Application;
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


public class ImageComponent extends JPanel {

    private Image displayImage;
    private final OnLoadImageListener loadImageListener;

    public ImageComponent(@NotNull File assetImage, int size, int imageSize, @NotNull OnLoadImageListener listener) {
        this.loadImageListener = listener;
        setPreferredSize(new Dimension(size, size));
        setBorder(new RoundedLineBorder(JBColor.border(), JBUIScale.scale(4), JBUIScale.scale(2)));
        loadImage(assetImage, imageSize);
    }

    private void loadImage(@NotNull File assetImage, int imageSize) {
        final Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final ImageUtils.ImageInfo imageInfo = ImageUtils.loadThumbnail(assetImage, imageSize, imageSize);
            if (imageInfo == null) {
                application.invokeLater(() -> {
                    setLayout(new BorderLayout());
                    JLabel iconLabel = new JLabel(ImageUtils.loadImageIcon(PluginIcons.FAIL_PIC, imageSize));
                    add(iconLabel, BorderLayout.CENTER);
                });
            } else {
                application.invokeLater(() -> {
                    this.displayImage = imageInfo.image;
                    repaint();
                    this.loadImageListener.onSuccess(new Dimension(imageInfo.width, imageInfo.height));
                });
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (displayImage == null) return;
        int imageWidth = displayImage.getWidth(null);
        int imageHeight = displayImage.getHeight(null);
        int startX = (getWidth() - imageWidth) / 2;
        int startY = (getHeight() - imageHeight) / 2;
        g.drawImage(displayImage, startX, startY, imageWidth, imageHeight, null);
    }

    public void dispose() {
        if (this.displayImage != null) {
            this.displayImage.flush();
            this.displayImage = null;
        }
    }

    interface OnLoadImageListener {

        void onSuccess(Dimension dimension);
    }
}
