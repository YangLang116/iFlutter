package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.FontUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PubSpecFileHandler {

    void addAsset(@NotNull Project project, @NotNull List<String> addAssetList) {
        LogUtils.info("PubSpecFileHandler addAsset: " + addAssetList);
        PubspecUtils.readAsset(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            for (String asset : addAssetList) {
                if (FontUtils.isFontAsset(asset)) {
                    newFontList.add(asset);
                } else {
                    newAssetList.add(asset);
                }
            }
            PubspecUtils.writeAsset(project, newAssetList, newFontList);
        });
    }

    void removeAsset(@NotNull Project project, String removeAsset) {
        LogUtils.info("PubSpecFileHandler removeAsset: " + removeAsset);
        PubspecUtils.readAsset(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            if (FontUtils.isFontAsset(removeAsset)) {
                newFontList.remove(removeAsset);
            } else {
                newAssetList.remove(removeAsset);
            }
            PubspecUtils.writeAsset(project, newAssetList, newFontList);
        });
    }

    void changeAsset(Project project, String oldAssetName, String newAssetName) {
        LogUtils.info(String.format(Locale.ROOT, "PubSpecFileHandler changeAsset(%s -> %s)", oldAssetName, newAssetName));
        PubspecUtils.readAsset(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            if (FontUtils.isFontAsset(oldAssetName)) {
                newFontList.remove(oldAssetName);
            } else {
                newAssetList.remove(oldAssetName);
            }
            if (FontUtils.isFontAsset(newAssetName)) {
                newFontList.add(newAssetName);
            } else {
                newAssetList.add(newAssetName);
            }
            PubspecUtils.writeAsset(project, newAssetList, newFontList);
        });
    }

    public void onPsiFileChanged(@NotNull Project project) {
        //root package pubspec.yaml changed
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        PubspecUtils.readAsset(project, (name, version, assetList, fontList) -> {
            DartRFileGenerator.getInstance().generate(project, name, version, assetList);
            DartFontFileGenerator.getInstance().generate(project, fontList);
        });
    }

}
