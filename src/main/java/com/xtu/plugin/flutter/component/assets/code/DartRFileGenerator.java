package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DartRFileGenerator {

    //缓存上一次写入记录，避免重复写入
    private final List<String> latestAssetList = new ArrayList<>();

    private static final DartRFileGenerator sInstance = new DartRFileGenerator();

    private DartRFileGenerator() {
    }

    public static DartRFileGenerator getInstance() {
        return sInstance;
    }

    public void generate(@NotNull Project project,
                         @NotNull String projectName,
                         @NotNull String projectVersion,
                         @NotNull String resPrefix,
                         @NotNull List<String> assetList) {
        if (assetList.equals(latestAssetList)) return;
        //create new res
        Map<String, List<String>> assetCategory = new HashMap<>();
        for (String assetFileName : assetList) {
            String assetDirName = assetFileName.substring(0, assetFileName.indexOf("/"));
            if (!assetCategory.containsKey(assetDirName))
                assetCategory.put(assetDirName, new ArrayList<>());
            List<String> assets = assetCategory.get(assetDirName);
            assets.add(assetFileName);
        }
        try {
            //virtual file
            File libDirectory = new File(project.getBasePath(), "lib");
            LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
            final VirtualFile libVirtualDirectory = localFileSystem.refreshAndFindFileByIoFile(libDirectory);
            assert libVirtualDirectory != null;
            VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
            if (assetCategory.size() > 0) {
                if (resVirtualDirectory == null) {
                    resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                }
                List<String> usefulFileNameList = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : assetCategory.entrySet()) {
                    String fileName = generateFile(project, resVirtualDirectory,
                            resPrefix,
                            entry.getKey(), entry.getValue(),
                            projectName, projectVersion);
                    usefulFileNameList.add(fileName);
                }
                //删除无用文件
                deleteUselessFile(project, resVirtualDirectory, usefulFileNameList);
            } else if (resVirtualDirectory != null) {
                deleteUselessFile(project, resVirtualDirectory, Collections.emptyList());
            }
            latestAssetList.clear();
            latestAssetList.addAll(assetList);
        } catch (Exception e) {
            LogUtils.error("DartRFileGenerator generate: " + e.getMessage());
            ToastUtil.make(project, MessageType.ERROR, e.getMessage());
        }
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
            child.delete(project);
        }
    }

    @NotNull
    private String generateFile(@NotNull Project project, @NotNull VirtualFile rDirectory,
                                @NotNull String resPrefix,
                                @NotNull String assetDirName, @NotNull List<String> assetFileNames,
                                @NotNull String projectName,
                                @NotNull String projectVersion) {
        String className = getClassName(assetDirName);
        StringBuilder fileStringBuilder = new StringBuilder();
        fileStringBuilder.append("/// Generated file. Do not edit.\n\n")
                .append("// ignore_for_file: constant_identifier_names\n")
                .append("// ignore_for_file: lines_longer_than_80_chars\n")
                .append("class ").append(className).append(" {\n");
        //create name and version
        fileStringBuilder.append("  static const String PLUGIN_NAME = '").append(projectName).append("';\n");
        fileStringBuilder.append("  static const String PLUGIN_VERSION = '").append(projectVersion).append("';\n");
        if (!CollectionUtils.isEmpty(assetFileNames)) {
            for (String assetFileName : assetFileNames) {
                if (needIgnoreAsset(project, assetFileName)) continue;
                String variantName = getResName(assetFileName);
                fileStringBuilder.append("  static const String ")
                        .append(variantName).append(" = '")
                        .append(resPrefix).append(assetFileName)
                        .append("';\n");
            }
        }
        fileStringBuilder.append("}\n");
        String fileName = assetDirName.toLowerCase() + "_res.dart";
        DartUtils.createDartFile(project, rDirectory, fileName, fileStringBuilder.toString(), new DartUtils.OnCreateDartFileListener() {
            @Override
            public void onSuccess(@NotNull VirtualFile virtualFile) {
                //ignore
            }

            @Override
            public void onFail(String message) {
                ToastUtil.make(project, MessageType.ERROR, message);
            }
        });
        return fileName;
    }

    @NotNull
    public static String getClassName(String assetDirName) {
        return StringUtil.getClassName(assetDirName) + "Res";
    }

    public static String getResName(String assetFileName) {
        int startIndex = assetFileName.lastIndexOf("/") + 1;
        int endIndex = assetFileName.lastIndexOf(".");
        if (endIndex < startIndex) {
            endIndex = assetFileName.length();
        }
        return assetFileName.substring(startIndex, endIndex)
                .toUpperCase()
                .replace("-", "_");
    }

    private boolean needIgnoreAsset(Project project, String assetFileName) {
        if (assetFileName.endsWith("/")) return true;
        String extension = getAssetExtension(assetFileName);
        if (StringUtils.isEmpty(extension)) return false;
        StorageService storageService = StorageService.getInstance(project);
        List<String> ignoreResExtension = storageService.getState().ignoreResExtension;
        return ignoreResExtension.contains(extension);
    }

    private String getAssetExtension(String assetFileName) {
        int lastDotIndex = assetFileName.lastIndexOf(".");
        if (lastDotIndex < 0) return null;
        return assetFileName.substring(lastDotIndex);
    }
}
