package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.YamlUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Locale;

public class PubSpecFileHandler {

    void addAsset(@NotNull Project project, String assetName) {
        LogUtils.info("PubSpecFileHandler addAsset: " + assetName);
        YamlUtils.loadFromYaml(project, (yaml, data, assetList) -> {
            assetList.add(assetName);
            Collections.sort(assetList);
            YamlUtils.writeToYaml(project, yaml, data, assetList);
        });
    }

    void removeAsset(@NotNull Project project, String assetName) {
        LogUtils.info("PubSpecFileHandler removeAsset: " + assetName);
        YamlUtils.loadFromYaml(project, (yaml, data, assetList) -> {
            assetList.remove(assetName);
            YamlUtils.writeToYaml(project, yaml, data, assetList);
        });
    }

    void changeAsset(Project project, String oldAssetName, String newAssetName) {
        LogUtils.info(String.format(Locale.ROOT, "PubSpecFileHandler changeAsset(%s -> %s)", oldAssetName, newAssetName));
        YamlUtils.loadFromYaml(project, (yaml, data, assetList) -> {
            assetList.remove(oldAssetName);
            assetList.add(newAssetName);
            Collections.sort(assetList);
            YamlUtils.writeToYaml(project, yaml, data, assetList);
        });
    }


    public void onPsiFileChanged(PsiFile psiFile) {
        if (psiFile == null) return;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;
        String fileName = virtualFile.getName();
        if (!YamlUtils.getFileName().equals(fileName)) return;
        Project project = psiFile.getProject();
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        YamlUtils.loadFromYaml(project, (yaml, data, assetList) -> DartRFileGenerator.getInstance().generate(project, assetList));
    }

}
