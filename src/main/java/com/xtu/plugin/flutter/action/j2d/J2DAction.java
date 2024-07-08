package com.xtu.plugin.flutter.action.j2d;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.j2d.ui.J2DDialog;
import com.xtu.plugin.flutter.base.action.LibDirAction;
import org.jetbrains.annotations.NotNull;

public class J2DAction extends LibDirAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile virtualDirectory = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project == null || virtualDirectory == null) return;
        J2DDialog dialog = new J2DDialog(project, virtualDirectory);
        dialog.show();
    }
}
