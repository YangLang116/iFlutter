package com.xtu.plugin.flutter.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

//在资源目录下Action
public abstract class BaseResourceAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (!PluginUtils.isFlutterProject(project)) {
            e.getPresentation().setVisible(false);
            return;
        }
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || !virtualFile.isDirectory()) {
            e.getPresentation().setVisible(false);
            return;
        }
        String selectDirPath = virtualFile.getPath();
        String projectPath = PluginUtils.getProjectPath(project);
        List<String> assetFoldNameList = AssetUtils.supportAssetFoldName(project);
        for (String foldName : assetFoldNameList) {
            if (selectDirPath.equals(projectPath + "/" + foldName)) {
                e.getPresentation().setVisible(true);
                return;
            }
        }
        e.getPresentation().setVisible(false);
    }
}
