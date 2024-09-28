package com.xtu.plugin.flutter.base.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
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
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(extension.toLowerCase());
        if (!iterator.hasNext()) return null;
        final List<ImageReader> readerList = new ArrayList<>();
        iterator.forEachRemaining(readerList::add);
        for (ImageReader imageReader : readerList) {
            if (isTwelveMonkeysRead(imageReader)) return imageReader;
        }
        return readerList.get(0);
    }

    @Nullable
    private static BufferedImage loadDefaultIcon(@NotNull String defaultUrl, int scaleSize) {
        URL defaultIcon = ImageUtils.class.getResource(defaultUrl);
        try {
            return Thumbnails.of(defaultIcon)
                    .size(scaleSize, scaleSize)
                    .imageType(BufferedImage.TYPE_INT_ARGB)
                    .asBufferedImage();
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static ImageInfo loadThumbnail(@NotNull File imageFile, @NotNull String defaultUrl, int scaleSize) {
        ImageReader imageReader = getImageReader(imageFile);
        if (imageReader == null) {
            BufferedImage defaultIcon = loadDefaultIcon(defaultUrl, scaleSize);
            return new ImageInfo(defaultIcon, 0, 0);
        }
        int originWidth = 0;
        int originHeight = 0;
        ImageInputStream imageStream = null;
        try {
            imageStream = ImageIO.createImageInputStream(imageFile);
            imageReader.setInput(imageStream);
            originWidth = imageReader.getWidth(0);
            originHeight = imageReader.getHeight(0);
            Dimension renderSize = getImageRenderSize(originWidth, originHeight, scaleSize);
            ImageReadParam readParam = imageReader.getDefaultReadParam();
            if (readParam.canSetSourceRenderSize()) {
                readParam.setSourceRenderSize(renderSize);
                BufferedImage image = imageReader.read(0, readParam);
                return new ImageInfo(image, originWidth, originHeight);
            } else {
                BufferedImage originImage = imageReader.read(0);
                Image scaleImage = getScaleImage(originImage, renderSize.width, renderSize.height);
                return new ImageInfo(scaleImage, originWidth, originHeight);
            }
        } catch (Exception e) {
            BufferedImage defaultIcon = loadDefaultIcon(defaultUrl, scaleSize);
            return new ImageInfo(defaultIcon, originWidth, originHeight);
        } finally {
            CloseUtils.close(imageStream);
            imageReader.dispose();
        }
    }

    @NotNull
    private static Dimension getImageRenderSize(int originWidth, int originHeight, int size) {
        int renderWidth;
        int renderHeight;
        if (originWidth > size || originHeight > size) {
            float widthRadio = originWidth * 1.0f / size;
            float heightRadio = originHeight * 1.0f / size;
            float radio = Math.max(widthRadio, heightRadio);
            renderWidth = Math.round(originWidth / radio);
            renderHeight = Math.round(originHeight / radio);
        } else if (originWidth > originHeight) {
            renderWidth = size;
            renderHeight = Math.round(size / (originWidth * 1.0f / originHeight));
        } else {
            renderWidth = Math.round(size * (originWidth * 1.0f / originHeight));
            renderHeight = size;
        }
        return new Dimension(renderWidth, renderHeight);
    }

    @Nullable
    private static Image getScaleImage(@Nullable BufferedImage originImage, int width, int height) {
        if (originImage == null) return null;
        return originImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    public static class ImageInfo {

        @Nullable
        public final Image image;
        public final int width;
        public final int height;

        public ImageInfo(@Nullable Image image, int width, int height) {
            this.image = image;
            this.width = width;
            this.height = height;
        }
    }
}