package com.xtu.plugin.flutter.store.asset;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@State(name = "iFlutterAsset", storages = {@Storage("iFlutter_Asset.xml")})
public class AssetRegisterStorageService implements PersistentStateComponent<AssetStorageEntity> {

    private final Project project;
    private AssetStorageEntity storageEntity = new AssetStorageEntity();

    public AssetRegisterStorageService(@NotNull Project project) {
        this.project = project;
    }

    public static AssetRegisterStorageService getService(@NotNull Project project) {
        return project.getService(AssetRegisterStorageService.class);
    }

    @Override
    public @Nullable
    AssetStorageEntity getState() {
        return storageEntity;
    }

    @Override
    public void loadState(@NotNull AssetStorageEntity state) {
        this.storageEntity = state;
    }

    //首次启动项目同步资源
    public void refreshIfNeed() {
        LogUtils.info("AssetRegisterStorageService refreshIfNeed");
        if (!PluginUtils.isFlutterProject(project)) return;
        if (!AssetUtils.isFoldRegister(project)) return;
        DumbService.getInstance(project).smartInvokeLater(this::refreshAsset); //同步最新资源列表
    }

    private void refreshAsset() {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        List<File> allAssetFile = AssetUtils.getAllAssetFile(project);
        List<String> newAssetList = new ArrayList<>();
        for (File assetFile : allAssetFile) {
            String assetPath = AssetUtils.getAssetPath(projectPath, assetFile);
            if (StringUtils.isEmpty(assetPath)) continue;
            if (!FontUtils.isFontAsset(assetPath)) {
                newAssetList.add(assetPath);
            }
        }
        List<String> assetList = this.storageEntity.assetList;
        assetList.clear();
        assetList.addAll(newAssetList);
        CollectionUtils.duplicateList(assetList);
        Collections.sort(assetList);
    }

    ///获取储存资源列表
    public static List<String> getAssetList(@NotNull Project project) {
        AssetRegisterStorageService service = getService(project);
        return service.storageEntity.assetList;
    }

    //更新资源列表
    public static List<String> updateAsset(@NotNull Project project, @NotNull List<String> newAssetList) {
        if (!AssetUtils.isFoldRegister(project)) return newAssetList;
        //pure asset list
        CollectionUtils.duplicateList(newAssetList);
        Collections.sort(newAssetList);
        //update asset
        AssetRegisterStorageService service = getService(project);
        List<String> assetList = service.storageEntity.assetList;
        assetList.clear();
        assetList.addAll(newAssetList);
        return calcAssetFold(newAssetList);
    }

    private static List<String> calcAssetFold(@NotNull List<String> newAssetList) {
        List<String> assetFoldList = new ArrayList<>();
        for (String asset : newAssetList) {
            if (asset.endsWith("/")) {
                assetFoldList.add(asset);
                continue;
            }
            int index = asset.lastIndexOf('/');
            if (index < 0) continue;
            assetFoldList.add(asset.substring(0, index + 1));
        }
        CollectionUtils.duplicateList(assetFoldList);
        Collections.sort(assetFoldList);
        return assetFoldList;
    }
}
