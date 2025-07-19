package com.xtu.plugin.flutter.action.convert.utils;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

public class DepUtils {

    @Nullable
    public static File selectSaveDir(@NotNull Project project) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return null;
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(
                false, true,
                false, false, false, false);
        chooserDescriptor.setTitle("Select Dependent Storage Location");
        VirtualFile chooseDirectory = FileChooser.chooseFile(chooserDescriptor, project, ProjectUtil.guessProjectDir(project));
        if (chooseDirectory == null) return null;
        String selectPath = chooseDirectory.getPath();
        if (!selectPath.startsWith(projectPath)) {
            ToastUtils.make(project, MessageType.ERROR, "select a project directory to store dependency");
            return null;
        }
        //disallow res dir or lib
        if (selectPath.startsWith(projectPath + "/lib")) {
            ToastUtils.make(project, MessageType.ERROR, "dependency are not allowed to be stored in the lib directory");
            return null;
        }
        List<String> assetFoldNameList = AssetUtils.supportAssetFoldName(project);
        for (String foldName : assetFoldNameList) {
            if (selectPath.startsWith(projectPath + "/" + foldName)) {
                ToastUtils.make(project, MessageType.ERROR, "dependency are not allowed to be stored in the res directory");
                return null;
            }
        }
        return new File(selectPath);
    }
}
