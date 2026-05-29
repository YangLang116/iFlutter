package com.xtu.plugin.flutter.component.assets.handler;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.entity.AssetInfo;
import com.xtu.plugin.flutter.base.utils.FontUtils;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PubSpecFileHandler {

    // package-private static，供 AssetFileHandler 构造 AssetMutation 时复用
    static void addAssetToList(List<String> assetList, List<String> fontList, String asset) {
        if (FontUtils.isFontAsset(asset)) {
            fontList.add(asset);
        } else {
            assetList.add(asset);
        }
    }

    static void removeAssetFromList(List<String> assetList, List<String> fontList, String asset) {
        if (FontUtils.isFontAsset(asset)) {
            fontList.remove(asset);
        } else {
            assetList.remove(asset);
        }
    }

    /**
     * 批量应用多个 AssetMutation：仅做一次 readAssetList + writeAssetList，
     * 避免批量文件事件（如复制多张图片、git checkout）时 pubspec.yaml 被反复读写。
     */
    public void flushMutations(@NotNull Project project, @NotNull List<AssetMutation> mutations) {
        if (mutations.isEmpty()) return;
        LogUtils.info("PubSpecFileHandler flushMutations: " + mutations.size() + " ops");
        WriteCommandAction.runWriteCommandAction(project, () -> {
            AssetInfo assetResult = PubSpecUtils.readAssetList(project);
            List<String> newAssetList = new ArrayList<>(assetResult.assetList);
            List<String> newFontList = new ArrayList<>(assetResult.fontList);
            for (AssetMutation mutation : mutations) {
                mutation.apply(newAssetList, newFontList);
            }
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
    }

    public void onPsiFileChanged(@NotNull Project project) {
        //root package pubspec.yaml changed
        LogUtils.info("PubSpecFileHandler pubspec.yaml changed");
        AssetInfo assetResult = PubSpecUtils.readAssetList(project);
        DartRFileGenerator.getInstance().generate(project, assetResult.meta, assetResult.assetList);
        DartFontFileGenerator.getInstance().generate(project, assetResult.meta, assetResult.fontList);
    }
}
