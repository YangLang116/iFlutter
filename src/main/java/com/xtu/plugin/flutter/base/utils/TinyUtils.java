package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.tinify.Tinify;
import com.xtu.plugin.flutter.store.ide.IdeStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Tinify.setKey(IdeStorageService.getStorage().tinyApiKey);
        new Task.Backgroundable(project, "Compressing...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                int fileCount = imageFileList.size();
                List<File> successList = new ArrayList<>();
                try {
                    for (int i = 0; i < fileCount; i++) {
                        File file = imageFileList.get(i);
                        String message = String.format("compressing %s [%d/%d]", file.getName(), i + 1, fileCount);
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

    public interface OnCompressListener {
        void onFinish(@NotNull List<File> successList);
    }
}
