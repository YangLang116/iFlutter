package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import io.flutter.sdk.FlutterSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class DartUtils {

    public static String getFlutterPath(@NotNull Project project) {
        FlutterSdk flutterSdk = FlutterSdk.getFlutterSdk(project);
        if (flutterSdk != null) return flutterSdk.getHomePath();
        String projectPath = project.getBasePath();
        File configFile = new File(projectPath, "android" + File.separator + "local.properties");
        if (!configFile.exists()) return null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty("flutter.sdk");
        } catch (Exception e) {
            LogUtils.error("DartUtils getFlutterPath", e);
            return null;
        } finally {
            CloseUtils.close(inputStream);
        }
    }

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
        return FileUtils.isChildPath(flutterPath, dartFile.getPath());
    }

    public static void createDartFile(@NotNull Project project,
                                      @NotNull VirtualFile parentDir,
                                      @NotNull String fileName,
                                      @NotNull String fileContent) {
        createDartFile(project, parentDir, fileName, fileContent, null);
    }


    public static void createDartFile(@NotNull Project project,
                                      @NotNull VirtualFile parentDir,
                                      @NotNull String fileName,
                                      @NotNull String fileContent,
                                      @Nullable DartUtils.OnFileCreatedListener listener) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiDirectory parentPsiDir = psiManager.findDirectory(parentDir);
            if (parentPsiDir == null) return;
            PsiFile existPsiFile = parentPsiDir.findFile(fileName);
            if (existPsiFile == null) {
                addNewFile(project, parentPsiDir, fileName, fileContent, listener);
            } else {
                updateFileContent(project, existPsiFile, fileContent, listener);
            }
        });
    }

    private static void addNewFile(@NotNull Project project,
                                   @NotNull PsiDirectory directory,
                                   @NotNull String fileName,
                                   @NotNull String fileContent,
                                   @Nullable DartUtils.OnFileCreatedListener listener) {
        FileTypeRegistry typeRegistry = FileTypeRegistry.getInstance();
        FileType fileType = typeRegistry.getFileTypeByFileName(fileName);
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile newFile = fileFactory.createFileFromText(fileName, fileType, fileContent);
        CodeStyleManager.getInstance(project).reformat(newFile);
        directory.add(newFile);
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(newFile);
        if (document != null) {
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        }
        if (listener != null) {
            Application application = ApplicationManager.getApplication();
            application.invokeLater(listener::onFinish);
        }
    }

    private static void updateFileContent(@NotNull Project project,
                                          @NotNull PsiFile file,
                                          @NotNull String fileContent,
                                          @Nullable DartUtils.OnFileCreatedListener listener) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(file);
        if (document == null) return;
        document.setText(fileContent);
        psiDocumentManager.commitDocument(document);
        CodeStyleManager.getInstance(project).reformat(file);
        if (listener != null) {
            Application application = ApplicationManager.getApplication();
            application.invokeLater(listener::onFinish);
        }
    }

    public interface OnFileCreatedListener {
        void onFinish();
    }
}
