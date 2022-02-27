package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
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
        //root package pubspec.yaml changed
        if (PubspecUtils.isRootPubspecFile(psiFile)) {
            LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
            final Project project = psiFile.getProject();
            PubspecUtils.readAssetAtReadAction(project, assetList ->
                    //psi event thread，need invoke late
                    ApplicationManager.getApplication().invokeLater(() -> DartRFileGenerator.getInstance().generate(project, assetList)));
        }
    }

}
