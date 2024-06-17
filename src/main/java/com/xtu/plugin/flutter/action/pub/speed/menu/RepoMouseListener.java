package com.xtu.plugin.flutter.action.pub.speed.menu;

import com.intellij.ui.components.JBList;
import com.xtu.plugin.flutter.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class RepoMouseListener extends MouseAdapter {

    private final JBList<String> listView;

    public RepoMouseListener(@NotNull JBList<String> listView) {
        this.listView = listView;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            showContextMenu(e.getComponent(), e.getPoint());
        }
    }

    private void showContextMenu(@NotNull Component component, @NotNull Point point) {
        JPopupMenu popupMenu = new JPopupMenu();
        //添加删除按钮
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            String repo = getRepoByPoint(point);
            if (StringUtils.isEmpty(repo)) return;
            DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
            if (model == null) return;
            model.removeElement(repo);
        });
        popupMenu.add(deleteItem);
        popupMenu.show(component, (int) point.getX(), (int) point.getY());
    }

    private String getRepoByPoint(@NotNull Point point) {
        DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
        if (model == null) return null;
        int index = listView.locationToIndex(point);
        if (index < 0 || index >= model.size()) return null;
        return model.elementAt(index);
    }
}