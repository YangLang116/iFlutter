package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.entity.AssetResultEntity;
import com.xtu.plugin.flutter.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

///字体资源文件"font_res.dart"生成

public class DartFontFileGenerator {

    private static final String FONT_FILE_NAME = "font_res.dart";
    private static final String FONT_CLASS_NAME = "FontRes";

    //缓存上一次写入记录，避免重复写入
    private final List<String> latestFontList = new ArrayList<>();

    private static final DartFontFileGenerator sInstance = new DartFontFileGenerator();

    private DartFontFileGenerator() {
    }

    public static DartFontFileGenerator getInstance() {
        return sInstance;
    }

    public void generate(@NotNull Project project, @NotNull AssetResultEntity resultEntity) {
        String projectName = resultEntity.projectName;
        List<String> fontAssetList = resultEntity.fontList;
        String resPrefix = AssetUtils.getResPrefix(project, projectName);
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> WriteAction.run(() -> {
            if (fontAssetList.equals(latestFontList)) return;
            try {
                File libDirectory = new File(project.getBasePath(), "lib");
                LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
                final VirtualFile libVirtualDirectory = localFileSystem.refreshAndFindFileByIoFile(libDirectory);
                assert libVirtualDirectory != null;
                VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
                if (!fontAssetList.isEmpty()) {
                    if (resVirtualDirectory == null) {
                        resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                    }
                    generateFile(project, resVirtualDirectory, resPrefix, fontAssetList);
                } else if (resVirtualDirectory != null) {
                    VirtualFile fontVirtualFile = resVirtualDirectory.findChild(FONT_FILE_NAME);
                    if (fontVirtualFile != null) fontVirtualFile.delete(project);
                }
                latestFontList.clear();
                latestFontList.addAll(fontAssetList);
            } catch (Exception e) {
                LogUtils.error("DartRFileGenerator generate", e);
                ToastUtils.make(project, MessageType.ERROR, e.getMessage());
            }
        }));
    }

    private void generateFile(Project project, VirtualFile rDirectory,
                              @NotNull String resPrefix,
                              @NotNull List<String> fontAssetList) {
        LogUtils.info("DartFontFileGenerator Class: " + FONT_CLASS_NAME);
        StringBuilder fileStringBuilder = new StringBuilder();
        fileStringBuilder.append("/// Generated file. Do not edit.\n")
                .append("/// This file is generated by the iFlutter\n\n")
                .append("// ignore_for_file: constant_identifier_names\n")
                .append("// ignore_for_file: lines_longer_than_80_chars\n")
                .append("class ").append(FONT_CLASS_NAME).append(" {\n");
        //private constructor
        fileStringBuilder.append(FONT_CLASS_NAME).append("._();");
        List<String> familyList = new ArrayList<>();
        for (String fontAsset : fontAssetList) {
            String fontFamily = FontUtils.getFontFamilyName(fontAsset);
            familyList.add(fontFamily);
        }
        CollectionUtils.standardList(familyList);
        for (String fontFamily : familyList) {
            String variantName = getFontVariant(fontFamily);
            fileStringBuilder.append("  static const String ")
                    .append(variantName).append(" = '")
                    .append(resPrefix).append(fontFamily)
                    .append("';\n");
        }
        fileStringBuilder.append("}\n");
        DartUtils.createDartFile(project, rDirectory, FONT_FILE_NAME, fileStringBuilder.toString(), null);
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

    public void resetCache() {
        this.latestFontList.clear();
    }
}
