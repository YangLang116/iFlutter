package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class PubSpecFileHandler {

    void addAsset(@NotNull Project project, String assetName) {
        LogUtils.info("PubSpecFileHandler addAsset: " + assetName);
        PubspecUtils.readAssetAtReadAction(project, assetList -> {
            assetList.add(assetName);
            PubspecUtils.writeAssetAtWriteAction(project, assetList);
        });
    }

    void removeAsset(@NotNull Project project, String assetName) {
        LogUtils.info("PubSpecFileHandler removeAsset: " + assetName);
        PubspecUtils.readAssetAtReadAction(project, assetList -> {
            assetList.remove(assetName);
            PubspecUtils.writeAssetAtWriteAction(project, assetList);
        });
    }

    void changeAsset(Project project, String oldAssetName, String newAssetName) {
        LogUtils.info(String.format(Locale.ROOT, "PubSpecFileHandler changeAsset(%s -> %s)", oldAssetName, newAssetName));
        PubspecUtils.readAssetAtReadAction(project, assetList -> {
            assetList.remove(oldAssetName);
            assetList.add(newAssetName);
            PubspecUtils.writeAssetAtWriteAction(project, assetList);
        });
    }


    public void onPsiFileChanged(PsiFile psiFile) {
        if (psiFile == null) return;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;
        String fileName = virtualFile.getName();
        if (!PubspecUtils.getFileName().equals(fileName)) return;
        Project project = psiFile.getProject();
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        PubspecUtils.readAssetAtReadAction(project, assetList -> DartRFileGenerator.getInstance().generate(project, assetList));
    }

}
