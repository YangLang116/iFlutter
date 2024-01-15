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
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.PubSpecUtils;
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
        this.imageSizeAnalyzer.onPsiFileAdd(project, virtualFile);
        if (AssetUtils.enableResCheck(project)) {
            this.assetFileHandler.onFileAdded(project, virtualFile);
        }
    }

    @Override
    public void onFileMove(@NotNull Project project,
                           @NotNull File oldFile,
                           @NotNull VirtualFile newFile) {
        if (AssetUtils.enableResCheck(project)) {
            this.assetFileHandler.onFileMoved(project, oldFile, newFile);
        }
    }

    @Override
    public void onFileDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (AssetUtils.enableResCheck(project)) {
            this.assetFileHandler.onFileDeleted(project, virtualFile);
        }
    }

    @Override
    public void onFileNameChanged(@NotNull Project project,
                                  @NotNull VirtualFile virtualFile,
                                  @NotNull String oldName,
                                  @NotNull String newName) {
        if (AssetUtils.enableResCheck(project)) {
            this.assetFileHandler.onFileChanged(project, virtualFile, oldName, newName);
        }
    }

    @Override
    public void onFileContentChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (PubSpecUtils.isRootPubSpecFile(project, virtualFile)) {
            if (AssetUtils.enableResCheck(project)) {
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
}
