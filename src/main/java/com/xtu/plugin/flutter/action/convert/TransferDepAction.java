package com.xtu.plugin.flutter.action.convert;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.convert.utils.DepUtils;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import com.xtu.plugin.flutter.base.task.FileCopyTask;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransferDepAction extends FlutterProjectAction {

    private static final Pattern sRegex = Pattern.compile("^(.+?)/hosted/(.+?)/([^/]+)(.*)$");

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile contextFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || contextFile == null) return;
        Matcher matcher = sRegex.matcher(contextFile.getPath());
        if (!matcher.matches()) return;
        File pluginDir = new File(String.format("%s/hosted/%s/%s", matcher.group(1), matcher.group(2), matcher.group(3)));
        File outputDirectory = DepUtils.selectSaveDir(project);
        if (outputDirectory == null) return;
        new FileCopyTask(project, "Transfer dependence", pluginDir, outputDirectory).queue();
    }

    @Override
    public void whenUpdate(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile contextFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || contextFile == null) {
            e.getPresentation().setVisible(false);
        } else {
            e.getPresentation().setVisible(isLocalHostedPlugin(project, contextFile));
        }
    }

    private static boolean isLocalHostedPlugin(@NotNull Project project, @NotNull VirtualFile file) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        String filePath = file.getPath();
        if (filePath.startsWith(projectPath)) return false;
        return sRegex.matcher(filePath).matches();
    }
}
