package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.component.packages.update.FlutterPackageUpdater;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * 处理flutter项目中的资源文件
 */
public class AssetsManager implements BulkFileListener {

    private final Project project;
    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;
    private final FlutterPackageUpdater packageUpdater;
    private final ImageSizeAnalyzer imageSizeAnalyzer;

    public AssetsManager(@NotNull Project project, @NotNull FlutterPackageUpdater packageUpdater) {
        this.project = project;
        this.packageUpdater = packageUpdater;
        this.specFileHandler = new PubSpecFileHandler();
        this.assetFileHandler = new AssetFileHandler(specFileHandler);
        this.imageSizeAnalyzer = new ImageSizeAnalyzer(project);
    }

    public void attach() {
        LogUtils.info("AssetsManager attach");
        project.getMessageBus().connect()
                .subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    public void detach() {
        LogUtils.info("AssetsManager detach");
    }

    private boolean disableResCheck() {
        return !StorageService.getInstance(project).getState()
                .resCheckEnable;
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
            if (event instanceof VFileCopyEvent) {
                VirtualFile virtualFile = ((VFileCopyEvent) event).findCreatedFile();
                if (virtualFile != null) onFileAdded(project, virtualFile);
                continue;
            }
            if (event instanceof VFileMoveEvent) {
                VirtualFile oldFile = ((VFileMoveEvent) event).getFile();
                VirtualFile newParent = ((VFileMoveEvent) event).getNewParent();
                VirtualFile newFile = newParent.findChild(oldFile.getName());
                if (newFile != null) onFileMove(project, oldFile, newFile);
                continue;
            }
            VirtualFile virtualFile = event.getFile();
            if (virtualFile == null) continue;
            if (event instanceof VFileCreateEvent) {
                onFileAdded(project, virtualFile);
            } else if (event instanceof VFileDeleteEvent) {
                onFileDeleted(project, virtualFile);
            } else if (event instanceof VFilePropertyChangeEvent) {
                String propertyName = ((VFilePropertyChangeEvent) event).getPropertyName();
                if (Objects.equals(VirtualFile.PROP_NAME, propertyName)) {
                    String oldValue = (String) ((VFilePropertyChangeEvent) event).getOldValue();
                    String newValue = (String) ((VFilePropertyChangeEvent) event).getNewValue();
                    onFilePropertyChanged(project, virtualFile, oldValue, newValue);
                }
            } else if (event instanceof VFileContentChangeEvent) {
                onFileContentChanged(project, virtualFile);
            }
        }
    }

    private void onFileAdded(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        this.imageSizeAnalyzer.onPsiFileAdd(project, virtualFile);
        if (disableResCheck()) return;
        this.assetFileHandler.onFileAdded(project, virtualFile);
    }

    private void onFileDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (disableResCheck()) return;
        this.assetFileHandler.onFileRemoved(project, virtualFile);
    }

    private void onFilePropertyChanged(@NotNull Project project,
                                       @NotNull VirtualFile virtualFile,
                                       @NotNull String oldName,
                                       @NotNull String newName) {
        if (disableResCheck()) return;
        this.assetFileHandler.onFileChanged(project, virtualFile, oldName, newName);
    }

    private void onFileMove(@NotNull Project project,
                            @NotNull VirtualFile oldFile,
                            @NotNull VirtualFile newFile) {
        if (disableResCheck()) return;
        this.assetFileHandler.onFileMoved(project, oldFile, newFile);
    }

    private void onFileContentChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (PubspecUtils.isRootPubspecFile(project, virtualFile)) {
            this.packageUpdater.postPullLatestVersion();
            if (disableResCheck()) return;
            this.specFileHandler.onPsiFileChanged(project);
        }
    }
}
