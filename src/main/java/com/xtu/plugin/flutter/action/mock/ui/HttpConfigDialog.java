package com.xtu.plugin.flutter.action.mock.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.xtu.plugin.flutter.store.HttpEntity;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HttpConfigDialog extends DialogWrapper {

    private JPanel rootView;
    private JTextField pathView;
    private JComboBox<String> methodBox;
    private JTextField descriptionView;
    private JTextPane responseView;

    private final HttpEntity inputEntity;
    private HttpEntity resultEntity;
    private final Action saveAction = new SaveAction();
    private final Action cancelAction = new CancelAction();

    public HttpConfigDialog(@Nullable Project project, @Nullable HttpEntity httpEntity) {
        super(project, false, IdeModalityType.PROJECT);
        this.inputEntity = httpEntity;
        setTitle("HTTP Mock Config");
        setHorizontalStretch(2);
        setVerticalStretch(2);
        init();
    }

    @Override
    @Nullable
    @NonNls
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return pathView;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        methodBox.setModel(new DefaultComboBoxModel<>(new String[]{"Get", "Post"}));
        if (inputEntity != null) {
            pathView.setText(inputEntity.path);
            methodBox.setSelectedItem(inputEntity.method);
            descriptionView.setText(inputEntity.description);
            responseView.setText(inputEntity.response);
        }
        return rootView;
    }

    private void doSave() {
        String pathValue = pathView.getText().trim();
        String methodValue = (String) methodBox.getSelectedItem();
        String descriptionValue = descriptionView.getText().trim();
        String responseValue = responseView.getText().trim();
        if (StringUtils.isEmpty(pathValue)
                || StringUtils.isEmpty(methodValue)
                || StringUtils.isEmpty(descriptionValue)
                || StringUtils.isEmpty(responseValue)) {
            return;
        }
        resultEntity = new HttpEntity(pathValue, methodValue, descriptionValue, responseValue);
        close(DialogWrapper.OK_EXIT_CODE);
    }

    public HttpEntity getResultEntity() {
        return resultEntity;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{saveAction, cancelAction};
    }

    private class SaveAction extends DialogWrapper.DialogWrapperAction {

        protected SaveAction() {
            super("Save");
        }

        @Override
        protected void doAction(ActionEvent e) {
            doSave();
        }
    }

    private class CancelAction extends DialogWrapper.DialogWrapperAction {

        protected CancelAction() {
            super("Cancel");
        }

        @Override
        protected void doAction(ActionEvent e) {
            close(DialogWrapper.CANCEL_EXIT_CODE);
        }
    }
}
