package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;

public class AssetFileHandler {

    private final PubSpecFileHandler specFileHandler;

    public AssetFileHandler(PubSpecFileHandler specFileHandler) {
        this.specFileHandler = specFileHandler;
    }

    public void onPsiFileAdded(PsiFile psiFile) {
        String assetPath = getAssetFilePath(psiFile);
        if (assetPath == null) return;
        specFileHandler.addAsset(psiFile.getProject(), assetPath);
    }

    public void onPsiFileRemoved(PsiFile psiFile) {
        String assetPath = getAssetFilePath(psiFile);
        if (assetPath == null) return;
        specFileHandler.removeAsset(psiFile.getProject(), assetPath);
    }

    public void onPsiFileChanged(PsiFile psiFile, String oldValue, String newValue) {
        String assetPath = getAssetFilePath(psiFile);
        if (assetPath == null) return;
        String oldAssetPath = assetPath.replace(newValue, oldValue);
        specFileHandler.changeAsset(psiFile.getProject(), oldAssetPath, assetPath);
    }

    private String getAssetFilePath(PsiFile psiFile) {
        if (psiFile == null) return null;
        Project project = psiFile.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return null;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null || virtualFile.isDirectory()) return null;
        String filePath = virtualFile.getPath();
        List<String> supportAssetFoldName = PluginUtils.supportAssetFoldName(project);
        for (String directoryName : supportAssetFoldName) {
            String assetPrefixName = projectPath + "/" + directoryName;
            if (filePath.startsWith(assetPrefixName)) {
                return filePath.substring(projectPath.length() + 1);
            }
        }
        return null;
    }
}
