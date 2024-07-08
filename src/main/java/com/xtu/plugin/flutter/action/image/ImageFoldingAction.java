package com.xtu.plugin.flutter.action.image;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.action.AssetDirAction;
import com.xtu.plugin.flutter.base.entity.AssetResultEntity;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import com.xtu.plugin.flutter.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//整理images目录下的图片
public class ImageFoldingAction extends AssetDirAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualFile == null) return;
        doAction(project, virtualFile);
    }

    private void doAction(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        File imageDirectory = new File(virtualFile.getPath());
        ProjectStorageEntity storageEntity = ProjectStorageService.getStorage(project);
        boolean oldStatus = storageEntity.resCheckEnable;
        try {
            storageEntity.resCheckEnable = false; // 归整资源时，为防止反复对pubspec.yaml读写，临时关闭资源监听
            optimizeImage(project, imageDirectory);  //扫描图片，将图片进行分类
        } catch (Exception e) {
            LogUtils.error("OptimizeImageFoldTask run", e);
            ToastUtils.make(project, MessageType.ERROR, "failed to optimize image directory");
        } finally {
            storageEntity.resCheckEnable = oldStatus;
        }
    }

    //获取当前资源文件的父级目录名称(排除1.0x、2.0x...)
    private String getValidParentName(@NotNull File assetFile) {
        File parentFile = assetFile.getParentFile();
        String parentName = parentFile.getName();
        if (AssetUtils.isDimensionDir(parentName)) {
            return parentFile.getParentFile().getName();
        }
        return parentName;
    }

    //获取重整以后的资源存放目录
    private File getDestDirectory(@NotNull File rootDirectory, @NotNull String subDirectoryName, File assetFile) {
        String parentName = assetFile.getParentFile().getName();
        if (AssetUtils.isDimensionDir(parentName)) {
            return new File(rootDirectory, subDirectoryName + File.separator + parentName);
        }
        return new File(rootDirectory, subDirectoryName);
    }

    //删除空文件夹
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteEmptyDirectory(@NotNull File imageDirectory) {
        File[] files = imageDirectory.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            imageDirectory.delete();
            return;
        }
        for (File file : files) {
            if (file.isFile()) continue;
            deleteEmptyDirectory(file);
        }
    }

    private void optimizeImage(@NotNull Project project, @NotNull File imageDirectory) throws Exception {
        List<File> imageFileList = new ArrayList<>();
        FileUtils.scanDirectory(imageDirectory, imageFileList::add);
        if (imageFileList.isEmpty()) return;
        //整理图片位置
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        Map<String, String> pathMap = new HashMap<>();
        for (File assetFile : imageFileList) {
            String fileName = assetFile.getName();
            if (!fileName.contains("_")) continue;
            String destFoldName = fileName.split("_")[0];
            String parentFoldName = getValidParentName(assetFile);
            if (parentFoldName.equals(destFoldName)) continue;
            String oldAssetPath = AssetUtils.getAssetPath(projectPath, assetFile);
            File newImageDirectory = getDestDirectory(imageDirectory, destFoldName, assetFile);
            org.apache.commons.io.FileUtils.moveFileToDirectory(assetFile, newImageDirectory, true);
            String newAssetPath = AssetUtils.getAssetPath(projectPath, new File(newImageDirectory, fileName));
            pathMap.put(oldAssetPath, newAssetPath);
        }
        if (pathMap.isEmpty()) return;
        //删除空目录
        deleteEmptyDirectory(imageDirectory);
        //刷新images目录
        refreshDirectory(imageDirectory);
        //更新dart文件中所有资源引用
        List<File> dartFileList = new ArrayList<>();
        File libDirectory = new File(projectPath, "lib");
        FileUtils.scanDirectory(libDirectory, dartFileList::add);
        if (!dartFileList.isEmpty()) {
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
        WriteCommandAction.runWriteCommandAction(project, () -> {
            AssetResultEntity assetResult = PubSpecUtils.readAssetList(project);
            List<String> newAssetList = new ArrayList<>();
            List<String> newFontList = new ArrayList<>();
            for (String asset : assetResult.assetList) {
                newAssetList.add(pathMap.getOrDefault(asset, asset));
            }
            for (String fontAsset : assetResult.fontList) {
                newFontList.add(pathMap.getOrDefault(fontAsset, fontAsset));
            }
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
    }

    private void refreshDirectory(File directory) {
        if (directory == null) return;
        VfsUtil.markDirtyAndRefresh(true, directory.isDirectory(), directory.isDirectory(), directory);
    }
}
