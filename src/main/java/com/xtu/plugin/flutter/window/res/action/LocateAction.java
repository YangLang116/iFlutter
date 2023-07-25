package com.xtu.plugin.flutter.window.res.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import org.jetbrains.annotations.NotNull;

public class LocateAction extends AnAction {

    private final ResManagerRootPanel rootPanel;

    public LocateAction(@NotNull ResManagerRootPanel rootPanel) {
        super(AllIcons.General.Locate);
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
        this.rootPanel.localeRes(virtualFile.getName());
    }
}
