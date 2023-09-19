package com.xtu.plugin.flutter.component.analysis.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.utils.CloseUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.Locale;

public class ImageAnalysisUtils {

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
        ImageInputStream imageInputStream = null;
        ImageReader imageReader = null;
        try {
            imageInputStream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) return null;
            imageReader = readers.next();
            imageReader.setInput(imageInputStream);
            int width = imageReader.getWidth(0);
            int height = imageReader.getHeight(0);
            return new PicDimension(width, height);
        } catch (Exception e) {
            LogUtils.error("ImageAnalysisUtils getImageDimension", e);
            return null;
        } finally {
            CloseUtils.close(imageInputStream);
            if (imageReader != null) imageReader.dispose();
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
