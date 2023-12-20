package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.store.StorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetUtils {

    private static final Pattern sPattern = Pattern.compile("\\d+\\.0x");

    //当前目录名是否多尺寸适配
    //2.0x、3.0x、4.0x
    public static boolean isDimensionDir(String dirName) {
        if (StringUtils.isEmpty(dirName)) return false;
        Matcher matcher = sPattern.matcher(dirName);
        return matcher.matches();
    }

    public static boolean isAssetFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String assetPath = virtualFile.getPath();
        File assetFile = new File(assetPath);
        return isAssetFile(project, assetFile);
    }

    private static boolean isAssetFile(@NotNull Project project, @NotNull File file) {
        if (file.isDirectory()) return false;
        if (file.getName().startsWith(".")) return false;
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        String filePath = file.getAbsolutePath();
        File projectDirectory = new File(projectPath);
        List<String> supportAssetFoldName = supportAssetFoldName(project);
        for (String directoryName : supportAssetFoldName) {
            File assetDirectory = new File(projectDirectory, directoryName);
            if (filePath.startsWith(assetDirectory.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String getAssetFilePath(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        File assetFile = new File(virtualFile.getPath());
        return getAssetFilePath(project, assetFile);
    }

    @Nullable
    public static String getAssetFilePath(@NotNull Project project, @NotNull File file) {
        if (isAssetFile(project, file)) {
            String projectPath = PluginUtils.getProjectPath(project);
            return getAssetPath(projectPath, file);
        }
        return null;
    }

    //获取asset配置
    @Nullable
    public static String getAssetPath(String projectPath, File assetFile) {
        if (StringUtils.isEmpty(projectPath) || assetFile == null) return null;
        String relativePath = FileUtils.getRelativePath(projectPath, assetFile);
        if (StringUtils.isEmpty(relativePath)) return null;
        String parentName = assetFile.getParentFile().getName();
        if (isDimensionDir(parentName)) {
            return relativePath.replace(parentName + "/", "");
        } else {
            return relativePath;
        }
    }

    /**
     * 判断是否还存在其他尺寸素材
     * <p>
     * case 1：
     * -2.0x
     * - a.png
     * -3.0x
     * - a.png
     * <p>
     * case 2:
     * -2.0x
     * - a.png
     * -a.png
     */
    public static boolean hasOtherDimensionAsset(String projectPath, File assetFile) {
        if (StringUtils.isEmpty(projectPath) || assetFile == null) return false;
        File parentFile = assetFile.getParentFile();
        String parentName = parentFile.getName();
        if (isDimensionDir(parentName)) {
            return hasOtherDimensionAsset(projectPath, parentFile.getParentFile(), assetFile);
        } else {
            return hasOtherDimensionAsset(projectPath, parentFile, assetFile);
        }
    }

    private static boolean hasOtherDimensionAsset(@NotNull String projectPath,
                                                  @NotNull File rootFile,
                                                  @NotNull File compareFile) {
        String compareRelativePath = FileUtils.getRelativePath(projectPath, compareFile);
        String compareAssetPath = getAssetPath(projectPath, compareFile);
        List<File> fileList = new ArrayList<>();
        FileUtils.scanDirectory(rootFile, fileList::add);
        for (File assetFile : fileList) {
            //资源路径一样，但是相对路径不一样
            String currentAssetPath = getAssetPath(projectPath, assetFile);
            String currentRelativePath = FileUtils.getRelativePath(projectPath, assetFile);
            if (StringUtils.equals(currentAssetPath, compareAssetPath)
                    && !StringUtils.equals(currentRelativePath, compareRelativePath)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> supportAssetFoldName(@NotNull Project project) {
        return StorageService.getInstance(project).getState().resDir;
    }

    public static boolean isFoldRegister(@NotNull Project project) {
        return StorageService.getInstance(project).getState().foldRegisterEnable;
    }

    @NotNull
    public static List<File> getAllAssetFile(@NotNull Project project) {
        List<File> assetFileList = new ArrayList<>();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return assetFileList;
        List<String> assetFoldNameList = supportAssetFoldName(project);
        for (String assetFoldName : assetFoldNameList) {
            File assetDirectory = new File(projectPath, assetFoldName);
            FileUtils.scanDirectory(assetDirectory, (file -> {
                String fileName = file.getName();
                //修复隐藏文件导致，生成的res.dart文件异常，如mac下的.DS_Store文件
                if (!fileName.startsWith(".")) {
                    assetFileList.add(file);
                }
            }));
        }
        return assetFileList;
    }
}
