package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;

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
        String oldAssetName = assetPath.replace(newValue, oldValue);
        specFileHandler.changeAsset(psiFile.getProject(), oldAssetName, assetPath);
    }

    private String getAssetFilePath(PsiFile psiFile) {
        if (psiFile == null) return null;
        String projectPath = psiFile.getProject().getBasePath();
        if (StringUtils.isEmpty(projectPath)) return null;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return null;
        String filePath = virtualFile.getPath();
        for (String directoryName : PluginUtils.getSupportAssetFoldName()) {
            String assetPrefixName = projectPath + File.separator + directoryName;
            if (filePath.startsWith(assetPrefixName)) {
                return filePath.substring(projectPath.length() + 1);
            }
        }
        return null;
    }
}
