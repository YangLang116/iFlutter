package com.xtu.plugin.flutter.action.image.task;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptimizeImageFoldTask implements Runnable {

    private final Project project;
    private final VirtualFile virtualFile;

    public OptimizeImageFoldTask(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
    }

    @Override
    public void run() {
        File imageDirectory = new File(virtualFile.getPath());
        try {
            optimizeImage(project, imageDirectory);  //扫描图片，将图片进行分类
        } catch (Exception e) {
            LogUtils.error("OptimizeImageFoldTask run: " + e.getMessage());
            ToastUtil.make(project, MessageType.ERROR, "图片目录优化失败");
        }
    }

    private void optimizeImage(@NotNull Project project, @NotNull File imageDirectory) throws Exception {
        String imageDirectoryName = imageDirectory.getName();
        List<File> imageFileList = new ArrayList<>();
        FileUtils.scanDirectory(imageDirectory, imageFileList::add);
        if (imageFileList.size() <= 0) return;
        //整理图片位置
        String projectPath = PluginUtils.getProjectPath(project);
        if(StringUtils.isEmpty(projectPath)) return;
        Map<String, String> pathMap = new HashMap<>();
        for (File imageFile : imageFileList) {
            String fileName = imageFile.getName();
            if (!fileName.contains("_")) continue;
            String destFoldName = fileName.split("_")[0];
            String parentFoldName = imageFile.getParentFile().getName();
            if (parentFoldName.equals(destFoldName)) continue;
            String relativePath = FileUtils.getAssetPath(projectPath, imageFile);
            File newImageDirectory = new File(imageDirectory, destFoldName);
            org.apache.commons.io.FileUtils.moveFileToDirectory(imageFile, newImageDirectory, true);
            pathMap.put(relativePath, imageDirectoryName + "/" + destFoldName + "/" + imageFile.getName());
        }
        if (pathMap.size() <= 0) return;
        //刷新images目录
        refreshDirectory(imageDirectory);
        //更新dart文件中所有资源引用
        List<File> dartFileList = new ArrayList<>();
        File libDirectory = new File(projectPath, "lib");
        FileUtils.scanDirectory(libDirectory, dartFileList::add);
        if (dartFileList.size() > 0) {
            boolean hasDartFileModify = false;
            for (File file : dartFileList) {
                if (file.getName().endsWith("_res.dart")) continue;
                String fileContent = FileUtils.readFromFile(file);
                if (StringUtils.isEmpty(fileContent)) continue;
                boolean isModify = false;
                for (Map.Entry<String, String> path : pathMap.entrySet()) {
                    if (fileContent.contains(path.getKey())) {
                        isModify = true;
                        fileContent = fileContent.replace(path.getKey(), path.getValue());
                    }
                }
                if (isModify) {
                    hasDartFileModify = true;
                    if (!FileUtils.write2File(file, fileContent)) {
                        throw new IllegalStateException("文件修改失败: " + file.getAbsolutePath());
                    }
                }
            }
            if (hasDartFileModify) {
                refreshDirectory(libDirectory);
            }
        }
        //更新pubspec.yaml中资源引用
        PubspecUtils.readAssetAtReadAction(project, assetList -> {
            List<String> newAssetList = new ArrayList<>();
            for (String asset : assetList) {
                newAssetList.add(pathMap.getOrDefault(asset, asset));
            }
            PubspecUtils.writeAssetAtWriteAction(project, newAssetList);
        });
    }

    private void refreshDirectory(File directory) {
        if (directory == null) return;
        VfsUtil.markDirtyAndRefresh(true, directory.isDirectory(), directory.isDirectory(), directory);
    }
}
