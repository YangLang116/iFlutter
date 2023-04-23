package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.tinify.Tinify;
import com.xtu.plugin.flutter.configuration.SettingsConfiguration;
import com.xtu.plugin.flutter.service.StorageService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TinyUtils {

    private static final List<String> SUPPORT_COMPRESS_FORMAT = Arrays.asList("jpg", "jpeg", "png", "webp");

    public static boolean isSupport(@NotNull File imageFile) {
        String extension = FileUtils.getExtension(imageFile);
        if (StringUtils.isEmpty(extension)) return false;
        return SUPPORT_COMPRESS_FORMAT.contains(extension);
    }

    public static void compressImage(@NotNull Project project,
                                     @NotNull List<File> imageFileList,
                                     @NotNull OnReloadListener onReloadListener) {
        final String tinyKey = StorageService.getInstance(project).getState().tinyApiKey;
        if (StringUtils.isEmpty(tinyKey)) {
            ToastUtil.make(project, MessageType.INFO, "add api key for TinyPng");
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
                    ToastUtil.make(project, MessageType.INFO, "compress image success");
                    Application application = ApplicationManager.getApplication();
                    application.invokeLater(() -> onReloadListener.reload(imageFileList));
                } catch (Exception e) {
                    LogUtils.error("ResMenuHelper compressImage: " + e.getMessage());
                    ToastUtil.make(project, MessageType.ERROR, "compress image fail: " + e.getMessage());
                } finally {
                    indicator.setIndeterminate(false);
                    indicator.setFraction(1);
                }
            }
        }.queue();
    }

    public interface OnReloadListener {

        void reload(@NotNull List<File> changeFileList);

    }
}
