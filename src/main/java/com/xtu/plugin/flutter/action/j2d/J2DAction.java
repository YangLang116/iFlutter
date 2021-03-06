package com.xtu.plugin.flutter.action.j2d;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.j2d.ui.J2DDialog;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class J2DAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) {
            e.getPresentation().setVisible(false);
            return;
        }
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        String path = virtualFile.getPath();
        boolean isLibChildPath = path.startsWith(projectPath + "/lib");
        if (isLibChildPath) {
            e.getPresentation().setVisible(virtualFile.isDirectory());
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualDirectory = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualDirectory == null) return;
        J2DDialog dialog = new J2DDialog(project, virtualDirectory);
        dialog.show();
    }
}
