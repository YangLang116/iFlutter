package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ResRowComponent extends JPanel {

    private JLabel sizeLabel;
    private ImageComponent imageComponent;

    ResRowComponent(@NotNull File imageFile) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(2, 5));
        loadFileInfo(imageFile);
        loadThumbnail(imageFile);
        add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
    }

    private void loadThumbnail(@NotNull File assetFile) {
        add(this.imageComponent = new ImageComponent(60), BorderLayout.WEST);
        this.imageComponent.loadImage(assetFile, 50, (width, height) -> {
            String originText = this.sizeLabel.getText();
            this.sizeLabel.setText(String.format("%s | %d x %d", originText, width, height));
        });
    }

    private void loadFileInfo(@NotNull File assetFile) {
        Box container = Box.createVerticalBox();
        container.setBorder(JBUI.Borders.empty(10, 5));

        container.add(Box.createVerticalGlue());

        JLabel nameLabel = new JLabel(FileUtils.getFileName(assetFile));
        Font nameFont = new Font(null, Font.BOLD, JBUI.scaleFontSize(13f));
        nameLabel.setFont(nameFont);
        container.add(nameLabel);

        container.add(Box.createVerticalStrut(5));

        String imageSize = StringUtil.formatFileSize(assetFile.length());
        sizeLabel = new JLabel(imageSize);
        Font sizeFont = new Font(null, Font.PLAIN, JBUI.scaleFontSize(12f));
        sizeLabel.setForeground(JBColor.foreground());
        sizeLabel.setFont(sizeFont);
        container.add(sizeLabel);

        container.add(Box.createVerticalGlue());

        add(container, BorderLayout.CENTER);
    }

    public void dispose() {
        this.imageComponent.dispose();
    }
}
