package com.xtu.plugin.flutter.utils;

import com.intellij.util.ui.ImageUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImageUtils {

    private static boolean isTwelveMonkeysRead(@NotNull ImageReader reader) {
        ImageReaderSpi imageReaderSpi = reader.getOriginatingProvider();
        if (imageReaderSpi == null) return false;
        Class<? extends ImageReaderSpi> pClass = imageReaderSpi.getClass();
        String packageName = pClass.getPackageName();
        return packageName.startsWith("com.twelvemonkeys.imageio");
    }

    @Nullable
    private static ImageReader getImageReader(@NotNull File imageFile) {
        if (!imageFile.canRead()) return null;
        String extension = FileUtils.getExtension(imageFile);
        if (StringUtils.isEmpty(extension)) return null;
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(extension);
        if (!iterator.hasNext()) return null;
        final List<ImageReader> readerList = new ArrayList<>();
        iterator.forEachRemaining(readerList::add);
        for (ImageReader imageReader : readerList) {
            if (isTwelveMonkeysRead(imageReader)) return imageReader;
        }
        return readerList.get(0);
    }

    private static ImageInfo loadDefaultIcon(@NotNull URL url, int imageSize) {
        ImageIcon icon = new ImageIcon(url);
        Image image = ImageUtil.scaleImage(icon.getImage(), imageSize, imageSize);
        return new ImageInfo(image, 0, 0, imageSize, imageSize);
    }

    @NotNull
    public static ImageInfo loadThumbnail(@NotNull File imageFile, @NotNull URL defaultUrl, int size) {
        ImageReader imageReader = getImageReader(imageFile);
        if (imageReader == null) return loadDefaultIcon(defaultUrl, size);
        ImageInputStream imageInputStream = null;
        BufferedImage recycleImage = null;
        try {
            imageInputStream = ImageIO.createImageInputStream(imageFile);
            imageReader.setInput(imageInputStream);
            BufferedImage originImage = imageReader.read(0);
            int imageWidth = originImage.getWidth();
            int imageHeight = originImage.getHeight();
            if (imageWidth > size || imageHeight > size) {
                float widthScaleRadio = imageWidth * 1.0f / size;
                float heightScaleRadio = imageHeight * 1.0f / size;
                float radio = Math.max(widthScaleRadio, heightScaleRadio);
                int displayWidth = (int) (imageWidth / radio);
                int displayHeight = (int) (imageHeight / radio);
                recycleImage = originImage;
                Image scaleImage = ImageUtil.scaleImage(originImage, displayWidth, displayHeight);
                return new ImageInfo(scaleImage, imageWidth, imageHeight, displayWidth, displayHeight);
            } else {
                return new ImageInfo(originImage, imageWidth, imageHeight);
            }
        } catch (Exception e) {
            LogUtils.error("ImageUtils loadThumbnail", e);
            return loadDefaultIcon(defaultUrl, size);
        } finally {
            CloseUtils.close(imageInputStream);
            imageReader.dispose();
            if (recycleImage != null) recycleImage.flush();
        }
    }

    public static class ImageInfo {

        public final int width;
        public final int height;
        public final int dWidth;
        public final int dHeight;
        public final Image image;

        public ImageInfo(@NotNull Image image, int width, int height, int dWidth, int dHeight) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.dWidth = dWidth;
            this.dHeight = dHeight;
        }

        public ImageInfo(@NotNull Image image, int width, int height) {
            this.image = image;
            this.width = width;
            this.height = height;
            this.dWidth = width;
            this.dHeight = height;
        }
    }
}