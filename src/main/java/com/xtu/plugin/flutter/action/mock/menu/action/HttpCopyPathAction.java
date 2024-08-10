package com.xtu.plugin.flutter.action.mock.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class HttpCopyPathAction extends AnAction {

    private final HttpEntity httpEntity;

    public HttpCopyPathAction(@NotNull HttpEntity httpEntity) {
        super("Copy Path", null, PluginIcons.COPY);
        this.httpEntity = httpEntity;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        HttpMockManager mockManager = HttpMockManager.getService(project);
        String url = mockManager.getUrl(httpEntity.path);
        if (StringUtils.isEmpty(url)) {
            ToastUtils.make(project, MessageType.ERROR, "mock server fail");
        } else {
            PluginUtils.copyToClipboard(project, url, "copy path success");
        }
    }
}
