package com.xtu.plugin.flutter.action.pub.speed.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class RepoListRender extends ColoredListCellRenderer<String> {
    @Override
    protected void customizeCellRenderer(@NotNull JList<? extends String> jList,
                                         String value,
                                         int index,
                                         boolean isSelected, boolean cellHasFocus) {
        setBorderInsets(new Insets(10, 5, 10, 5));
        setIcon(AllIcons.CodeWithMe.CwmInvite);
        append(value, SimpleTextAttributes.REGULAR_ATTRIBUTES, true);
    }
}