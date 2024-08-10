package com.xtu.plugin.flutter.action.intl.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.action.intl.BaseIntlAction;
import com.xtu.plugin.flutter.action.intl.IntlResManager;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.base.utils.CollectionUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("SpellCheckingInspection")
public class RemoveIntlAction extends BaseIntlAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        List<String> localeList = IntlUtils.getLocaleList(project);
        if (CollectionUtils.isEmpty(localeList)) return;
        String key = Messages.showInputDialog(project, "", "Remove Intl Res", null);
        if (StringUtils.isEmpty(key)) return;
        IntlResManager.removeLocaleAssetList(project, key, localeList);
    }
}
