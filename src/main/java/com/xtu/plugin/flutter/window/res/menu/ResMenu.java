package com.xtu.plugin.flutter.window.res.menu;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.TinyUtils;
import com.xtu.plugin.flutter.window.res.menu.item.CompressImgItem;
import com.xtu.plugin.flutter.window.res.menu.item.CopyPathItem;
import com.xtu.plugin.flutter.window.res.menu.item.CopyRefItem;
import com.xtu.plugin.flutter.window.res.menu.item.DeleteItem;
import com.xtu.plugin.flutter.window.res.menu.item.OpenFileItem;
import com.xtu.plugin.flutter.window.res.ui.ResManagerListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.swing.JPopupMenu;

public class ResMenu extends JPopupMenu {

    public ResMenu(@NotNull Project project,
                   @NotNull File imageFile,
                   @NotNull ResManagerListener listener) {
        add(new CopyRefItem(project, imageFile, listener));
        add(new CopyPathItem(project, imageFile, listener));
        if (TinyUtils.isSupport(imageFile)) {
            add(new CompressImgItem(project, imageFile, listener));
        }
        add(new DeleteItem(project, imageFile, listener));
        add(new OpenFileItem(project, imageFile, listener));
    }
}
