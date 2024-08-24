package com.xtu.plugin.flutter.action.mock.menu.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.action.mock.callback.IHttpListComponent;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class HttpDeleteAction extends AnAction {

    private final HttpEntity httpEntity;
    private final IHttpListComponent listComponent;

    public HttpDeleteAction(@NotNull HttpEntity httpEntity, @NotNull IHttpListComponent listComponent) {
        super("Delete", null, PluginIcons.DELETE);
        this.httpEntity = httpEntity;
        this.listComponent = listComponent;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        this.listComponent.deleteHttpConfig(httpEntity);
    }
}
