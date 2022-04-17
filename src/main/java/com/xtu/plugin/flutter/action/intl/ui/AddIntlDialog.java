package com.xtu.plugin.flutter.action.intl.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddIntlDialog extends DialogWrapper {

    private final List<String> localeList;
    private JTextField keyTextField;
    private final Map<String, JTextField> localeFieldMap = new HashMap<>();

    public AddIntlDialog(@Nullable Project project, @NotNull List<String> localeList) {
        super(project, null, false, IdeModalityType.PROJECT, true);
        this.localeList = localeList;
        init();
    }

    @Override
    @Nullable
    @NonNls
    protected String getDimensionServiceKey() {
        return AddIntlDialog.class.getSimpleName();
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        Box rootPanel = Box.createVerticalBox();
        //add key container
        Box keyContainer = Box.createHorizontalBox();
        keyContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        keyContainer.add(new JLabel("Key:"));
        keyContainer.add(Box.createHorizontalStrut(10));
        keyTextField = new JTextField();
        keyContainer.add(keyTextField);
        rootPanel.add(keyContainer);
        //local list
        rootPanel.add(Box.createVerticalStrut(10));
        JLabel localeTip = new JLabel("Locale List：");
        localeTip.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(localeTip);
        rootPanel.add(Box.createVerticalStrut(5));
        for (String locale : localeList) {
            Box localeItemBox = Box.createHorizontalBox();
            localeItemBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            localeItemBox.setBorder(JBUI.Borders.emptyBottom(5));
            JLabel localeLabel = new JLabel(locale);
            localeLabel.setMinimumSize(new Dimension(100, 0));
            localeItemBox.add(localeLabel);
            localeItemBox.add(Box.createHorizontalStrut(10));
            JTextField localeTextField = new JTextField();
            localeItemBox.add(localeTextField);
            localeFieldMap.put(locale, localeTextField);
            rootPanel.add(localeItemBox);
        }
        return rootPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return keyTextField;
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{getOKAction()};
    }

    public String getKey() {
        return this.keyTextField.getText().trim();
    }

    public Map<String, String> getLocaleMap() {
        Map<String, String> localeMap = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : this.localeFieldMap.entrySet()) {
            String value = entry.getValue().getText().trim();
            localeMap.put(entry.getKey(), value);
        }
        return localeMap;
    }
}
