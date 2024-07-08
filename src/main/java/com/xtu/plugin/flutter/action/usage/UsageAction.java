package com.xtu.plugin.flutter.action.usage;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class UsageAction extends FlutterProjectAction {

    @SuppressWarnings("HttpUrlsUsage")
    private static final String URL_DOCUMENT = "http://iflutter.toolu.cn";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.open(URL_DOCUMENT);
    }
}
