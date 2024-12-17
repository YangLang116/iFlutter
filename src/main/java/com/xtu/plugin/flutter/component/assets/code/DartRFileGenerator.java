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
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import org.jetbrains.annotations.NotNull;
import template.PluginTemplate;
import template.data.ResFileTemplateData;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DartRFileGenerator {

    private static final DartRFileGenerator sInstance = new DartRFileGenerator();

    private DartRFileGenerator() {
    }

    public static DartRFileGenerator getInstance() {
        return sInstance;
    }

    public void generate(@NotNull Project project, @NotNull AssetInfoMetaEntity meta, @NotNull List<String> assetList) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> WriteAction.run(() -> {
            try {
                Map<String, List<String>> assetCategory = categoryAssetList(assetList);
                LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
                File libDirectory = new File(project.getBasePath(), "lib");
                final VirtualFile libVirtualDirectory = localFileSystem.refreshAndFindFileByIoFile(libDirectory);
                assert libVirtualDirectory != null;
                VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
                if (!assetCategory.isEmpty()) {
                    if (resVirtualDirectory == null) {
                        resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                    }
                    List<String> usefulFileNameList = new ArrayList<>();
                    for (Map.Entry<String, List<String>> entry : assetCategory.entrySet()) {
                        String dirName = entry.getKey();
                        List<String> assetPathList = entry.getValue();
                        String fileName = generateFile(project, meta, resVirtualDirectory, dirName, assetPathList);
                        usefulFileNameList.add(fileName);
                    }
                    //删除无用文件
                    deleteUselessFile(project, resVirtualDirectory, usefulFileNameList);
                } else if (resVirtualDirectory != null) {
                    deleteUselessFile(project, resVirtualDirectory, Collections.emptyList());
                }
            } catch (Exception e) {
                LogUtils.error("DartRFileGenerator generate", e);
                ToastUtils.make(project, MessageType.ERROR, e.getMessage());
            }
        }));
    }

    @NotNull
    private Map<String, List<String>> categoryAssetList(List<String> assetList) {
        Map<String, List<String>> assetCategory = new HashMap<>();
        for (String assetFileName : assetList) {
            String assetDirName = getAssetDirName(assetFileName);
            if (!assetCategory.containsKey(assetDirName)) {
                assetCategory.put(assetDirName, new ArrayList<>());
            }
            List<String> assets = assetCategory.get(assetDirName);
            assets.add(assetFileName);
        }
        return assetCategory;
    }

    private void deleteUselessFile(@NotNull Project project,
                                   @NotNull VirtualFile directory,
                                   @NotNull List<String> excludeNameList) throws IOException {
        VirtualFile[] children = directory.getChildren();
        if (children == null) return;
        for (VirtualFile child : children) {
            String fileName = child.getName();
            if (Objects.equals(fileName, DartFontFileGenerator.getFileName())) continue;
            if (excludeNameList.contains(fileName)) continue;
            if (!fileName.endsWith("_res.dart")) continue;
            child.delete(project);
        }
    }

    @NotNull
    private String generateFile(@NotNull Project project,
                                @NotNull AssetInfoMetaEntity meta,
                                @NotNull VirtualFile rDirectory,
                                @NotNull String assetDirName,
                                @NotNull List<String> assetPathList) {
        String className = getClassName(assetDirName);
        LogUtils.info("DartRFileGenerator Class: " + className);
        String content = buildFileContent(project, meta, className, assetPathList);
        String fileName = assetDirName.toLowerCase() + "_res.dart";
        DartUtils.createDartFile(project, rDirectory, fileName, content);
        return fileName;
    }

    @NotNull
    private String buildFileContent(@NotNull Project project,
                                    @NotNull AssetInfoMetaEntity meta,
                                    @NotNull String className,
                                    @NotNull List<String> assetPathList) {
        String resPrefix = AssetUtils.getResPrefix(project, meta.projectName);
        List<ResFileTemplateData.Field> fieldList = new ArrayList<>();
        fieldList.add(new ResFileTemplateData.Field("PLUGIN_NAME", meta.projectName));
        fieldList.add(new ResFileTemplateData.Field("PLUGIN_VERSION", meta.projectVersion));
        for (String assetPath : assetPathList) {
            if (ignoreAsset(project, assetPath)) continue;
            String variantName = getResName(assetPath);
            fieldList.add(new ResFileTemplateData.Field(variantName, resPrefix + assetPath));
        }
        ResFileTemplateData data = new ResFileTemplateData(className, fieldList);
        return PluginTemplate.getResFileContent(data);
    }


    @NotNull
    private static String getAssetDirName(String assetFileName) {
        int index = assetFileName.indexOf("/");
        if (index <= 0) return "default";
        return assetFileName.substring(0, index);
    }

    private boolean ignoreAsset(Project project, String assetFileName) {
        if (assetFileName.endsWith("/")) return true;
        String extension = AssetUtils.getAssetExtension(assetFileName);
        if (StringUtils.isEmpty(extension)) return false;
        List<String> ignoreResExtension = ProjectStorageService.getStorage(project).ignoreResExtension;
        return ignoreResExtension.contains(extension);
    }

    @NotNull
    public static String getClassName(String assetDirName) {
        return ClassUtils.getClassName(assetDirName) + "Res";
    }

    @NotNull
    public static String getResName(String assetPath) {
        int startIndex = assetPath.lastIndexOf("/") + 1;
        int endIndex = assetPath.lastIndexOf(".");
        if (endIndex < startIndex) {
            endIndex = assetPath.length();
        }
        String assetName = assetPath.substring(startIndex, endIndex);
        return ClassUtils.getFieldName(assetName);
    }
}
