package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.component.packages.update.FlutterPackageUpdater;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.FileUtils;
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
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VFileEvent event : events) {
            if (event instanceof VFileCopyEvent) {
                VirtualFile virtualFile = ((VFileCopyEvent) event).findCreatedFile();
                PsiFile psiFile = FileUtils.vf2PsiFile(psiManager, virtualFile);
                if (psiFile != null) onPsiFileAdded(psiFile);
                continue;
            }
            PsiFile psiFile = FileUtils.vf2PsiFile(psiManager, event.getFile());
            if (psiFile == null) continue;
            if (event instanceof VFileCreateEvent) {
                onPsiFileAdded(psiFile);
            } else if (event instanceof VFileDeleteEvent) {
                onPsiFileDeleted(psiFile);
            } else if (event instanceof VFilePropertyChangeEvent) {
                String propertyName = ((VFilePropertyChangeEvent) event).getPropertyName();
                if (Objects.equals(VirtualFile.PROP_NAME, propertyName)) {
                    String oldValue = (String) ((VFilePropertyChangeEvent) event).getOldValue();
                    String newValue = (String) ((VFilePropertyChangeEvent) event).getNewValue();
                    onPsiFilePropertyChanged(psiFile, oldValue, newValue);
                }
            } else if (event instanceof VFileContentChangeEvent) {
                onPsiFileContentChanged(psiFile);
            }
        }
    }

    private void onPsiFileAdded(@NotNull PsiFile psiFile) {
        this.imageSizeAnalyzer.onPsiFileAdd(psiFile);
        if (disableResCheck()) return;
        this.assetFileHandler.onPsiFileAdded(psiFile);
    }

    private void onPsiFileDeleted(@NotNull PsiFile psiFile) {
        if (disableResCheck()) return;
        this.assetFileHandler.onPsiFileRemoved(psiFile);
    }

    private void onPsiFilePropertyChanged(@NotNull PsiFile psiFile,
                                          String oldName,
                                          String newName) {
        if (disableResCheck()) return;
        this.assetFileHandler.onPsiFileChanged(psiFile, oldName, newName);
    }

    private void onPsiFileContentChanged(@NotNull PsiFile psiFile) {
        if (PubspecUtils.isRootPubspecFile((psiFile))) {
            this.packageUpdater.postPullLatestVersion();
            if (disableResCheck()) return;
            this.specFileHandler.onPsiFileChanged(psiFile);
        }
    }
}
