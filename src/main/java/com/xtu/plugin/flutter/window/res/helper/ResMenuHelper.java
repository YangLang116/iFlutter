package com.xtu.plugin.flutter.window.res.helper;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.io.File;

public class ResMenuHelper {

    public static JPopupMenu createMenu(@NotNull Project project, @NotNull File imageFile) {
        JPopupMenu menu = new JPopupMenu();
        menu.add(createAction("Copy Reference", e -> copyReference()));
        menu.add(createAction("Compress Image", e -> compressImage()));
        return menu;
    }

    private static void copyReference() {
        
    }

    private static void compressImage() {

    }

    private static JMenuItem createAction(@NotNull String title, @NotNull ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        return menuItem;
    }
}
