package com.xtu.plugin.flutter.component;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.util.Disposer;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.component.assets.AssetsManager;
import com.xtu.plugin.flutter.component.packages.update.FlutterPackageUpdater;
import com.xtu.plugin.flutter.service.asset.AssetStorageService;
import com.xtu.plugin.flutter.upgrader.UpgradeManager;
import com.xtu.plugin.flutter.utils.LogUtils;
import org.jetbrains.annotations.NotNull;

public class FStartupActivityImpl implements StartupActivity, Disposable {

    private Project project;
    private AssetsManager assetsManager;
    private FlutterPackageUpdater packageUpdater;

    @Override
    public void runActivity(@NotNull Project project) {
        LogUtils.info("FStartupActivityImpl open Project");
        Disposer.register(project, this);
        this.project = project;
        this.packageUpdater = new FlutterPackageUpdater(project);
        this.assetsManager = new AssetsManager(project);
        //initialization
        this.assetsManager.attach();
        this.packageUpdater.attach();
        HttpMockManager.getService(project).activeServer();
        AssetStorageService.refreshAssetIfNeed(project);
        UpgradeManager.getInstance().checkPluginVersion(project);
    }

    @Override
    public void dispose() {
        LogUtils.info("FStartupActivityImpl close Project");
        this.assetsManager.detach();
        this.packageUpdater.detach();
    }
}
