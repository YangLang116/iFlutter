package com.xtu.plugin.flutter.action.analysis.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.analysis.ui.UselessResAnalysisDialog;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class UselessResAnalysisTask extends Task.Backgroundable {

    private final Project project;
    private final String projectPath;

    public UselessResAnalysisTask(Project project, String projectPath) {
        super(project, "analysis useless res", false);
        this.project = project;
        this.projectPath = projectPath;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        Map<File, String> fileList = getAssetFileList();
        List<AssetFile> assetFileList = convertAsset(fileList);
        scanCode(assetFileList);
        showResultDialog(assetFileList);
        indicator.setIndeterminate(false);
        indicator.setFraction(1);

    }

    private List<AssetFile> convertAsset(Map<File, String> fileList) {
        List<AssetFile> assetFileList = new ArrayList<>();
        for (Map.Entry<File, String> entry : fileList.entrySet()) {
            String foldName = entry.getValue();
            File file = entry.getKey();
            List<String> assetList = getAssetList(foldName, file);
            assetFileList.add(new AssetFile(file, assetList, 0));
        }
        return assetFileList;
    }

    private List<String> getAssetList(String foldName, File file) {
        List<String> assetList = new ArrayList<>();
        //资源绝对路径表示
        String relativePath = FileUtils.getRelativePath(projectPath, file);
        if (relativePath != null) {
            assetList.add(relativePath);
            //字体font_family表示
            if (FontUtils.isFontAsset(relativePath)) {
                String fontFamilyName = FontUtils.getFontFamilyName(relativePath);
                assetList.add(fontFamilyName);
                //添加字体R表示方式
                String fontClassName = DartFontFileGenerator.getClassName();
                String fontVariantName = DartFontFileGenerator.getFontVariant(fontFamilyName);
                assetList.add(fontClassName + "." + fontVariantName);
            } else {
                //添加资源R表示方式
                String resClassName = DartRFileGenerator.getClassName(foldName);
                String resVariantName = DartRFileGenerator.getResName(relativePath);
                assetList.add(resClassName + "." + resVariantName);
            }
        }
        String assetPath = AssetUtils.getAssetPath(projectPath, file);
        if (assetPath != null && !StringUtils.equals(assetPath, relativePath)) {
            //资源dimension路径表示
            assetList.add(assetPath);
        }
        return assetList;
    }

    private Map<File, String> getAssetFileList() {
        final Map<File, String> assetFileList = new HashMap<>();
        List<String> foldNameList = AssetUtils.supportAssetFoldName(project);
        for (String foldName : foldNameList) {
            File assetFold = new File(projectPath, foldName);
            FileUtils.scanDirectory(assetFold, file -> assetFileList.put(file, foldName));
        }
        return assetFileList;
    }

    private void scanCode(List<AssetFile> assetFileList) {
        File codeFold = new File(projectPath, "lib");
        FileUtils.scanDirectory(codeFold, dartFile -> {
            String fileName = dartFile.getName();
            if (!fileName.endsWith(".dart")) return;
            if (fileName.endsWith("_res.dart")) return;
            String fileContent = null;
            try {
                fileContent = FileUtils.readFromFile(dartFile);
            } catch (Exception e) {
                //ignore
            }
            if (StringUtils.isEmpty(fileContent)) return;
            for (AssetFile assetFile : assetFileList) {
                for (String asset : assetFile.assetList) {
                    if (fileContent.contains(asset)) assetFile.useCount++;
                }
            }
        });
    }

    private void showResultDialog(List<AssetFile> assetFileList) {
        List<String> resultList = new ArrayList<>();
        for (AssetFile assetFile : assetFileList) {
            if (assetFile.useCount != 0) continue;
            String relativePath = FileUtils.getRelativePath(projectPath, assetFile.file);
            resultList.add(relativePath);
        }
        Collections.sort(resultList);
        final String result = CollectionUtils.join(resultList, "\n");
        ApplicationManager.getApplication()
                .invokeLater(() -> {
                    if (StringUtils.isEmpty(result)) {
                        ToastUtil.make(project, MessageType.INFO, "no useless resources found");
                    } else {
                        new UselessResAnalysisDialog(project, result).show();
                    }
                });
    }

    private static class AssetFile {

        public final File file;
        public final List<String> assetList;
        public int useCount;

        public AssetFile(File file, List<String> assetList, int useCount) {
            this.file = file;
            this.assetList = assetList;
            this.useCount = useCount;
        }
    }

}
