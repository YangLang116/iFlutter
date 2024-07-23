package com.xtu.plugin.flutter.action.usage;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import org.jetbrains.annotations.NotNull;

public class UsageAction extends FlutterProjectAction {

    @SuppressWarnings("HttpUrlsUsage")
    private static final String URL_DOCUMENT = "https://yanglang116.github.io/iFlutter";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.open(URL_DOCUMENT);
    }
}
