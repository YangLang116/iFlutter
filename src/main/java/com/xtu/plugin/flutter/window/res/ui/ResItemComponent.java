package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.util.text.Formats;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.base.component.ImageComponent;
import com.xtu.plugin.flutter.base.entity.ImageSize;
import com.xtu.plugin.flutter.base.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ResItemComponent extends JPanel {

    private JLabel sizeLabel;
    private ImageComponent imageComponent;

    public ResItemComponent(@NotNull File imageFile) {
        setLayout(new BorderLayout());
        setBorder(JBUI.Borders.empty(0, 5));
        loadFileInfo(imageFile);
        loadThumbnail(imageFile);
        add(new JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH);
    }

    private void loadThumbnail(@NotNull File assetFile) {
        add(this.imageComponent = new ImageComponent(new Dimension(60, 60)), BorderLayout.WEST);
        this.imageComponent.loadImage(assetFile, new ImageSize(50, 50), (size) -> {
            String originText = this.sizeLabel.getText();
            this.sizeLabel.setText(String.format("%s | %d x %d", originText, size.width, size.height));
        });
    }

    private void loadFileInfo(@NotNull File assetFile) {
        Box container = Box.createVerticalBox();
        container.setBorder(JBUI.Borders.empty(0, 5));

        container.add(Box.createVerticalGlue());

        JLabel nameLabel = new JLabel(FileUtils.getFileName(assetFile));
        Font nameFont = new Font(null, Font.BOLD, JBUI.scaleFontSize(13f));
        nameLabel.setFont(nameFont);
        container.add(nameLabel);

        container.add(Box.createVerticalStrut(5));

        String imageSize = Formats.formatFileSize(assetFile.length());
        sizeLabel = new JLabel(imageSize);
        sizeLabel.setFont(new Font(null, Font.PLAIN, JBUI.scaleFontSize(12f)));
        container.add(sizeLabel);

        container.add(Box.createVerticalGlue());

        add(container, BorderLayout.CENTER);
    }

    public void dispose() {
        this.imageComponent.dispose();
    }
}
