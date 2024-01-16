package com.xtu.plugin.flutter.decorator;

import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ProjectViewNode;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.xtu.plugin.flutter.utils.AssetUtils;

public class AssetNodeDecorator implements ProjectViewNodeDecorator {
    @Override
    public void decorate(ProjectViewNode<?> node, PresentationData data) {
        Project project = node.getProject();
        if (project == null) return;
        VirtualFile virtualFile = node.getVirtualFile();
        if (virtualFile == null) return;
        boolean isAssetDir = AssetUtils.isAssetDir(project, virtualFile);
        if (!isAssetDir) return;
        PresentationData presentation = node.getPresentation();
        presentation.setIcon(AllIcons.Nodes.ResourceBundle);
    }

    @Override
    public void decorate(PackageDependenciesNode node, ColoredTreeCellRenderer cellRenderer) {
    }
}
