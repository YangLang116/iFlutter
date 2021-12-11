package com.xtu.plugin.flutter.component;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.component.assets.AssetsManager;

public class FProjectComponentImpl implements FProjectComponent {

    private final Project project;

    public FProjectComponentImpl(Project project) {
        this.project = project;
    }

    @Override
    public void projectOpened() {
        HttpMockManager.getInstance(project).activeServerIfNeed();
        AssetsManager.getInstance().attach(project);
    }

    @Override
    public void projectClosed() {
        HttpMockManager.getInstance(project).release();
        AssetsManager.getInstance().detach(project);
    }

}
