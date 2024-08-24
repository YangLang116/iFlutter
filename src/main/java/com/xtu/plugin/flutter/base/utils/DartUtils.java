package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartReferenceExpression;
import io.flutter.sdk.FlutterSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
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
        try {
            VirtualFile targetFile = parentDir.findOrCreateChildData(project, fileName);
            targetFile.setBinaryContent(fileContent.getBytes(StandardCharsets.UTF_8));
            targetFile.refresh(false, false);
            PsiFile targetPsi = PsiManager.getInstance(project).findFile(targetFile);
            if (targetPsi == null) return;
            CodeStyleManager.getInstance(project).reformat(targetPsi);
            PsiUtils.savePsiFile(project, targetPsi);
            if (listener != null) {
                Application application = ApplicationManager.getApplication();
                application.invokeLater(() -> listener.onFinish(targetFile));
            }
        } catch (Exception e) {
            LogUtils.error("DartUtils createDartFile", e);
        }
    }

    public interface OnFileCreatedListener {

        void onFinish(@NotNull VirtualFile virtualFile);

    }
}
