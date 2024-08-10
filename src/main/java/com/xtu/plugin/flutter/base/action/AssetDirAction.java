package com.xtu.plugin.flutter.base.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

//在资源目录下Action
public abstract class AssetDirAction extends FlutterProjectAction {

    @Override
    public void whenUpdate(@NotNull AnActionEvent e) {
        VirtualFile selectFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (selectFile == null || !selectFile.isDirectory()) {
            e.getPresentation().setVisible(false);
            return;
        }
        String selectDirPath = selectFile.getPath();
        Project project = e.getProject();
        assert project != null;
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
