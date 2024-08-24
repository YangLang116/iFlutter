package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DoNotAskOption;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.adapter.AssetFileChangedObserver;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.TinyUtils;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.store.ide.IdeStorageService;
import com.xtu.plugin.flutter.store.project.AssetRegisterStorageService;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * 处理flutter项目中的资源文件
 */
public class AssetsManager extends AssetFileChangedObserver implements Disposable {

    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;
    private final ImageSizeAnalyzer imageSizeAnalyzer;

    private AssetsManager(@NotNull Project project) {
        super(project);
        this.specFileHandler = new PubSpecFileHandler();
        this.assetFileHandler = new AssetFileHandler(specFileHandler);
        this.imageSizeAnalyzer = new ImageSizeAnalyzer(project);
    }

    public static AssetsManager getService(@NotNull Project project) {
        return project.getService(AssetsManager.class);
    }

    public void attach() {
        if (!PluginUtils.isFlutterProject(project)) return;
        LogUtils.info("AssetsManager attach");
        bindFileChangedEvent();
    }

    @Override
    public void dispose() {
        LogUtils.info("AssetsManager detach");
    }

    @Override
    public void onAssetAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileAdded(project, addFile);
        }
        if (enableSizeCheck(project)) {
            this.imageSizeAnalyzer.analysis(addFile);
        }
        if (canTinyImage(project, addFile)) {
            showTinyDialog(project, addFile);
        }
    }

    @Override
    public void onAssetMove(@NotNull Project project,
                            @NotNull File oldFile,
                            @NotNull VirtualFile newFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileMoved(project, oldFile, newFile);
        }
    }

    @Override
    public void onAssetDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileDeleted(project, virtualFile);
        }
    }

    @Override
    public void onAssetNameChanged(@NotNull Project project,
                                   @NotNull VirtualFile virtualFile,
                                   @NotNull String oldName,
                                   @NotNull String newName) {
        if (enableResCheck(project)) {
            this.assetFileHandler.onFileChanged(project, virtualFile, oldName, newName);
        }
    }

    @Override
    public void onPubSpecFileChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck(project)) {
            this.specFileHandler.onPsiFileChanged(project);
        }
    }

    @Override
    public void branchHasChanged(@NotNull String s) {
        super.branchHasChanged(s);
        AssetRegisterStorageService.getService(project).refreshIfNeed();
    }

    private boolean canTinyImage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String tinyApiKey = IdeStorageService.getStorage().tinyApiKey;
        if (StringUtils.isEmpty(tinyApiKey)) return false;
        ProjectStorageEntity state = ProjectStorageService.getStorage(project);
        if (!state.autoTinyImage) return false;
        return TinyUtils.isSupport(virtualFile);
    }

    private long remindOptionTime;
    private boolean remindOptionResult; //是否压缩

    private void showTinyDialog(@NotNull Project project,
                                @NotNull VirtualFile addFile) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String title = "Compress Image";
            String message = String.format("Do you need to compress '%s' ?", addFile.getName());
            boolean needCompress;
            if (System.currentTimeMillis() - remindOptionTime <= 30 * 1000) {
                needCompress = remindOptionResult;
            } else {
                needCompress = MessageDialogBuilder.okCancel(title, message)
                        .yesText("Compress")
                        .noText("Cancel")
                        .doNotAsk(new DoNotAskOption.Adapter() {
                            @Override
                            public boolean shouldSaveOptionsOnCancel() {
                                return true;
                            }

                            @Override
                            @NotNull
                            public String getDoNotShowMessage() {
                                return "No more reminders within 30s";
                            }

                            @Override
                            public void rememberChoice(boolean isSelected, int exitCode) {
                                if (!isSelected) return;
                                remindOptionTime = System.currentTimeMillis();
                                remindOptionResult = exitCode == 0;
                            }
                        })
                        .ask(project);
            }
            if (!needCompress) return;
            List<File> fileList = Collections.singletonList(new File(addFile.getPath()));
            TinyUtils.compressImage(project, fileList, null);
        });
    }

    public boolean enableSizeCheck(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).enableSizeMonitor;
    }

    public boolean enableResCheck(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).resCheckEnable;
    }
}
