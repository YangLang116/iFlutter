package com.xtu.plugin.flutter.action.intl.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.action.intl.BaseIntlAction;
import com.xtu.plugin.flutter.action.intl.IntlResManager;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 简化国际化语言内容删除
 */
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
