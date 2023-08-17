package com.xtu.plugin.flutter.component;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.component.assets.AssetsManager;
import com.xtu.plugin.flutter.component.packages.update.FlutterPackageUpdater;
import com.xtu.plugin.flutter.store.asset.AssetRegisterStorageService;
import com.xtu.plugin.flutter.upgrader.UpgradeManager;
import com.xtu.plugin.flutter.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

public class FStartupActivityImpl implements StartupActivity, Disposable {

    @Override
    public void runActivity(@NotNull Project project) {
        LogUtils.info("FStartupActivityImpl open Project");
        AssetRegisterStorageService.getService(project).refreshIfNeed();
        AssetsManager.getService(project).attach();
        HttpMockManager.getService(project).activeServer();
        FlutterPackageUpdater.getService(project).attach();
        UpgradeManager.checkPluginVersion(project);
    }

    @Override
    public void dispose() {
        LogUtils.info("FStartupActivityImpl close Project");
    }
}
