package com.xtu.plugin.flutter.component;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.component.assets.AssetsManager;
import com.xtu.plugin.flutter.component.packages.update.FlutterPackageUpdater;
import com.xtu.plugin.flutter.service.asset.AssetStorageService;
import org.jetbrains.annotations.NotNull;

public class FProjectComponentImpl implements FProjectComponent {

    private final Project project;
    private final AssetsManager assetsManager;
    private final FlutterPackageUpdater packageUpdater;

    public FProjectComponentImpl(@NotNull Project project) {
        this.project = project;
        this.assetsManager = new AssetsManager(project);
        this.packageUpdater = new FlutterPackageUpdater(project);
    }

    @Override
    public void projectOpened() {
        this.assetsManager.attach();
        this.packageUpdater.attach();
        HttpMockManager.getService(project).activeServer();
        AssetStorageService.refreshAssetIfNeed(project);
    }

    @Override
    public void projectClosed() {
        this.assetsManager.detach();
        this.packageUpdater.detach();
        HttpMockManager.getService(project).releaseServer();
    }

}
