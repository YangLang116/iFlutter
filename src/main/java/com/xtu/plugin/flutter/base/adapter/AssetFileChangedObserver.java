package com.xtu.plugin.flutter.base.adapter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.util.messages.MessageBusConnection;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class AssetFileChangedObserver implements BulkFileListener, BranchChangeListener {

    public final Project project;
    private boolean isBranchChanging = false;

    public AssetFileChangedObserver(@NotNull Project project) {
        this.project = project;
    }

    public void bindFileChangedEvent() {
        MessageBusConnection connect = project.getMessageBus().connect();
        connect.subscribe(VirtualFileManager.VFS_CHANGES, this);
        connect.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, this);
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
        for (VFileEvent event : events) {
            if (event instanceof VFileCopyEvent) {
                VirtualFile virtualFile = ((VFileCopyEvent) event).findCreatedFile();
                if (AssetUtils.isAssetFile(project, virtualFile)) onAssetAdded(project, virtualFile);
            } else if (event instanceof VFileCreateEvent) {
                VirtualFile createFile = event.getFile();
                if (AssetUtils.isAssetFile(project, createFile)) onAssetAdded(project, createFile);
            } else if (event instanceof VFileMoveEvent) {
                String fileName = ((VFileMoveEvent) event).getFile().getName();
                VirtualFile oldParent = ((VFileMoveEvent) event).getOldParent();
                File oldFile = new File(oldParent.getPath(), fileName);
                VirtualFile newParent = ((VFileMoveEvent) event).getNewParent();
                VirtualFile newFile = newParent.findChild(fileName);
                if (AssetUtils.isAssetFile(project, newFile)) onAssetMove(project, oldFile, newFile);
            } else if (event instanceof VFileDeleteEvent) {
                VirtualFile deleteFile = event.getFile();
                if (AssetUtils.isAssetFile(project, deleteFile)) onAssetDeleted(project, deleteFile);
            } else if (event instanceof VFileContentChangeEvent) {
                VirtualFile file = event.getFile();
                if (PubSpecUtils.isRootPubSpecFile(project, file)) {
                    onPubSpecFileChanged(project, file);
                } else if (AssetUtils.isAssetFile(project, file)) {
                    onAssetContentChanged(project, file);
                }
            } else if (event instanceof VFilePropertyChangeEvent) {
                String propertyName = ((VFilePropertyChangeEvent) event).getPropertyName();
                if (Objects.equals(VirtualFile.PROP_NAME, propertyName)) {
                    VirtualFile changedFile = event.getFile();
                    if (AssetUtils.isAssetFile(project, changedFile)) {
                        String oldValue = (String) ((VFilePropertyChangeEvent) event).getOldValue();
                        String newValue = (String) ((VFilePropertyChangeEvent) event).getNewValue();
                        onAssetNameChanged(project, changedFile, oldValue, newValue);
                    }
                }
            }
        }
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
