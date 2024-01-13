package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.FontUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PubSpecUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PubSpecFileHandler {

    private void addAssetToList(List<String> assetList, List<String> fontList, String asset) {
        if (FontUtils.isFontAsset(asset)) {
            fontList.add(asset);
        } else {
            assetList.add(asset);
        }
    }

    private void removeAssetFromList(List<String> assetList, List<String> fontList, String asset) {
        if (FontUtils.isFontAsset(asset)) {
            fontList.remove(asset);
        } else {
            assetList.remove(asset);
        }
    }

    void addAsset(@NotNull Project project, @NotNull List<String> addAssetList) {
        LogUtils.info("PubSpecFileHandler addAsset: " + addAssetList);
        PubSpecUtils.readAssetSafe(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            for (String asset : addAssetList) {
                addAssetToList(newAssetList, newFontList, asset);
            }
            PubSpecUtils.writeAssetSafe(project, newAssetList, newFontList);
        });
    }

    void removeAsset(@NotNull Project project, String removeAsset) {
        LogUtils.info("PubSpecFileHandler removeAsset: " + removeAsset);
        PubSpecUtils.readAssetSafe(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            removeAssetFromList(newAssetList, newFontList, removeAsset);
            PubSpecUtils.writeAssetSafe(project, newAssetList, newFontList);
        });
    }

    void changeAsset(Project project, String oldAssetName, String newAssetName) {
        LogUtils.info(String.format(Locale.ROOT, "PubSpecFileHandler changeAsset(%s -> %s)", oldAssetName, newAssetName));
        PubSpecUtils.readAssetSafe(project, (name, version, assetList, fontList) -> {
            List<String> newAssetList = new ArrayList<>(assetList);
            List<String> newFontList = new ArrayList<>(fontList);
            removeAssetFromList(newAssetList, newFontList, oldAssetName);
            addAssetToList(newAssetList, newFontList, newAssetName);
            PubSpecUtils.writeAssetSafe(project, newAssetList, newFontList);
        });
    }

    public void onPsiFileChanged(@NotNull Project project) {
        //root package pubspec.yaml changed
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        PubSpecUtils.readAssetSafe(project, (name, version, assetList, fontList) -> {
            String resPrefix = AssetUtils.getResPrefix(project, name);
            DartRFileGenerator.getInstance().generate(project, name, version, resPrefix, assetList, false);
            DartFontFileGenerator.getInstance().generate(project, resPrefix, fontList, false);
        });
    }
}
