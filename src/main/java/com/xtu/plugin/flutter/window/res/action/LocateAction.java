package com.xtu.plugin.flutter.window.res.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

public class LocateAction extends AnAction {

    private final IResRootPanel rootPanel;

    public LocateAction(@NotNull IResRootPanel rootPanel) {
        super(PluginIcons.LOCATE);
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) return;
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        FileEditor selectedEditor = fileEditorManager.getSelectedEditor();
        if (selectedEditor == null) return;
        VirtualFile virtualFile = selectedEditor.getFile();
        if (virtualFile == null) return;
        this.rootPanel.locateRes(virtualFile.getName());
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
