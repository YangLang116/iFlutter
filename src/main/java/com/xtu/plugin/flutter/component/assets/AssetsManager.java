package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

/**
 * 处理flutter项目中的资源文件
 */
public class AssetsManager implements BulkFileListener {

    private final Project project;
    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;
    private final ImageSizeAnalyzer imageSizeAnalyzer;

    public AssetsManager(@NotNull Project project) {
        this.project = project;
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

    private boolean enableResCheck() {
        return StorageService.getInstance(project).getState().resCheckEnable;
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
                String fileName = ((VFileMoveEvent) event).getFile().getName();
                VirtualFile oldParent = ((VFileMoveEvent) event).getOldParent();
                File oldFile = new File(oldParent.getPath(), fileName);
                VirtualFile newParent = ((VFileMoveEvent) event).getNewParent();
                VirtualFile newFile = newParent.findChild(fileName);
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
        if (enableResCheck()) {
            this.assetFileHandler.onFileAdded(project, virtualFile);
        }
    }

    private void onFileDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck()) {
            this.assetFileHandler.onFileDeleted(project, virtualFile);
        }
    }

    private void onFilePropertyChanged(@NotNull Project project,
                                       @NotNull VirtualFile virtualFile,
                                       @NotNull String oldName,
                                       @NotNull String newName) {
        if (enableResCheck()) {
            this.assetFileHandler.onFileChanged(project, virtualFile, oldName, newName);
        }
    }

    private void onFileMove(@NotNull Project project,
                            @NotNull File oldFile,
                            @NotNull VirtualFile newFile) {
        if (enableResCheck()) {
            this.assetFileHandler.onFileMoved(project, oldFile, newFile);
        }
    }

    private void onFileContentChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (PubspecUtils.isRootPubspecFile(project, virtualFile)) {
            if (enableResCheck()) {
                this.specFileHandler.onPsiFileChanged(project);
            }
        }
    }
}
