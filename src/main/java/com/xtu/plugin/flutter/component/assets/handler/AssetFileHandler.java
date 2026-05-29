package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * 将 VFS 文件事件转换为对 pubspec.yaml 资源列表的 AssetMutation。
 * 不直接写 pubspec.yaml，只返回待执行的 mutation，由调用方批量刷新。
 */
public class AssetFileHandler {

    private final PubSpecFileHandler specFileHandler;

    public AssetFileHandler(PubSpecFileHandler specFileHandler) {
        this.specFileHandler = specFileHandler;
    }

    public PubSpecFileHandler getSpecFileHandler() {
        return specFileHandler;
    }

    /**
     * 文件新增 → 返回"将该 asset 加入列表"的 mutation。
     */
    @Nullable
    public AssetMutation computeAddMutation(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String assetPath = AssetUtils.getAssetFilePath(project, virtualFile);
        if (assetPath == null) return null;
        return (al, fl) -> PubSpecFileHandler.addAssetToList(al, fl, assetPath);
    }

    /**
     * 文件删除 → 若还有其他尺寸资源则保留（add），否则移除。
     */
    @Nullable
    public AssetMutation computeDeleteMutation(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        File deleteFile = new File(virtualFile.getPath());
        return computeDeleteMutation(project, deleteFile);
    }

    @Nullable
    private AssetMutation computeDeleteMutation(@NotNull Project project, @NotNull File file) {
        String assetPath = AssetUtils.getAssetFilePath(project, file);
        if (assetPath == null) return null;
        String projectPath = PluginUtils.getProjectPath(project);
        if (AssetUtils.hasOtherDimensionAsset(projectPath, file)) {
            // 还有其他尺寸版本，保留 assetPath 注册（兼容 IDEA 自动引用处理）
            return (al, fl) -> PubSpecFileHandler.addAssetToList(al, fl, assetPath);
        } else {
            return (al, fl) -> PubSpecFileHandler.removeAssetFromList(al, fl, assetPath);
        }
    }

    /**
     * 文件重命名 → 若旧名还有其他尺寸资源则同时保留旧路径，否则做 change。
     */
    @Nullable
    public AssetMutation computeRenameMutation(@NotNull Project project,
                                               @NotNull VirtualFile virtualFile,
                                               @NotNull String oldName,
                                               @NotNull String newName) {
        String newAssetPath = AssetUtils.getAssetFilePath(project, virtualFile);
        if (newAssetPath == null) return null;
        String projectPath = PluginUtils.getProjectPath(project);
        File newAssetFile = new File(virtualFile.getPath());
        File oldAssetFile = new File(newAssetFile.getParentFile(), oldName);
        String oldAssetPath = newAssetPath.replace(newName, oldName);
        if (AssetUtils.hasOtherDimensionAsset(projectPath, oldAssetFile)) {
            return (al, fl) -> {
                PubSpecFileHandler.addAssetToList(al, fl, newAssetPath);
                PubSpecFileHandler.addAssetToList(al, fl, oldAssetPath);
            };
        } else {
            return (al, fl) -> {
                PubSpecFileHandler.removeAssetFromList(al, fl, oldAssetPath);
                PubSpecFileHandler.addAssetToList(al, fl, newAssetPath);
            };
        }
    }

    /**
     * 文件移动 → 根据新旧路径是否在 asset 目录，决定 add / remove / change。
     */
    @Nullable
    public AssetMutation computeMoveMutation(@NotNull Project project,
                                             @NotNull File oldFile,
                                             @NotNull VirtualFile newFile) {
        String oldAssetPath = AssetUtils.getAssetFilePath(project, oldFile);
        String newAssetPath = AssetUtils.getAssetFilePath(project, newFile);
        if (oldAssetPath != null && newAssetPath == null) {
            // 从 asset 目录移到外面 → 删除
            return computeDeleteMutation(project, oldFile);
        }
        if (newAssetPath == null) return null;
        if (oldAssetPath == null) {
            // 从外面移进 asset 目录 → 新增
            return (al, fl) -> PubSpecFileHandler.addAssetToList(al, fl, newAssetPath);
        }
        String projectPath = PluginUtils.getProjectPath(project);
        if (AssetUtils.hasOtherDimensionAsset(projectPath, oldFile)) {
            return (al, fl) -> {
                PubSpecFileHandler.addAssetToList(al, fl, newAssetPath);
                PubSpecFileHandler.addAssetToList(al, fl, oldAssetPath);
            };
        } else {
            return (al, fl) -> {
                PubSpecFileHandler.removeAssetFromList(al, fl, oldAssetPath);
                PubSpecFileHandler.addAssetToList(al, fl, newAssetPath);
            };
        }
    }
}
