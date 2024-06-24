package com.xtu.plugin.flutter.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FontUtils {

    private static final List<String> FONT_EXTENSION_LIST = List.of("ttf", "font", "fon", "otf", "eot", "woff", "ttc");

    //通过后缀来简单区分资源类型
    public static boolean isFontAsset(@NotNull String assetName) {
        int lastDotIndex = assetName.lastIndexOf(".");
        if (lastDotIndex < 0) return false;
        String assetExtension = assetName.substring(lastDotIndex + 1);
        return FONT_EXTENSION_LIST.contains(assetExtension);
    }

    //截取字体资源font family: (注意以下情况）
    //fonts:
    //    - family: test
    //      fonts:
    //        - asset: fonts/test.ttf
    //        - asset: fonts/test@style_italic.ttf
    //          style: italic
    public static String getFontFamilyName(@NotNull String fontAsset) {
        int startIndex = fontAsset.lastIndexOf("/") + 1;
        int endIndex = fontAsset.lastIndexOf(".");
        if (endIndex < startIndex) {
            endIndex = fontAsset.length();
        }
        String assetFileName = fontAsset.substring(startIndex, endIndex);
        if (assetFileName.contains("@")) return assetFileName.split("@")[0];
        return assetFileName;
    }

    @NotNull
    public static List<FontFamily> getFontFamilyList(@NotNull List<String> fontList) {
        final Map<String, FontFamily> fontFamilyMap = new HashMap<>();
        for (String fontAsset : fontList) {
            String familyName = getFontFamilyName(fontAsset);
            if (!fontFamilyMap.containsKey(familyName)) {
                fontFamilyMap.put(familyName, new FontFamily(familyName));
            }
            FontFamily fontFamilyEntity = fontFamilyMap.get(familyName);
            FontAsset fontAssetEntity = FontAsset.parse(fontAsset);
            fontFamilyEntity.fonts.add(fontAssetEntity);
        }
        List<FontFamily> fontFamilyList = new ArrayList<>(fontFamilyMap.values());
        fontFamilyList.sort(Comparator.comparing(family -> family.family));
        return fontFamilyList;
    }

    public static class FontFamily {
        public final String family;
        public final List<FontAsset> fonts = new ArrayList<>();

        public FontFamily(String name) {
            this.family = name;
        }
    }

    public static class FontAsset {
        public final String asset;
        public final Map<String, String> extras;

        public FontAsset(String asset, Map<String, String> extras) {
            this.asset = asset;
            this.extras = extras;
        }

        public static FontAsset parse(String fontAsset) {
            int startIndex = fontAsset.lastIndexOf("/") + 1;
            int endIndex = fontAsset.lastIndexOf(".");
            if (endIndex < startIndex) {
                endIndex = fontAsset.length();
            }
            String assetFileName = fontAsset.substring(startIndex, endIndex);
            Map<String, String> extras = new HashMap<>();
            if (assetFileName.contains("@")) {
                String styleStr = assetFileName.split("@")[1];
                String[] styleList = styleStr.split("_");
                if (styleList.length % 2 == 0) {
                    for (int i = 0; i < styleList.length; i += 2) {
                        extras.put(styleList[i], styleList[i + 1]);
                    }
                }
            }
            return new FontAsset(fontAsset, extras);
        }
    }
}
