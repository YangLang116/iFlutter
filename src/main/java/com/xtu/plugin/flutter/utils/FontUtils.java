package com.xtu.plugin.flutter.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FontUtils {

    private static final List<String> FONT_EXTENSION_LIST = List.of("ttf", "font", "fon", "otf", "eot", "woff", "ttc");

    //通过后缀来简单区分资源类型
    public static boolean isFontAsset(@NotNull String assetName) {
        int lastDotIndex = assetName.lastIndexOf(".");
        if (lastDotIndex < 0) return false;
        String assetExtension = assetName.substring(lastDotIndex + 1);
        return FONT_EXTENSION_LIST.contains(assetExtension);
    }

    //截取字体资源font family
    public static String getFontFamily(@NotNull String fontAsset) {
        int startIndex = fontAsset.lastIndexOf("/") + 1;
        int endIndex = fontAsset.lastIndexOf(".");
        if (endIndex < startIndex) {
            endIndex = fontAsset.length();
        }
        return fontAsset.substring(startIndex, endIndex);
    }
}
