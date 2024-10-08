package com.xtu.plugin.flutter.action.analysis.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBScrollPane;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class UselessResAnalysisDialog extends DialogWrapper {

    private final Project project;
    private final String result;
    private final Action copyAction;

    public UselessResAnalysisDialog(@Nullable Project project, String result) {
        super(project, null, false, IdeModalityType.IDE, true);
        this.project = project;
        this.result = result;
        this.copyAction = new CopyAction();
        setTitle("Useless Resources in Project");
        init();
    }

    @Override
    protected @Nullable
    @NonNls
    String getDimensionServiceKey() {
        return UselessResAnalysisDialog.class.getSimpleName();
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{copyAction, getOKAction()};
    }

    @Override
    protected @Nullable
    JComponent createCenterPanel() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setText(result);
        return new JBScrollPane(textPane);
    }

    private class CopyAction extends DialogWrapperAction {

        protected CopyAction() {
            super("Copy");
        }


        @Override
        protected void doAction(ActionEvent e) {
            PluginUtils.copyToClipboard(project, result, "copy success");
            UselessResAnalysisDialog.this.close(OK_EXIT_CODE);
        }
    }
}
