package com.xtu.plugin.flutter.window.res.menu.item;

import com.intellij.ide.actions.RevealFileAction;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.window.res.ui.ResManagerListener;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.io.File;

public class OpenFileItem extends AbstractItem {

    public OpenFileItem(@NotNull Project project,
                        @NotNull File imageFile,
                        @NotNull ResManagerListener listener) {
        super("Open In File Browser", project, imageFile, listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        RevealFileAction.openFile(imageFile);
    }
}
