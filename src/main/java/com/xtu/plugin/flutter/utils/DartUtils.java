package com.xtu.plugin.flutter.utils;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * 采用DartFmt命令格式化，避免iFlutter依赖Dart Plugin
 */
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
    public static void format(Project project, File dartFile, OnFormatCompleteListener listener) {
        final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dartFile);
        if (virtualFile == null) return;
        PsiManager psiManager = PsiManager.getInstance(project);
        List<PsiFile> list = PsiUtilCore.toPsiFiles(psiManager, List.of(virtualFile));
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(
                project,
                PsiUtilCore.toPsiFileArray(list),
                () -> {
                    if (listener != null) listener.finishFormat(virtualFile);
                },
                false);
        processor.run();
    }

    public interface OnFormatCompleteListener {

        void finishFormat(@NotNull VirtualFile virtualFile);
    }
}
