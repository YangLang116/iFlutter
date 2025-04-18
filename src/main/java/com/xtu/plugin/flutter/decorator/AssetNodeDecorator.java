package com.xtu.plugin.flutter.decorator;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.AssetUtils;

public class AssetNodeDecorator implements ProjectViewNodeDecorator {
    @Override
    public void decorate(ProjectViewNode<?> node, PresentationData data) {
        Project project = node.getProject();
        if (project == null) return;
        VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null) return;
        boolean isAssetDir = AssetUtils.isAssetDir(project, virtualFile);
        if (!isAssetDir) return;
        data.setIcon(AllIcons.Nodes.ResourceBundle);
    }
}
