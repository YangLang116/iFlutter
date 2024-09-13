package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.entity.AssetInfoEntity;
import com.xtu.plugin.flutter.base.utils.FontUtils;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
        WriteCommandAction.runWriteCommandAction(project, () -> {
            AssetInfoEntity assetResult = PubSpecUtils.readAssetList(project);
            List<String> newAssetList = new ArrayList<>(assetResult.assetList);
            List<String> newFontList = new ArrayList<>(assetResult.fontList);
            for (String asset : addAssetList) {
                addAssetToList(newAssetList, newFontList, asset);
            }
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
    }

    void removeAsset(@NotNull Project project, String removeAsset) {
        LogUtils.info("PubSpecFileHandler removeAsset: " + removeAsset);
        WriteCommandAction.runWriteCommandAction(project, () -> {
            AssetInfoEntity resultEntity = PubSpecUtils.readAssetList(project);
            List<String> newAssetList = new ArrayList<>(resultEntity.assetList);
            List<String> newFontList = new ArrayList<>(resultEntity.fontList);
            removeAssetFromList(newAssetList, newFontList, removeAsset);
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
    }

    void changeAsset(Project project, String oldAssetName, String newAssetName) {
        LogUtils.info(String.format("PubSpecFileHandler changeAsset(%s -> %s)", oldAssetName, newAssetName));
        WriteCommandAction.runWriteCommandAction(project, () -> {
            AssetInfoEntity assetResult = PubSpecUtils.readAssetList(project);
            List<String> newAssetList = new ArrayList<>(assetResult.assetList);
            List<String> newFontList = new ArrayList<>(assetResult.fontList);
            removeAssetFromList(newAssetList, newFontList, oldAssetName);
            addAssetToList(newAssetList, newFontList, newAssetName);
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
    }

    public void onPsiFileChanged(@NotNull Project project) {
        //root package pubspec.yaml changed
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        AssetInfoEntity assetResult = PubSpecUtils.readAssetList(project);
        DartRFileGenerator.getInstance().generate(project, assetResult.meta, assetResult.assetList);
        DartFontFileGenerator.getInstance().generate(project, assetResult.meta, assetResult.fontList);
    }
}
