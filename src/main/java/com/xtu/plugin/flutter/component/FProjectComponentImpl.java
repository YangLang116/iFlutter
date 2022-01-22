package com.xtu.plugin.flutter.component;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.component.assets.AssetsManager;
import org.jetbrains.annotations.NotNull;

public class FProjectComponentImpl implements FProjectComponent {

    private final Project project;
    private final AssetsManager assetsManager;

    public FProjectComponentImpl(@NotNull Project project) {
        this.project = project;
        this.assetsManager = new AssetsManager(project);
    }

    @Override
    public void projectOpened() {
        this.assetsManager.attach();
        HttpMockManager.getService(project).activeServer();
    }

    @Override
    public void projectClosed() {
        this.assetsManager.detach();
        HttpMockManager.getService(project).releaseServer();
    }

}
