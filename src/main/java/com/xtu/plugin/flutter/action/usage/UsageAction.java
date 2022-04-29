package com.xtu.plugin.flutter.action.usage;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class UsageAction extends AnAction {

    private static final String URL_DOCUMENT = "https://iflutter.toolu.cn";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        BrowserUtil.open(URL_DOCUMENT);
    }
}
