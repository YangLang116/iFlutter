package com.xtu.plugin.flutter.base.component;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.ui.scale.JBUIScale;
import com.xtu.plugin.flutter.base.entity.ImageInfo;
import com.xtu.plugin.flutter.base.entity.ImageSize;
import com.xtu.plugin.flutter.base.utils.ImageUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class ImageComponent extends JLabel {

    @Nullable
    private Image image;

    public ImageComponent(@NotNull Dimension size) {
        setPreferredSize(size);
        setBorder(new RoundedLineBorder(JBColor.border(), JBUIScale.scale(4), JBUIScale.scale(2)));
    }

    public void loadImage(@NotNull File file, @NotNull ImageSize size, @NotNull OnLoadImageListener listener) {
        Application application = ApplicationManager.getApplication();
        application.executeOnPooledThread(() -> {
            final ImageInfo info = ImageUtils.loadImageInfo(file, "/icons/load_fail.png", size);
            flushImage(info, listener);
        });
    }

    private void flushImage(@NotNull ImageInfo info, @NotNull OnLoadImageListener listener) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            listener.loadFinish(info.size);
            image = info.image;
            revalidate();
            repaint();
        }, ModalityState.any());
    }

    @Override
    public void paint(Graphics g) {
        if (image == null) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);
        int startX = (getWidth() - imgWidth) / 2;
        int startY = (getHeight() - imgHeight) / 2;
        g2d.drawImage(image, startX, startY, startX + imgWidth, startY + imgHeight, 0, 0, imgWidth, imgHeight, null);
    }

    public void dispose() {
        if (image == null) return;
        image.flush();
        image = null;
    }

    public interface OnLoadImageListener {
        void loadFinish(@NotNull ImageSize size);
    }
}
