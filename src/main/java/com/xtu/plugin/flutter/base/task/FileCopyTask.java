package com.xtu.plugin.flutter.base.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class FileCopyTask extends Task.Backgroundable {

    private final Project project;
    private final File sourceDir;
    private final File destinationDir;

    public FileCopyTask(@Nullable Project project, @NlsContexts.ProgressTitle @NotNull String title, @NotNull File sourceDir, @NotNull File destinationDir) {
        super(project, title);
        this.project = project;
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        try {
            FileUtils.copyDirectoryToDirectory(sourceDir, destinationDir);
            refreshDirectory();
        } catch (Exception e) {
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private void refreshDirectory() {
        ApplicationManager.getApplication().invokeLater(() -> {
            File targetDir = new File(destinationDir, sourceDir.getName());
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(targetDir);
            ToastUtils.make(project, MessageType.INFO, "Copy Success");
        });
    }
}
