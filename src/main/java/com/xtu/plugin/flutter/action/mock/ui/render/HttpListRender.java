package com.xtu.plugin.flutter.action.mock.ui.render;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import icons.PluginIcons;

import javax.swing.*;
import java.awt.*;

public class HttpListRender extends JPanel implements ListCellRenderer<HttpEntity> {

    private final JLabel pathLabel;
    private final JLabel descLabel;

    public HttpListRender() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(0, 10));
        add(new JLabel(PluginIcons.LINK), BorderLayout.WEST);

        Box infoPanel = Box.createVerticalBox();
        infoPanel.setBorder(JBUI.Borders.empty(5, 10, 5, 0));
        this.pathLabel = new JLabel();
        pathLabel.setForeground(JBColor.foreground());
        pathLabel.setFont(new Font(null, Font.BOLD, 16));
        infoPanel.add(pathLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        this.descLabel = new JLabel();
        descLabel.setForeground(JBColor.foreground().darker());
        descLabel.setFont(new Font(null, Font.BOLD, 13));
        infoPanel.add(descLabel);
        add(infoPanel, BorderLayout.CENTER);

        add(new JSeparator(), BorderLayout.SOUTH);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends HttpEntity> list,
                                                  HttpEntity entity,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {
        this.pathLabel.setText(entity.path);
        this.descLabel.setText(entity.description);
        return this;
    }
}