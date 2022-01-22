package com.xtu.plugin.flutter.component.assets.code;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void generate(Project project, List<String> assetList) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                File libDirectory = new File(project.getBasePath(), "lib");
                File rDirectory = new File(libDirectory, "res");
                org.apache.commons.io.FileUtils.deleteDirectory(rDirectory);
                Map<String, List<String>> assetCategory = new HashMap<>();
                for (String assetFileName : assetList) {
                    String assetDirName = assetFileName.substring(0, assetFileName.indexOf("/"));
                    if (!assetCategory.containsKey(assetDirName))
                        assetCategory.put(assetDirName, new ArrayList<>());
                    List<String> assets = assetCategory.get(assetDirName);
                    assets.add(assetFileName);
                }
                if (assetCategory.size() > 0) {
                    if (!rDirectory.exists()) rDirectory.mkdirs();
                    for (Map.Entry<String, List<String>> entry : assetCategory.entrySet()) {
                        generateFile(project, rDirectory, entry.getKey(), entry.getValue());
                    }
                }
                //刷新lib目录
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(libDirectory);
                if (virtualFile != null) {
                    virtualFile.refresh(false, true, () -> {
                        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                        if (statusBar != null) {
                            statusBar.setInfo("R File Update Success");
                        }
                    });
                }
            } catch (Exception e) {
                LogUtils.error("DartRFileGenerator generate: " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void generateFile(Project project, File rDirectory, String assetDirName, List<String> assetFileNames) throws IOException {
        File resFile = new File(rDirectory, assetDirName.toLowerCase() + "_res.dart");
        if (!resFile.exists()) resFile.createNewFile();
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
        FileUtils.write2File(resFile, fileStringBuilder.toString());
        DartUtils.format(project, resFile.getAbsolutePath());
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
