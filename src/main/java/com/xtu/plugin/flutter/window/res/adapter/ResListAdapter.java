package com.xtu.plugin.flutter.window.res.adapter;

import com.xtu.plugin.flutter.window.res.ui.ResItemComponent;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ResListAdapter implements ListCellRenderer<File> {

    private final Map<String, ResItemComponent> componentCache = new HashMap<>();

    @Override
    public Component getListCellRendererComponent(JList<? extends File> list, File assetFile, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        String path = assetFile.getAbsolutePath();
        if (!componentCache.containsKey(path)) {
            componentCache.put(path, new ResItemComponent(assetFile));
        }
        return componentCache.get(path);
    }

    public void resetItems(@NotNull List<File> changeFileList) {
        for (File imageFile : changeFileList) {
            String path = imageFile.getAbsolutePath();
            ResItemComponent resRowComponent = this.componentCache.remove(path);
            if (resRowComponent != null) resRowComponent.dispose();
        }
    }

    public void cleanResList() {
        for (Map.Entry<String, ResItemComponent> entry : componentCache.entrySet()) {
            ResItemComponent jComponent = entry.getValue();
            jComponent.dispose();
        }
        this.componentCache.clear();
    }

}
