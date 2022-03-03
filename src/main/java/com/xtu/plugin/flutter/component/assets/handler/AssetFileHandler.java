package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.FileUtils;
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
        specFileHandler.addAsset(psiFile.getProject(), List.of(assetPath));
    }

    public void onPsiFileRemoved(PsiFile psiFile) {
        String assetPath = getAssetFilePath(psiFile);
        if (assetPath == null) return;
        File assetFile = FileUtils.fromPsiFile(psiFile);
        String projectPath = PluginUtils.getProjectPath(psiFile.getProject());
        //判断是否还存在其他dimension资源,兼容IDEA自动引用处理，需要强制保留assetPath
        if (AssetUtils.hasOtherDimensionAsset(projectPath, assetFile)) {
            specFileHandler.addAsset(psiFile.getProject(), List.of(assetPath));
        } else {
            specFileHandler.removeAsset(psiFile.getProject(), assetPath);
        }
    }

    public void onPsiFileChanged(PsiFile psiFile, String oldValue, String newValue) {
        String newAssetPath = getAssetFilePath(psiFile);
        if (newAssetPath == null) return;
        String projectPath = PluginUtils.getProjectPath(psiFile.getProject());
        File newAssetFile = FileUtils.fromPsiFile(psiFile);
        assert newAssetFile != null;
        File oldAssetFile = new File(newAssetFile.getParentFile(), oldValue);
        String oldAssetPath = newAssetPath.replace(newValue, oldValue);
        //判断是否还存在其他dimension资源,兼容IDEA自动引用处理，需要强制保留oldAssetPath
        if (AssetUtils.hasOtherDimensionAsset(projectPath, oldAssetFile)) {
            specFileHandler.addAsset(psiFile.getProject(), List.of(newAssetPath, oldAssetPath));
        } else {
            specFileHandler.changeAsset(psiFile.getProject(), oldAssetPath, newAssetPath);
        }
    }

    private String getAssetFilePath(PsiFile psiFile) {
        if (isAssetFile(psiFile)) {
            final Project project = psiFile.getProject();
            String projectPath = PluginUtils.getProjectPath(project);
            File assetFile = FileUtils.fromPsiFile(psiFile);
            return AssetUtils.getAssetPath(projectPath, assetFile);
        }
        return null;
    }

    private boolean isAssetFile(PsiFile psiFile) {
        if (psiFile == null) return false;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null || virtualFile.isDirectory()) return false;
        if (virtualFile.getName().startsWith(".")) return false;
        String filePath = virtualFile.getPath();
        Project project = psiFile.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        List<String> supportAssetFoldName = PluginUtils.supportAssetFoldName(project);
        for (String directoryName : supportAssetFoldName) {
            String assetPrefixName = projectPath + "/" + directoryName;
            if (filePath.startsWith(assetPrefixName)) {
                return true;
            }
        }
        return false;
    }
}
