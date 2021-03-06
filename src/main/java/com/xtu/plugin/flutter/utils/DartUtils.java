package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DartUtils {

    public static boolean isBuiltInType(String type) {
        return StringUtils.equals(type, "String")
                || StringUtils.equals(type, "bool")
                || StringUtils.equals(type, "int")
                || StringUtils.equals(type, "double")
                || StringUtils.equals(type, "num")
                || StringUtils.equals(type, "var")
                || StringUtils.equals(type, "dynamic")
                || StringUtils.equals(type, "Object");
    }

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
    private static void formatInWriteAction(Project project, VirtualFile virtualFile, OnFormatCompleteListener listener) {
        FileDocumentManager.getInstance().reloadFiles(virtualFile);
        PsiManager psiManager = PsiManager.getInstance(project);
        PsiFile psiFile = psiManager.findFile(virtualFile);
        if (psiFile != null) {
            CodeStyleManager.getInstance(project).reformat(psiFile);
        }
        listener.finishFormat(virtualFile);
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
        ApplicationManager.getApplication()
                .invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                    try {
                        VirtualFile childFile = parentDirectory.findChild(fileName);
                        if (childFile == null) {
                            childFile = parentDirectory.createChildData(project, fileName);
                        }
                        childFile.setBinaryContent(fileContent.getBytes(StandardCharsets.UTF_8));
                        formatInWriteAction(project, childFile, virtualFile -> {
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
                }));
    }

    public interface OnCreateDartFileListener {

        void onSuccess(@NotNull VirtualFile virtualFile);

        void onFail(String message);

    }
}
