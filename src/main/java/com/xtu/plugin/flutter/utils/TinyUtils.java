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
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

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
                                     @Nullable TinyUtils.OnCompressListener listener) {
        final String tinyKey = ProjectStorageService.getInstance(project).getState().tinyApiKey;
        if (StringUtils.isEmpty(tinyKey)) {
            int result = Messages.showYesNoDialog(
                    project,
                    "You must configure an apiKey for TinyPng",
                    "", "Configure", "Cancel",
                    Messages.getErrorIcon());
            if (result == Messages.YES) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfiguration.class);
            }
            return;
        }
        Tinify.setKey(tinyKey);
        new Task.Backgroundable(project, "compressing...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                int fileCount = imageFileList.size();
                List<File> successList = new ArrayList<>();
                try {
                    for (int i = 0; i < fileCount; i++) {
                        File file = imageFileList.get(i);
                        String message = String.format(Locale.ROOT, "compressing %s [%d/%d]", file.getName(), i + 1, fileCount);
                        indicator.setText(message);
                        String imageFilePath = file.getAbsolutePath();
                        Tinify.fromFile(imageFilePath).toFile(imageFilePath);
                        successList.add(file);
                    }
                    ToastUtils.make(project, MessageType.INFO, "compress image success");
                } catch (Exception e) {
                    LogUtils.error("TinyUtils compressImage", e);
                    ToastUtils.make(project, MessageType.ERROR, "compress image fail: " + e.getMessage());
                } finally {
                    postCallBack(successList);
                    indicator.setIndeterminate(false);
                    indicator.setFraction(1);
                }
            }

            private void postCallBack(@NotNull List<File> successList) {
                if (listener == null) return;
                Application application = ApplicationManager.getApplication();
                application.invokeLater(() -> listener.onFinish(successList));
            }
        }.queue();
    }

    public static void showTinyDialog(@NotNull Project project,
                                      @NotNull VirtualFile virtualFile) {
        ApplicationManager.getApplication().invokeLater(() -> {
            String content = String.format(Locale.ROOT, "Do you need to compress %s ?", virtualFile.getName());
            int result = Messages.showYesNoDialog(project, content, "", "OK", "Cancel", Messages.getInformationIcon());
            if (result != Messages.YES) return;
            List<File> fileList = Collections.singletonList(new File(virtualFile.getPath()));
            compressImage(project, fileList, null);
        });
    }

    public interface OnCompressListener {
        void onFinish(@NotNull List<File> successList);
    }
}
