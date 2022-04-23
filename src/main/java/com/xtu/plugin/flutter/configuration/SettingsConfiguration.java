package com.xtu.plugin.flutter.configuration;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.util.Collections;
import java.util.Objects;

public final class SettingsConfiguration implements SearchableConfigurable {

    private static final String LIST_SPLIT_CHAR = ",";

    private final Project project;

    private JPanel rootPanel;
    private JTextField resDetectField;
    private JTextField ignoreResField;
    private JCheckBox flutter2EnableBox;
    private JCheckBox resCheckEnableBox;
    private JCheckBox foldRegisterBox;
    private JTextField apiKeyField;
    private JTextField apiSecretField;
    private JTextField maxPicSizeField;
    private JTextField maxPicWidthField;
    private JTextField maxPicHeightField;

    public SettingsConfiguration(Project project) {
        this.project = project;
    }

    @Override
    public String getId() {
        return getClass().getName();
    }

    @Override
    public String getDisplayName() {
        return "IFlutter";
    }

    @Override
    public JComponent createComponent() {
        maxPicSizeField.setDocument(new NumberDocument());
        maxPicWidthField.setDocument(new NumberDocument());
        maxPicHeightField.setDocument(new NumberDocument());
        return rootPanel;
    }

    private StorageEntity getStorageEntity() {
        StorageService storageService = StorageService.getInstance(project);
        return storageService.getState();
    }

    @Override
    public boolean isModified() {
        StorageEntity storageEntity = getStorageEntity();
        return !CollectionUtils.join(storageEntity.resDir, LIST_SPLIT_CHAR).equals(resDetectField.getText().trim())
                || !CollectionUtils.join(storageEntity.ignoreResExtension, LIST_SPLIT_CHAR).equals(ignoreResField.getText().trim())
                || storageEntity.flutter2Enable != flutter2EnableBox.isSelected()
                || storageEntity.resCheckEnable != resCheckEnableBox.isSelected()
                || storageEntity.foldRegisterEnable != foldRegisterBox.isSelected()
                || !Objects.equals(storageEntity.apiKey, apiKeyField.getText().trim())
                || !Objects.equals(storageEntity.apiSecret, apiSecretField.getText().trim())
                || storageEntity.maxPicSize != Integer.parseInt(maxPicSizeField.getText().trim())
                || storageEntity.maxPicWidth != Integer.parseInt(maxPicWidthField.getText().trim())
                || storageEntity.maxPicHeight != Integer.parseInt(maxPicHeightField.getText().trim());

    }

    @Override
    public void reset() {
        StorageEntity storageEntity = getStorageEntity();
        String resDirListStr = CollectionUtils.join(storageEntity.resDir, LIST_SPLIT_CHAR);
        resDetectField.setText(resDirListStr);
        String ignoreResExtensionStr = CollectionUtils.join(storageEntity.ignoreResExtension, LIST_SPLIT_CHAR);
        ignoreResField.setText(ignoreResExtensionStr);
        flutter2EnableBox.setSelected(storageEntity.flutter2Enable);
        resCheckEnableBox.setSelected(storageEntity.resCheckEnable);
        foldRegisterBox.setSelected(storageEntity.foldRegisterEnable);
        apiKeyField.setText(storageEntity.apiKey);
        apiSecretField.setText(storageEntity.apiSecret);
        maxPicSizeField.setText(String.valueOf(storageEntity.maxPicSize));
        maxPicWidthField.setText(String.valueOf(storageEntity.maxPicWidth));
        maxPicHeightField.setText(String.valueOf(storageEntity.maxPicHeight));
    }

    @Override
    public void apply() {
        StorageEntity storageEntity = getStorageEntity();
        String resStr = resDetectField.getText().trim();
        storageEntity.resDir = StringUtils.isEmpty(resStr) ?
                Collections.emptyList() : CollectionUtils.split(resStr, LIST_SPLIT_CHAR);
        String ignoreResExtensionStr = ignoreResField.getText().trim();
        storageEntity.ignoreResExtension = StringUtils.isEmpty(ignoreResExtensionStr) ?
                Collections.emptyList() : CollectionUtils.split(ignoreResExtensionStr, LIST_SPLIT_CHAR);
        storageEntity.flutter2Enable = flutter2EnableBox.isSelected();
        storageEntity.resCheckEnable = resCheckEnableBox.isSelected();
        storageEntity.apiKey = apiKeyField.getText().trim();
        storageEntity.apiSecret = apiSecretField.getText().trim();
        storageEntity.maxPicSize = Integer.parseInt(maxPicSizeField.getText().trim());
        storageEntity.maxPicWidth = Integer.parseInt(maxPicWidthField.getText().trim());
        storageEntity.maxPicHeight = Integer.parseInt(maxPicHeightField.getText().trim());
        //检查是否更新了注册方式
        if (storageEntity.foldRegisterEnable != foldRegisterBox.isSelected()) {
            storageEntity.foldRegisterEnable = foldRegisterBox.isSelected();
            reStartIDE();
        }
    }

    private void reStartIDE() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            Messages.showMessageDialog(project, "资源注册方式发生修改，IDEA需要重启", "iFlutter提示", null);
            ((ApplicationImpl) application).restart(true);
        }, ModalityState.NON_MODAL);
    }


    private static class NumberDocument extends PlainDocument {

        public void insertString(int var1, String var2, AttributeSet var3) throws BadLocationException {
            if (this.isNumeric(var2)) {
                super.insertString(var1, var2, var3);
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }

        private boolean isNumeric(String var1) {
            try {
                Long.valueOf(var1);
                return true;
            } catch (NumberFormatException var3) {
                return false;
            }
        }
    }
}
