package com.xtu.plugin.flutter.action.intl.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.intl.BaseIntlAction;
import com.xtu.plugin.flutter.action.intl.IntlResManager;
import com.xtu.plugin.flutter.action.intl.ui.AddIntlDialog;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AddIntlAction extends BaseIntlAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        final VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (virtualFile != null && IntlUtils.isIntlDir(virtualFile)) {
            List<String> locateList = IntlUtils.getLocaleList(virtualFile);
            showAddIntlDialog(project, virtualFile, locateList);
        } else {
            VirtualFile rootIntlDir = IntlUtils.getRootIntlDir(project);
            if (rootIntlDir == null) return;
            List<String> rootLocateList = IntlUtils.getLocaleList(rootIntlDir);
            showAddIntlDialog(project, rootIntlDir, rootLocateList);
        }
    }

    private void showAddIntlDialog(@NotNull Project project,
                                   @NotNull VirtualFile intlDir,
                                   @Nullable List<String> localeList) {
        if (localeList == null || localeList.isEmpty()) return;
        AddIntlDialog addIntlDialog = new AddIntlDialog(project, localeList);
        boolean isOk = addIntlDialog.showAndGet();
        if (!isOk) return;
        String key = addIntlDialog.getKey();
        if (StringUtils.isEmpty(key)) {
            ToastUtils.make(project, MessageType.ERROR, "key cannot be empty");
            return;
        }
        Map<String, String> localeMap = addIntlDialog.getLocaleMap();
        for (Map.Entry<String, String> localeEntry : localeMap.entrySet()) {
            String locale = localeEntry.getKey();
            String value = localeEntry.getValue();
            if (StringUtils.isEmpty(locale) || StringUtils.isEmpty(value)) {
                ToastUtils.make(project, MessageType.ERROR, "locale cannot be empty");
                return;
            }
        }
        boolean canReplaceKey = addIntlDialog.canReplaceKey();
        IntlResManager.addLocaleAssetList(project, intlDir, key, localeMap, canReplaceKey);
    }
}
