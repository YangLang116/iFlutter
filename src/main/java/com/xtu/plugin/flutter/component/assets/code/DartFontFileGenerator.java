package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.utils.DartUtils;
import com.xtu.plugin.flutter.utils.FontUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

///字体资源文件"i_font_res.dart"生成

public class DartFontFileGenerator {

    private static final String FONT_FILE_NAME = "i_font_res.dart";
    private static final String FONT_CLASS_NAME = "FontRes";

    //缓存上一次写入记录，避免重复写入
    private final List<String> latestFontList = new ArrayList<>();

    private static final DartFontFileGenerator sInstance = new DartFontFileGenerator();

    private DartFontFileGenerator() {
    }

    public static DartFontFileGenerator getInstance() {
        return sInstance;
    }

    public void generate(Project project, @NotNull List<String> fontAssetList) {
        if (fontAssetList.equals(latestFontList)) return;
        try {
            File libDirectory = new File(project.getBasePath(), "lib");
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
            final VirtualFile libVirtualDirectory = localFileSystem.refreshAndFindFileByIoFile(libDirectory);
            assert libVirtualDirectory != null;
            VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
            if (fontAssetList.size() > 0) {
                if (resVirtualDirectory == null) {
                    resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                }
                generateFile(project, resVirtualDirectory, fontAssetList);
            } else if (resVirtualDirectory != null) {
                VirtualFile fontVirtualFile = resVirtualDirectory.findChild(FONT_FILE_NAME);
                if (fontVirtualFile != null) fontVirtualFile.delete(project);
            }
            latestFontList.clear();
            latestFontList.addAll(fontAssetList);
        } catch (Exception e) {
            LogUtils.error("DartRFileGenerator generate: " + e.getMessage());
            ToastUtil.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    @NotNull
    private void generateFile(Project project, VirtualFile rDirectory, @NotNull List<String> fontAssetList) {
        StringBuilder fileStringBuilder = new StringBuilder();
        fileStringBuilder.append("/// Generated file. Do not edit.\n\n")
                .append("// ignore_for_file: constant_identifier_names\n")
                .append("// ignore_for_file: lines_longer_than_80_chars\n")
                .append("class ").append(FONT_CLASS_NAME).append(" {\n");
        for (String fontAsset : fontAssetList) {
            String fontFamily = FontUtils.getFontFamily(fontAsset);
            String variantName = getFontVariant(fontFamily);
            fileStringBuilder.append("  static const String ")
                    .append(variantName).append(" = '").append(fontFamily)
                    .append("';\n");
        }
        fileStringBuilder.append("}\n");
        DartUtils.createDartFile(project, rDirectory, FONT_FILE_NAME, fileStringBuilder.toString(), new DartUtils.OnCreateDartFileListener() {
            @Override
            public void onSuccess(@NotNull VirtualFile virtualFile) {
                //ignore
            }

            @Override
            public void onFail(String message) {
                ToastUtil.make(project, MessageType.ERROR, message);
            }
        });
    }

    @NotNull
    public static String getFontVariant(String fontFamily) {
        return fontFamily.toUpperCase().replace("-", "_");
    }

    public static String getFileName() {
        return FONT_FILE_NAME;
    }

    public static String getClassName() {
        return FONT_CLASS_NAME;
    }
}
