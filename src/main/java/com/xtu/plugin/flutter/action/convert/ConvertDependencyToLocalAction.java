package com.xtu.plugin.flutter.action.convert;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.BaseDependencyAction;
import com.xtu.plugin.flutter.action.convert.task.ConvertDependencyToLocalTask;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import kotlin.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.util.List;

public class ConvertDependencyToLocalAction extends BaseDependencyAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        Pair<String, YAMLKeyValue> packageInfo = getPackageInfo(e);
        if (packageInfo == null) return;
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(false, true,
                false, false, false, false);
        chooserDescriptor.setTitle("Select dependent storage location");
        VirtualFile chooseDirectory = FileChooser.chooseFile(chooserDescriptor, project, ProjectUtil.guessProjectDir(project));
        if (chooseDirectory == null) return;
        String filePath = chooseDirectory.getPath();
        if (!filePath.startsWith(projectPath)) {
            ToastUtil.make(project, MessageType.ERROR, "select a project directory to store dependency");
            return;
        }
        //disallow res dir or lib
        if (filePath.startsWith(projectPath + "/lib")) {
            ToastUtil.make(project, MessageType.ERROR, "dependency are not allowed to be stored in the lib directory");
            return;
        }
        List<String> assetFoldNameList = AssetUtils.supportAssetFoldName(project);
        for (String foldName : assetFoldNameList) {
            if (filePath.startsWith(projectPath + "/" + foldName)) {
                ToastUtil.make(project, MessageType.ERROR, "dependency are not allowed to be stored in the res directory");
                return;
            }
        }

        File outputDirectory = new File(filePath);
        new ConvertDependencyToLocalTask(project, packageInfo.getFirst(),
                packageInfo.getSecond(), outputDirectory)
                .queue();
    }
}
