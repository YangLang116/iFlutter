package com.xtu.plugin.flutter.base.adapter;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class BulkFileAdapter implements BulkFileListener {

    public final Project project;

    public BulkFileAdapter(@NotNull Project project) {
        this.project = project;
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
                    onFileNameChanged(project, virtualFile, oldValue, newValue);
                }
            } else if (event instanceof VFileContentChangeEvent) {
                onFileContentChanged(project, virtualFile);
            }
        }
    }

    public void onFileAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
    }

    public void onFileMove(@NotNull Project project, @NotNull File oldFile, @NotNull VirtualFile newFile) {
    }

    public void onFileDeleted(@NotNull Project project, @NotNull VirtualFile deleteFile) {
    }

    public void onFileContentChanged(@NotNull Project project, @NotNull VirtualFile file) {
    }

    public void onFileNameChanged(@NotNull Project project, @NotNull VirtualFile virtualFile,
                                  @NotNull String oldName, @NotNull String newName) {
    }

}
