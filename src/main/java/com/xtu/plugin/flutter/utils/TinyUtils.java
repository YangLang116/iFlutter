package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.tinify.Tinify;
import com.xtu.plugin.flutter.configuration.SettingsConfiguration;
import com.xtu.plugin.flutter.store.StorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TinyUtils {

    private static final List<String> SUPPORT_COMPRESS_FORMAT = Arrays.asList("jpg", "jpeg", "png", "webp");

    public static boolean isSupport(@NotNull VirtualFile virtualFile) {
        File file = new File(virtualFile.getPath());
        return isSupport(file);
    }

    public static boolean isSupport(@NotNull File imageFile) {
        String extension = FileUtils.getExtension(imageFile);
        if (StringUtils.isEmpty(extension)) return false;
        return SUPPORT_COMPRESS_FORMAT.contains(extension);
    }

    public static void compressImage(@NotNull Project project,
                                     @NotNull List<File> imageFileList,
                                     @Nullable OnResultListener onResultListener) {
        final String tinyKey = StorageService.getInstance(project).getState().tinyApiKey;
        if (StringUtils.isEmpty(tinyKey)) {
            ToastUtils.make(project, MessageType.INFO, "add api key for TinyPng");
            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfiguration.class);
            return;
        }
        Tinify.setKey(tinyKey);
        new Task.Backgroundable(project, "compressing...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                int fileCount = imageFileList.size();
                try {
                    for (int i = 0; i < fileCount; i++) {
                        File imageFile = imageFileList.get(i);
                        String message = String.format(Locale.ROOT, "compressing %s [%d/%d]",
                                imageFile.getName(), i + 1, fileCount);
                        indicator.setText(message);
                        String imageFilePath = imageFile.getAbsolutePath();
                        Tinify.fromFile(imageFilePath).toFile(imageFilePath);
                    }
                    ToastUtils.make(project, MessageType.INFO, "compress image success");
                    postCallBack(true);
                } catch (Exception e) {
                    LogUtils.error("ResMenuHelper compressImage", e);
                    ToastUtils.make(project, MessageType.ERROR, "compress image fail: " + e.getMessage());
                    postCallBack(false);
                } finally {
                    indicator.setIndeterminate(false);
                    indicator.setFraction(1);
                }
            }

            private void postCallBack(boolean success) {
                if (onResultListener != null) {
                    Application application = ApplicationManager.getApplication();
                    application.invokeLater(() -> onResultListener.onFinish(success));
                }
            }
        }.queue();
    }

    public static void showTinyDialog(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String content = String.format(Locale.ROOT, "do you need to compress %s ?", virtualFile.getName());
        int result = Messages.showYesNoDialog(project, content, "Tiny Image", null);
        if (result != 0) return;
        List<File> fileList = Collections.singletonList(new File(virtualFile.getPath()));
        compressImage(project, fileList, null);
    }

    public interface OnResultListener {
        void onFinish(boolean success);
    }
}
