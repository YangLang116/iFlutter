package com.xtu.plugin.flutter.base.dialog;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ComboBoxDialog extends DialogWrapper {

    private final String dialogKey;
    private final ComboBox<String> comboBox;

    public ComboBoxDialog(@NotNull Project project,
                          @NotNull String title,
                          @NotNull List<String> optionList,
                          @NotNull String dialogKey) {
        super(project, null, false, IdeModalityType.PROJECT, true);
        setHorizontalStretch(1.5f);
        setTitle(title);
        this.dialogKey = dialogKey;
        this.comboBox = createTypeComboBox(optionList);
        init();
    }

    private ComboBox<String> createTypeComboBox(@NotNull List<String> optionList) {
        ComboBox<String> comboBox = new ComboBox<>();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addAll(optionList);
        comboBox.setModel(model);
        comboBox.setSelectedIndex(0);
        return comboBox;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.comboBox;
    }


    public int getSelectIndex() {
        return this.comboBox.getSelectedIndex();
    }

    @Nullable
    @NonNls
    @Override
    protected String getDimensionServiceKey() {
        return dialogKey;
    }
}
