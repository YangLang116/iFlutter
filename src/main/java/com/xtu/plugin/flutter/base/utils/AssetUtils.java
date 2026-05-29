package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssetUtils {

    private static final Pattern sPattern = Pattern.compile("\\d+\\.0x");

    public static List<String> supportAssetFoldName(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).resDir;
    }

    public static boolean isFoldRegister(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).foldRegisterEnable;
    }

    public static boolean isAssetDir(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (!virtualFile.isDirectory()) return false;
        String dirName = virtualFile.getName();
        List<String> supportAssetFoldName = supportAssetFoldName(project);
        if (!supportAssetFoldName.contains(dirName)) return false;
        String projectPath = PluginUtils.getProjectPath(project);
        return StringUtils.equals(projectPath, virtualFile.getParent().getPath());
    }

    public static boolean isAssetFile(@NotNull Project project, @Nullable VirtualFile virtualFile) {
        if (virtualFile == null) return false;
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
    public static String getAssetPath(@Nullable String projectPath, @Nullable File assetFile) {
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

    //当前目录名是否多尺寸适配
    //2.0x、3.0x、4.0x
    public static boolean isDimensionDir(@Nullable String dirName) {
        if (StringUtils.isEmpty(dirName)) return false;
        Matcher matcher = sPattern.matcher(dirName);
        return matcher.matches();
    }

    /**
     * 判断是否还存在其他尺寸素材。
     *
     * <p>支持的目录结构：
     * <pre>
     * case 1：同一资产有多个尺寸版本
     *   assets/
     *     a.png
     *     2.0x/a.png
     *     3.0x/a.png
     *
     * case 2：资产仅在尺寸子目录中
     *   assets/
     *     2.0x/a.png
     *     3.0x/a.png
     * </pre>
     *
     * <p>性能优化：原先调用 {@link FileUtils#scanDirectory} 遍历整棵 asset 树（O(n)）；
     * 现改为只检查与当前文件同名的"兄弟尺寸目录"和"父目录"中对应文件是否存在（O(1)），
     * 大幅减少文件系统 IO。</p>
     */
    public static boolean hasOtherDimensionAsset(@Nullable String projectPath, @Nullable File assetFile) {
        if (StringUtils.isEmpty(projectPath) || assetFile == null) return false;
        File parentFile = assetFile.getParentFile();
        String parentName = parentFile.getName();
        String fileName = assetFile.getName();

        if (isDimensionDir(parentName)) {
            // assetFile 在尺寸子目录（如 2.0x/a.png），检查：
            // 1. 父目录的父目录（根目录）中是否有同名文件（a.png）
            // 2. 兄弟尺寸目录（3.0x, 4.0x 等）中是否有同名文件
            File assetRoot = parentFile.getParentFile();
            if (assetRoot == null) return false;
            // 检查根目录下的同名文件
            if (new File(assetRoot, fileName).exists()) return true;
            // 检查兄弟尺寸目录
            return hasSiblingDimensionFile(assetRoot, parentName, fileName);
        } else {
            // assetFile 在非尺寸目录（如 assets/a.png），检查尺寸子目录中是否有同名文件
            return hasSiblingDimensionFile(parentFile, null, fileName);
        }
    }

    /**
     * 在 {@code rootDir} 的直接子目录中，查找是否有（除 {@code excludeDirName} 外的）
     * 尺寸目录包含 {@code fileName}。只检查直接子目录，不递归遍历整棵树。
     */
    private static boolean hasSiblingDimensionFile(@NotNull File rootDir,
                                                    @Nullable String excludeDirName,
                                                    @NotNull String fileName) {
        File[] children = rootDir.listFiles();
        if (children == null) return false;
        for (File child : children) {
            if (!child.isDirectory()) continue;
            String childName = child.getName();
            if (!isDimensionDir(childName)) continue;
            if (StringUtils.equals(childName, excludeDirName)) continue;
            if (new File(child, fileName).exists()) return true;
        }
        return false;
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

    @NotNull
    public static String getResPrefix(@NotNull Project project, @NotNull String projectName) {
        boolean registerResWithPackage = ProjectStorageService.getStorage(project).registerResWithPackage;
        if (registerResWithPackage) return "packages/" + projectName + "/";
        return "";
    }

    @Nullable
    public static String getAssetExtension(String assetFileName) {
        int lastDotIndex = assetFileName.lastIndexOf(".");
        if (lastDotIndex < 0) return null;
        return assetFileName.substring(lastDotIndex + 1);
    }
}
