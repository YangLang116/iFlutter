package com.xtu.plugin.flutter.action.mock.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.action.mock.callback.IHttpListComponent;
import com.xtu.plugin.flutter.action.mock.menu.action.HttpCopyPathAction;
import com.xtu.plugin.flutter.action.mock.menu.action.HttpDeleteAction;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HttpMenuGroup extends ActionGroup {

    private final HttpEntity httpEntity;
    private final IHttpListComponent listComponent;

    public HttpMenuGroup(@NotNull HttpEntity httpEntity, @NotNull IHttpListComponent listComponent) {
        this.httpEntity = httpEntity;
        this.listComponent = listComponent;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
        List<AnAction> actionList = new ArrayList<>();
        actionList.add(new HttpCopyPathAction(httpEntity));
        actionList.add(new HttpDeleteAction(httpEntity, listComponent));
        return actionList.toArray(new AnAction[0]);
    }
}
