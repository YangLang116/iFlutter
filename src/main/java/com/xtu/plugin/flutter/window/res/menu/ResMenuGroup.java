package com.xtu.plugin.flutter.window.res.menu;

import com.intellij.openapi.actionSystem.*;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import com.xtu.plugin.flutter.window.res.menu.action.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResMenuGroup extends ActionGroup {

    private final File imageFile;
    private final IResRootPanel rootPanel;

    public ResMenuGroup(@NotNull File imageFile, @NotNull IResRootPanel rootPanel) {
        this.imageFile = imageFile;
        this.rootPanel = rootPanel;
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    @NotNull
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        List<AnAction> children = new ArrayList<>();
        children.add(new ResCopyPathAction(imageFile));
        children.add(new ResCopyRefAction(imageFile));
        children.add(new Separator());
        children.add(new ResCompressAction(imageFile, rootPanel));
        children.add(new Separator());
        children.add(new ResDeleteAction(imageFile));
        children.add(new Separator());
        children.add(new ResOpenFileAction(imageFile));
        return children.toArray(new AnAction[0]);
    }
}
