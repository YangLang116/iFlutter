package com.xtu.plugin.flutter.base.adapter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.util.messages.MessageBusConnection;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * VFS 文件事件监听基类。
 *
 * <p>性能优化：{@link #after(List)} 在处理批量 VFS 事件前，
 * 先缓存一次 {@code projectPath} 和 {@code assetDirList}，
 * 避免对每个事件都重复查询 {@code ProjectStorageService}。
 * 对 git checkout 等可能带来数百个 VFS 事件的操作效果明显。</p>
 */
public class AssetFileChangedObserver implements BulkFileListener, BranchChangeListener {

    public final Project project;
    private boolean isBranchChanging = false;
    private MessageBusConnection connection;

    public AssetFileChangedObserver(@NotNull Project project) {
        this.project = project;
    }

    public void bindFileChangedEvent() {
        if (connection != null) return;
        connection = project.getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
        connection.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, this);
    }

    public void disconnect() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    @Override
    public void branchWillChange(@NotNull String s) {
        this.isBranchChanging = true;
    }

    @Override
    public void branchHasChanged(@NotNull String s) {
        this.isBranchChanging = false;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        if (isBranchChanging) return;

        // 提前缓存，避免对每个 event 重复查询 storage（EDT 性能优化）
        final String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        final List<String> assetDirList = AssetUtils.supportAssetFoldName(project);

        for (VFileEvent event : events) {
            if (event instanceof VFileCopyEvent) {
                VirtualFile virtualFile = ((VFileCopyEvent) event).findCreatedFile();
                if (isAssetFile(projectPath, assetDirList, virtualFile)) onAssetAdded(project, virtualFile);
            } else if (event instanceof VFileCreateEvent) {
                VirtualFile createFile = event.getFile();
                if (isAssetFile(projectPath, assetDirList, createFile)) onAssetAdded(project, createFile);
            } else if (event instanceof VFileMoveEvent) {
                String fileName = ((VFileMoveEvent) event).getFile().getName();
                VirtualFile oldParent = ((VFileMoveEvent) event).getOldParent();
                File oldFile = new File(oldParent.getPath(), fileName);
                VirtualFile newParent = ((VFileMoveEvent) event).getNewParent();
                VirtualFile newFile = newParent.findChild(fileName);
                if (isAssetFile(projectPath, assetDirList, newFile)) onAssetMove(project, oldFile, newFile);
            } else if (event instanceof VFileDeleteEvent) {
                VirtualFile deleteFile = event.getFile();
                if (isAssetFile(projectPath, assetDirList, deleteFile)) onAssetDeleted(project, deleteFile);
            } else if (event instanceof VFileContentChangeEvent) {
                VirtualFile file = event.getFile();
                if (PubSpecUtils.isRootPubSpecFile(project, file)) {
                    onPubSpecFileChanged(project, file);
                } else if (isAssetFile(projectPath, assetDirList, file)) {
                    onAssetContentChanged(project, file);
                }
            } else if (event instanceof VFilePropertyChangeEvent) {
                String propertyName = ((VFilePropertyChangeEvent) event).getPropertyName();
                if (Objects.equals(VirtualFile.PROP_NAME, propertyName)) {
                    VirtualFile changedFile = event.getFile();
                    if (isAssetFile(projectPath, assetDirList, changedFile)) {
                        String oldValue = (String) ((VFilePropertyChangeEvent) event).getOldValue();
                        String newValue = (String) ((VFilePropertyChangeEvent) event).getNewValue();
                        onAssetNameChanged(project, changedFile, oldValue, newValue);
                    }
                }
            }
        }
    }

    /**
     * 使用预先缓存的 projectPath 和 assetDirList 判断文件是否为 asset，
     * 避免在每次 event 处理中重复访问 storage。
     */
    private static boolean isAssetFile(@NotNull String projectPath,
                                        @NotNull List<String> assetDirList,
                                        @Nullable VirtualFile virtualFile) {
        if (virtualFile == null) return false;
        if (virtualFile.isDirectory()) return false;
        String name = virtualFile.getName();
        if (name.startsWith(".")) return false;
        String filePath = virtualFile.getPath();
        for (String dirName : assetDirList) {
            String assetDirPath = projectPath + "/" + dirName;
            if (filePath.startsWith(assetDirPath)) return true;
        }
        return false;
    }

    public void onAssetAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
    }

    public void onAssetMove(@NotNull Project project, @NotNull File oldFile, @NotNull VirtualFile newFile) {
    }

    public void onAssetDeleted(@NotNull Project project, @NotNull VirtualFile deleteFile) {
    }

    public void onAssetNameChanged(@NotNull Project project, @NotNull VirtualFile virtualFile,
                                   @NotNull String oldName, @NotNull String newName) {
    }

    public void onAssetContentChanged(@NotNull Project project, @NotNull VirtualFile file) {
    }

    public void onPubSpecFileChanged(@NotNull Project project, @NotNull VirtualFile file) {
    }
}
