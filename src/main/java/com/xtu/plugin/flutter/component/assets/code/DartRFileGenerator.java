package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DartRFileGenerator {

    private static final DartRFileGenerator sInstance = new DartRFileGenerator();

    private DartRFileGenerator() {
    }

    public static DartRFileGenerator getInstance() {
        return sInstance;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void generate(Project project, List<String> assetList) {
        WriteAction.run(() -> {
            try {
                File libDirectory = new File(project.getBasePath(), "lib");
                VirtualFile libVirtualDirectory = LocalFileSystem.getInstance().findFileByIoFile(libDirectory);
                assert libVirtualDirectory != null;
                //create new res
                Map<String, List<String>> assetCategory = new HashMap<>();
                for (String assetFileName : assetList) {
                    String assetDirName = assetFileName.substring(0, assetFileName.indexOf("/"));
                    if (!assetCategory.containsKey(assetDirName))
                        assetCategory.put(assetDirName, new ArrayList<>());
                    List<String> assets = assetCategory.get(assetDirName);
                    assets.add(assetFileName);
                }
                VirtualFile resVirtualDirectory = libVirtualDirectory.findChild("res");
                if (assetCategory.size() > 0) {
                    if (resVirtualDirectory == null) {
                        resVirtualDirectory = libVirtualDirectory.createChildDirectory(project, "res");
                    }
                    for (Map.Entry<String, List<String>> entry : assetCategory.entrySet()) {
                        generateFile(project, resVirtualDirectory, entry.getKey(), entry.getValue());
                    }
                } else if (resVirtualDirectory != null) {
                    resVirtualDirectory.delete(project);
                }
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar != null) {
                    statusBar.setInfo("R File Update Success");
                }
            } catch (Exception e) {
                LogUtils.error("DartRFileGenerator generate: " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void generateFile(Project project, VirtualFile rDirectory, String assetDirName, List<String> assetFileNames) throws IOException {
        String className = StringUtil.upFirstChar(assetDirName) + "Res";
        StringBuilder fileStringBuilder = new StringBuilder();
        fileStringBuilder.append("/// This is a generated file do not edit.\n\n")
                .append("class ").append(className).append(" {\n");
        if (!CollectionUtils.isEmpty(assetFileNames)) {
            for (String assetFileName : assetFileNames) {
                if (needIgnoreAsset(project, assetFileName)) continue;
                String variantName = getResName(assetFileName);
                fileStringBuilder.append("  static const String ").append(variantName).append(" = '").append(assetFileName).append("';\n");
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
    }

    private String getResName(String assetFileName) {
        int startIndex = assetFileName.lastIndexOf("/") + 1;
        int endIndex = assetFileName.lastIndexOf(".");
        if (endIndex < startIndex) {
            endIndex = assetFileName.length();
        }
        String variantName = assetFileName.substring(startIndex, endIndex).toUpperCase();
        //replace specific char
        variantName = variantName.replace("-", "_");
        return variantName;
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
