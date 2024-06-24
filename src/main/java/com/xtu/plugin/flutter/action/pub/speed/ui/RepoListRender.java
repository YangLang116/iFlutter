package com.xtu.plugin.flutter.action.pub.speed.ui;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.JBUI;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RepoListRender extends ColoredListCellRenderer<String> {
    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends String> jList,
                                         String value,
                                         int index,
                                         boolean isSelected, boolean cellHasFocus) {
        setBorder(JBUI.Borders.empty(10, 5));
        setIcon(PluginIcons.MAVEN);
        append(value, SimpleTextAttributes.LINK_BOLD_ATTRIBUTES, true);
    }
}