package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class DartUtils {

    public static boolean isBuiltInType(@NotNull DartReferenceExpression referenceExpression) {
        Project project = referenceExpression.getProject();
        String flutterPath = getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return false;
        PsiReference reference = referenceExpression.getReference();
        if (reference == null) return false;
        PsiElement resolvePsiElement = reference.resolve();
        PsiFile filePsi = PsiTreeUtil.getParentOfType(resolvePsiElement, PsiFile.class);
        if (filePsi == null) return false;
        VirtualFile dartFile = filePsi.getVirtualFile();
        if (dartFile == null) return false;
        return PathUtils.isChildPath(flutterPath, dartFile.getPath());
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
            LogUtils.error("DartUtils getFlutterPath", e);
            return null;
        } finally {
            CloseUtils.close(inputStream);
        }
    }

    //生成Dart类
    public static void createDartFile(@NotNull Project project,
                                      @NotNull VirtualFile parentDirectory,
                                      @NotNull String fileName,
                                      @NotNull String fileContent,
                                      @Nullable OnDartFileCreateListener listener) {
        DumbService.getInstance(project).smartInvokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiDirectory psiDirectory = psiManager.findDirectory(parentDirectory);
            assert psiDirectory != null;
            PsiFile oldFile = psiDirectory.findFile(fileName);
            if (oldFile != null) oldFile.delete();
            FileType type = FileTypeRegistry.getInstance().getFileTypeByFileName(fileName);
            PsiFile tempFile = PsiFileFactory.getInstance(project).createFileFromText(fileName, type, fileContent);
            PsiFile newFile = (PsiFile) psiDirectory.add(CodeStyleManager.getInstance(project).reformat(tempFile));
            if (listener != null) {
                VirtualFile virtualFile = newFile.getVirtualFile();
                ApplicationManager.getApplication().invokeLater(() -> listener.onFinish(virtualFile));
            }
        }));
    }

    public interface OnDartFileCreateListener {
        void onFinish(@NotNull VirtualFile virtualFile);
    }
}
