package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.entity.AssetInfoMetaEntity;
import com.xtu.plugin.flutter.base.utils.*;
import org.jetbrains.annotations.NotNull;
import template.PluginTemplate;
import template.data.ResFileTemplateData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/// 字体资源文件"font_res.dart"生成

public class DartFontFileGenerator {

    private static final String FONT_FILE_NAME = "font_res.dart";
    private static final String FONT_CLASS_NAME = "FontRes";

    private static final DartFontFileGenerator sInstance = new DartFontFileGenerator();

    private DartFontFileGenerator() {
    }

    public static DartFontFileGenerator getInstance() {
        return sInstance;
    }

    public void generate(@NotNull Project project, @NotNull AssetInfoMetaEntity meta, @NotNull List<String> assetList) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> WriteAction.run(() -> {
            try {
                LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
                File libDirectory = new File(project.getBasePath(), "lib");
                final VirtualFile libVirtualDirectory = localFileSystem.refreshAndFindFileByIoFile(libDirectory);
                assert libVirtualDirectory != null;
                VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
                if (!assetList.isEmpty()) {
                    if (resVirtualDirectory == null) {
                        resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                    }
                    generateFile(project, meta, resVirtualDirectory, assetList);
                } else if (resVirtualDirectory != null) {
                    VirtualFile fontVirtualFile = resVirtualDirectory.findChild(FONT_FILE_NAME);
                    if (fontVirtualFile != null) fontVirtualFile.delete(project);
                }
            } catch (Exception e) {
                LogUtils.error("DartRFileGenerator generate", e);
                ToastUtils.make(project, MessageType.ERROR, e.getMessage());
            }
        }));
    }

    private void generateFile(@NotNull Project project,
                              @NotNull AssetInfoMetaEntity meta,
                              @NotNull VirtualFile rDirectory,
                              @NotNull List<String> fontAssetList) {
        LogUtils.info("DartFontFileGenerator Class: " + FONT_CLASS_NAME);
        String content = buildFileContent(project, meta, fontAssetList);
        DartUtils.createDartFile(project, rDirectory, FONT_FILE_NAME, content);
    }

    @NotNull
    private static String buildFileContent(@NotNull Project project,
                                           @NotNull AssetInfoMetaEntity meta,
                                           @NotNull List<String> fontAssetList) {
        List<String> familyList = new ArrayList<>();
        for (String fontAsset : fontAssetList) {
            String fontFamily = FontUtils.getFontFamilyName(fontAsset);
            familyList.add(fontFamily);
        }
        CollectionUtils.standardList(familyList);

        String resPrefix = AssetUtils.getResPrefix(project, meta.projectName);
        List<ResFileTemplateData.Field> fieldList = new ArrayList<>();
        fieldList.add(new ResFileTemplateData.Field("PLUGIN_NAME", meta.projectName));
        fieldList.add(new ResFileTemplateData.Field("PLUGIN_VERSION", meta.projectVersion));
        for (String fontFamily : familyList) {
            String variantName = getFontVariant(fontFamily);
            fieldList.add(new ResFileTemplateData.Field(variantName, resPrefix + fontFamily));
        }
        ResFileTemplateData data = new ResFileTemplateData(FONT_CLASS_NAME, fieldList);
        return PluginTemplate.getResFileContent(data);
    }

    @NotNull
    public static String getFontVariant(String fontFamily) {
        return ClassUtils.getFieldName(fontFamily);
    }

    public static String getFileName() {
        return FONT_FILE_NAME;
    }

    public static String getClassName() {
        return FONT_CLASS_NAME;
    }
}
