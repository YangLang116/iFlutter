package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Locale;

public class ImageUtils {

    public static boolean isImageFile(@NotNull VirtualFile file) {
        String fileName = file.getName().toLowerCase(Locale.ROOT);
        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

    //单位:K
    public static int getImageSize(@NotNull File file) {
        return (int) (file.length() / 1024);
    }

    @Nullable
    public static PicDimension getImageDimension(@NotNull File file) {
        try {
            BufferedImage imageSize = ImageIO.read(file);
            return imageSize == null ? null : new PicDimension(imageSize.getWidth(), imageSize.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class PicDimension {
        public int width;
        public int height;

        PicDimension(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}