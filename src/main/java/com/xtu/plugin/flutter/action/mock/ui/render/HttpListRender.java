package com.xtu.plugin.flutter.action.mock.ui.render;

import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.store.HttpEntity;
import icons.PluginIcons;

import javax.swing.*;
import java.awt.*;

public class HttpListRender extends JPanel implements ListCellRenderer<HttpEntity> {

    private final JLabel pathLabel;
    private final JLabel descLabel;

    public HttpListRender() {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(0, 5));
        add(new JLabel(PluginIcons.LINK), BorderLayout.WEST);

        Box infoPanel = Box.createVerticalBox();
        infoPanel.setBorder(JBUI.Borders.empty(5, 5));
        infoPanel.add(this.pathLabel = createLabel(Font.BOLD, 13));
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(this.descLabel = createLabel(Font.PLAIN, 12));
        add(infoPanel, BorderLayout.CENTER);

        add(new JSeparator(), BorderLayout.SOUTH);
    }

    private JLabel createLabel(int style, int size) {
        JLabel label = new JLabel();
        label.setFont(new Font(null, style, JBUI.scaleFontSize(size)));
        return label;
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