package com.xtu.plugin.flutter.component.assets;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DoNotAskOption;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Alarm;
import com.xtu.plugin.flutter.base.adapter.AssetFileChangedObserver;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.TinyUtils;
import com.xtu.plugin.flutter.component.analysis.ImageSizeAnalyzer;
import com.xtu.plugin.flutter.component.assets.handler.AssetFileHandler;
import com.xtu.plugin.flutter.component.assets.handler.AssetMutation;
import com.xtu.plugin.flutter.component.assets.handler.PubSpecFileHandler;
import com.xtu.plugin.flutter.store.ide.IdeStorageService;
import com.xtu.plugin.flutter.store.project.AssetRegisterStorageService;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 处理 flutter 项目中的资源文件。
 *
 * <p>批量写入优化：通过 {@code assetAlarm} 将 300ms 内的多次 asset 变更（新增/删除/移动/重命名）
 * 合并为一次 pubspec.yaml 读-改-写，解决批量文件操作（如复制多张图片、git checkout）
 * 时 pubspec.yaml 被反复读写的性能问题。</p>
 */
public class AssetsManager extends AssetFileChangedObserver implements Disposable {

    private static final int ASSET_FLUSH_DELAY_MS = 300;

    private final AssetFileHandler assetFileHandler;
    private final PubSpecFileHandler specFileHandler;
    private final ImageSizeAnalyzer imageSizeAnalyzer;
    /** 防抖：pubspec.yaml 外部变更（如编辑器直接修改） */
    private final Alarm pubSpecAlarm;
    /** 防抖：asset 文件增删改移，批量合并后一次性写 pubspec.yaml */
    private final Alarm assetAlarm;
    /** 待刷新的 mutation 队列，在 EDT 上操作，无需同步 */
    private final List<AssetMutation> pendingAssetMutations = new ArrayList<>();

    private AssetsManager(@NotNull Project project) {
        super(project);
        this.specFileHandler = new PubSpecFileHandler();
        this.assetFileHandler = new AssetFileHandler(specFileHandler);
        this.imageSizeAnalyzer = new ImageSizeAnalyzer(project);
        this.pubSpecAlarm = new Alarm(this);
        this.assetAlarm = new Alarm(this);
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
        disconnect();
    }

    // ---- VFS 事件处理 ----

    @Override
    public void onAssetAdded(@NotNull Project project, @NotNull VirtualFile addFile) {
        if (enableResCheck(project)) {
            enqueueAndSchedule(assetFileHandler.computeAddMutation(project, addFile));
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
            enqueueAndSchedule(assetFileHandler.computeMoveMutation(project, oldFile, newFile));
        }
    }

    @Override
    public void onAssetDeleted(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (enableResCheck(project)) {
            enqueueAndSchedule(assetFileHandler.computeDeleteMutation(project, virtualFile));
        }
    }

    @Override
    public void onAssetNameChanged(@NotNull Project project,
                                   @NotNull VirtualFile virtualFile,
                                   @NotNull String oldName,
                                   @NotNull String newName) {
        if (enableResCheck(project)) {
            enqueueAndSchedule(assetFileHandler.computeRenameMutation(project, virtualFile, oldName, newName));
        }
    }

    @Override
    public void onPubSpecFileChanged(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        pubSpecAlarm.cancelAllRequests();
        pubSpecAlarm.addRequest(() -> {
            PluginUtils.invalidateProjectCache(project);
            if (enableResCheck(project)) {
                this.specFileHandler.onPsiFileChanged(project);
            }
        }, ASSET_FLUSH_DELAY_MS);
    }

    @Override
    public void branchHasChanged(@NotNull String s) {
        super.branchHasChanged(s);
        AssetRegisterStorageService.getService(project).refreshIfNeed();
    }

    // ---- 批量刷新 ----

    /**
     * 将一个 mutation 加入队列，并重置防抖计时器。
     * 300ms 内无新增变更时，统一执行一次 pubspec.yaml 读写。
     */
    private void enqueueAndSchedule(@Nullable AssetMutation mutation) {
        if (mutation == null) return;
        pendingAssetMutations.add(mutation);
        assetAlarm.cancelAllRequests();
        assetAlarm.addRequest(this::flushAssetMutations, ASSET_FLUSH_DELAY_MS);
    }

    private void flushAssetMutations() {
        if (pendingAssetMutations.isEmpty()) return;
        List<AssetMutation> mutations = new ArrayList<>(pendingAssetMutations);
        pendingAssetMutations.clear();
        LogUtils.info("AssetsManager flushAssetMutations: " + mutations.size() + " ops");
        specFileHandler.flushMutations(project, mutations);
    }

    // ---- 辅助方法 ----

    public boolean enableSizeCheck(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).enableSizeMonitor;
    }

    public boolean enableResCheck(@NotNull Project project) {
        return ProjectStorageService.getStorage(project).resCheckEnable;
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

    private void showTinyDialog(@NotNull Project project, @NotNull VirtualFile addFile) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String title = "Compress Image";
            String message = String.format("Do you need to compress '%s' ?", addFile.getName());
            if (System.currentTimeMillis() - remindOptionTime <= 30 * 1000) {
                if (remindOptionResult) doCompress(project, addFile);
                return;
            }
            MessageDialogBuilder.okCancel(title, message).noText("Cancel").yesText("Compress")
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
                            boolean isOK = exitCode == 0;
                            if (isSelected) {
                                remindOptionTime = System.currentTimeMillis();
                                remindOptionResult = isOK;
                            }
                            if (isOK) doCompress(project, addFile);
                        }
                    }).ask(project);
        });
    }

    private void doCompress(@NotNull Project project, @NotNull VirtualFile addFile) {
        List<File> fileList = Collections.singletonList(new File(addFile.getPath()));
        TinyUtils.compressImage(project, fileList, null);
    }
}
