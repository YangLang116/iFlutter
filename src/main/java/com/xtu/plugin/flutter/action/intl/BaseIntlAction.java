package com.xtu.plugin.flutter.action.intl;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import org.jetbrains.annotations.NotNull;

public abstract class BaseIntlAction extends FlutterProjectAction {

    @Override
    public void whenUpdate(@NotNull AnActionEvent e) {
        final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        final Presentation presentation = e.getPresentation();
        if (virtualFile != null && IntlUtils.isIntlDir(virtualFile)) {
            presentation.setVisible(true);
        } else {
            final Project project = e.getProject();
            presentation.setVisible(project != null && IntlUtils.getRootIntlDir(project) != null);
        }
    }
}
