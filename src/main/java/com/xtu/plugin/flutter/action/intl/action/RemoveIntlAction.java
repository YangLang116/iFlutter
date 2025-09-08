package com.xtu.plugin.flutter.action.intl.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.intl.BaseIntlAction;
import com.xtu.plugin.flutter.action.intl.IntlResManager;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveIntlAction extends BaseIntlAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        String key = Messages.showInputDialog(project, "", "Remove Intl Res", null);
        if (key == null) return;
        final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && IntlUtils.isIntlDir(virtualFile)) {
            List<String> localeList = IntlUtils.getLocaleList(virtualFile);
            IntlResManager.removeLocaleAssetList(project, virtualFile, localeList, key);
        } else {
            VirtualFile rootIntlDir = IntlUtils.getRootIntlDir(project);
            if (rootIntlDir == null) return;
            List<String> rootLocaleList = IntlUtils.getLocaleList(rootIntlDir);
            IntlResManager.removeLocaleAssetList(project, rootIntlDir, rootLocaleList, key);
        }
    }
}
