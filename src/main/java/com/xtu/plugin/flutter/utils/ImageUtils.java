package com.xtu.plugin.flutter.utils;

import com.intellij.util.ui.ImageUtil;
import org.apache.commons.lang.StringUtils;
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
import java.util.Locale;

public class ImageUtils {

    @SuppressWarnings("SpellCheckingInspection")
    private static boolean isTwelveMonkeysRead(@NotNull ImageReader reader) {
        ImageReaderSpi imageReaderSpi = reader.getOriginatingProvider();
        if (imageReaderSpi == null) return false;
        Class<? extends ImageReaderSpi> pClass = imageReaderSpi.getClass();
        if (pClass == null) return false;
        String packageName = pClass.getPackageName();
        return packageName.startsWith("com.twelvemonkeys.imageio");
    }

    @Nullable
    private static ImageReader getImageReader(@NotNull File imageFile) {
        if (!imageFile.canRead()) return null;
        String extension = FileUtils.getExtension(imageFile);
        if (StringUtils.isEmpty(extension)) return null;
        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(extension.toLowerCase(Locale.ROOT));
        if (!iterator.hasNext()) return null;
        final List<ImageReader> readerList = new ArrayList<>();
        iterator.forEachRemaining(readerList::add);
        for (ImageReader imageReader : readerList) {
            if (isTwelveMonkeysRead(imageReader)) return imageReader;
        }
        return readerList.get(0);
    }

    @Nullable
    public static Image loadThumbnail(@NotNull File imageFile, int maxWidth, int maxHeight) {
        ImageReader imageReader = getImageReader(imageFile);
        if (imageReader == null) return null;
        ImageInputStream imageInputStream = null;
        BufferedImage recycleImage = null;
        try {
            imageInputStream = ImageIO.createImageInputStream(imageFile);
            imageReader.setInput(imageInputStream);
            BufferedImage originImage = imageReader.read(0);
            int imageWidth = originImage.getWidth();
            int imageHeight = originImage.getHeight();
            if (imageWidth > maxWidth || imageHeight > maxHeight) {
                float widthScaleRadio = imageWidth * 1.0f / maxWidth;
                float heightScaleRadio = imageHeight * 1.0f / maxHeight;
                float radio = Math.max(widthScaleRadio, heightScaleRadio);
                int displayWidth = (int) (imageWidth / radio);
                int displayHeight = (int) (imageHeight / radio);
                recycleImage = originImage;
                return ImageUtil.scaleImage(originImage, displayWidth, displayHeight);
            } else {
                return originImage;
            }
        } catch (Exception e) {
            LogUtils.error("ImageUtils loadThumbnail: " + e.getMessage());
            return null;
        } finally {
            CloseUtils.close(imageInputStream);
            imageReader.dispose();
            if (recycleImage != null) recycleImage.flush();
        }
    }
}