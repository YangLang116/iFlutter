package com.xtu.plugin.flutter.utils;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

public class DartUtils {

    private static String flutterPath;

    public static String getFlutterPath(@NotNull Project project) {
        if (!StringUtils.isEmpty(flutterPath)) return flutterPath;
        String projectPath = project.getBasePath();
        File configFile = new File(projectPath, "android" + File.separator + "local.properties");
        if (!configFile.exists()) return null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            return flutterPath = properties.getProperty("flutter.sdk");
        } catch (Exception e) {
            LogUtils.error("DartUtils getFlutterPath: " + e.getMessage());
            return null;
        } finally {
            CloseUtils.close(inputStream);
        }
    }

    //格式化dart代码
    public static void format(Project project, VirtualFile virtualFile, OnFormatCompleteListener listener) {
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiFile> list = PsiUtilCore.toPsiFiles(psiManager, List.of(virtualFile));
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(
                project,
                PsiUtilCore.toPsiFileArray(list),
                () -> {
                    // 修复Dart插件`DartPostFormatProcessor` 仅同步document -psi，忽视了document - file的同步。
                    // 导致出现 Cache File Conflict 问题
                    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
                    Document cachedDocument = fileDocumentManager.getCachedDocument(virtualFile);
                    if (cachedDocument != null && fileDocumentManager.isDocumentUnsaved(cachedDocument)) {
                        fileDocumentManager.saveDocument(cachedDocument);
                    }
                    if (listener != null) listener.finishFormat(virtualFile);
                },
                false);
        processor.run();
    }

    public interface OnFormatCompleteListener {

        void finishFormat(@NotNull VirtualFile virtualFile);
    }

    //生成Dart类
    public static void createDartFile(Project project,
                                      VirtualFile parentDirectory,
                                      String fileName,
                                      String fileContent,
                                      OnCreateDartFileListener listener) {
        try {
            VirtualFile childFile = parentDirectory.findChild(fileName);
            if (childFile == null) {
                childFile = parentDirectory.createChildData(project, fileName);
            }
            childFile.setBinaryContent(fileContent.getBytes(StandardCharsets.UTF_8));
            format(project, childFile, virtualFile -> {
                if (listener != null) {
                    listener.onSuccess(virtualFile);
                }
            });
        } catch (Exception e) {
            LogUtils.error("DartUtils createDartFile: " + e.getMessage());
            if (listener != null) {
                listener.onFail(e.getMessage());
            }
        }
    }

    public interface OnCreateDartFileListener {

        void onSuccess(@NotNull VirtualFile virtualFile);

        void onFail(String message);

    }
}
