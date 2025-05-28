package com.xtu.plugin.flutter.base.utils;

import com.xtu.plugin.flutter.base.entity.ImageInfo;
import com.xtu.plugin.flutter.base.entity.ImageSize;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private static <T> Image loadThumbnail(@NotNull Thumbnails.Builder<T> builder, @NotNull ImageSize size) {
        try {
            return builder.size(size.width, size.height).imageType(BufferedImage.TYPE_INT_ARGB).outputQuality(1.0).asBufferedImage();
        } catch (Exception e) {
            LogUtils.error("ImageUtils loadThumbnail", e);
            return null;
        }
    }

    @Nullable
    private static Image loadThumbnail(@NotNull String defaultUrl, @NotNull ImageSize size) {
        return loadThumbnail(Thumbnails.of(ImageUtils.class.getResource(defaultUrl)), size);
    }

    @Nullable
    private static Image loadThumbnail(@NotNull File file, @NotNull ImageSize size) {
        return loadThumbnail(Thumbnails.of(file), size);
    }

    @NotNull
    public static ImageInfo loadImageInfo(@NotNull File file, @NotNull String defaultUrl, @NotNull ImageSize size) {
        ImageSize imageSize = getImageDimension(file);
        if (imageSize == null) {
            return new ImageInfo(loadThumbnail(defaultUrl, size), new ImageSize(0, 0));
        } else {
            ImageSize fitSize = calcFitDimension(imageSize, size);
            return new ImageInfo(loadThumbnail(file, fitSize), imageSize);
        }
    }

    @NotNull
    private static ImageSize calcFitDimension(@NotNull ImageSize size, @NotNull ImageSize maxSize) {
        int renderWidth;
        int renderHeight;
        if (size.width > maxSize.width || size.height > maxSize.height) {
            float widthRadio = size.width * 1.0f / maxSize.width;
            float heightRadio = size.height * 1.0f / maxSize.height;
            float radio = Math.max(widthRadio, heightRadio);
            renderWidth = Math.round(size.width / radio);
            renderHeight = Math.round(size.height / radio);
        } else if (size.width > size.height) {
            renderWidth = maxSize.width;
            renderHeight = Math.round(maxSize.width / (size.width * 1.0f / size.height));
        } else {
            renderWidth = Math.round(maxSize.height * (size.width * 1.0f / size.height));
            renderHeight = maxSize.height;
        }
        return new ImageSize(renderWidth, renderHeight);
    }

    //单位:K
    public static int getImageSize(@NotNull File file) {
        return (int) (file.length() / 1024);
    }

    @Nullable
    public static ImageSize getImageDimension(@NotNull File file) {
        ImageReader imageReader = null;
        ImageInputStream imageInputStream = null;
        try {
            imageReader = getImageReader(file);
            if (imageReader == null) return null;
            imageInputStream = ImageIO.createImageInputStream(file);
            if (imageInputStream == null) return null;
            imageReader.setInput(imageInputStream);
            int width = imageReader.getWidth(0);
            int height = imageReader.getHeight(0);
            return new ImageSize(width, height);
        } catch (Exception e) {
            LogUtils.error("ImageUtils getImageDimension", e);
            return null;
        } finally {
            CloseUtils.close(imageInputStream);
            CloseUtils.close(imageReader);
        }
    }
}