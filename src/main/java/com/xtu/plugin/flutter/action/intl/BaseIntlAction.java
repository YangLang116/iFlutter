package com.xtu.plugin.flutter.action.intl;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseIntlAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        List<String> localeList = IntlUtils.getLocaleList(project);
        boolean hasLocale = !CollectionUtils.isEmpty(localeList);
        e.getPresentation().setVisible(hasLocale);
    }
}
