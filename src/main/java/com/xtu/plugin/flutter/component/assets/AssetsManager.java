package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.BranchChangeListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBusConnection;
import com.xtu.plugin.flutter.base.adapter.BulkFileAdapter;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.store.StorageEntity;
import com.xtu.plugin.flutter.store.StorageService;
import com.xtu.plugin.flutter.utils.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 处理flutter项目中的资源文件
 */
public class AssetsManager extends BulkFileAdapter implements BranchChangeListener, Disposable {

    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;
    private final ImageSizeAnalyzer imageSizeAnalyzer;

    public AssetsManager(@NotNull Project project) {
        super(project);
        this.specFileHandler = new PubSpecFileHandler();
        this.assetFileHandler = new AssetFileHandler(specFileHandler);
        this.imageSizeAnalyzer = new ImageSizeAnalyzer(project);
    }

    public static AssetsManager getService(@NotNull Project project) {
        return project.getService(AssetsManager.class);
    }

    public void attach() {
        LogUtils.info("AssetsManager attach");
        if (!PluginUtils.isFlutterProject(project)) return;
        MessageBusConnection connect = project.getMessageBus().connect();
        connect.subscribe(VirtualFileManager.VFS_CHANGES, this);
        connect.subscribe(BranchChangeListener.VCS_BRANCH_CHANGED, this);
    }

    public void detach() {
        LogUtils.info("AssetsManager detach");
    }

    @Override
    public void onFileAdded(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileAdded(project, virtualFile);
        }
        if (enableSizeCheck(project)) {
            this.imageSizeAnalyzer.analysis(virtualFile);
        }
        if (canTinyImage(project, virtualFile)) {
            TinyUtils.showTinyDialog(project, virtualFile);
        }
    }

    @Override
    public void onFileMove(@NotNull Project project,
                           @NotNull File oldFile,
                           @NotNull VirtualFile newFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileMoved(project, oldFile, newFile);
        }
    }

    @Override
    public void onFileDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileDeleted(project, virtualFile);
        }
    }

    @Override
    public void onFileNameChanged(@NotNull Project project,
                                  @NotNull VirtualFile virtualFile,
                                  @NotNull String oldName,
                                  @NotNull String newName) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileChanged(project, virtualFile, oldName, newName);
        }
    }

    @Override
    public void onFileContentChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (PubSpecUtils.isRootPubSpecFile(project, virtualFile)) {
            if (enableResCheck(project)) {
                this.specFileHandler.onPsiFileChanged(project);
            }
        }
    }

    @Override
    public void branchWillChange(@NotNull String s) {
        setBulkFileEnable(false);
    }

    @Override
    public void branchHasChanged(@NotNull String s) {
        setBulkFileEnable(true);
    }

    @Override
    public void dispose() {
        detach();
    }

    public static boolean canTinyImage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        StorageEntity state = StorageService.getInstance(project).getState();
        if (!state.autoTinyImage) return false;
        if (StringUtils.isEmpty(state.tinyApiKey)) return false;
        return TinyUtils.isSupport(virtualFile);
    }

    public static boolean enableSizeCheck(@NotNull Project project) {
        return StorageService.getInstance(project).getState().enableSizeMonitor;
    }

    public static boolean enableResCheck(@NotNull Project project) {
        return StorageService.getInstance(project).getState().resCheckEnable;
    }
}
