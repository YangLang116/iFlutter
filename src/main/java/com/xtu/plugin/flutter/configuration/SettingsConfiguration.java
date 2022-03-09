package com.xtu.plugin.flutter.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import java.util.Collections;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.commons.lang.StringUtils;

public final class SettingsConfiguration implements SearchableConfigurable {

    private static final String LIST_SPLIT_CHAR = ",";

    private final Project project;

    private JPanel rootPanel;
    private JTextField resDetectField;
    private JTextField ignoreResField;
    private JCheckBox flutter2EnableBox;
    private JCheckBox resCheckEnableBox;
    private JCheckBox updatePubsepcEnableBox;

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
                || storageEntity.resCheckEnable != resCheckEnableBox.isSelected() || storageEntity.updatePubsepc != updatePubsepcEnableBox.isSelected() ;
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
        updatePubsepcEnableBox.setSelected(storageEntity.updatePubsepc);
    }

    @Override
    public void apply() throws ConfigurationException {
        StorageEntity storageEntity = getStorageEntity();
        String resStr = resDetectField.getText().trim();
        storageEntity.resDir = StringUtils.isEmpty(resStr) ?
                Collections.emptyList() : CollectionUtils.split(resStr, LIST_SPLIT_CHAR);
        String ignoreResExtensionStr = ignoreResField.getText().trim();
        storageEntity.ignoreResExtension = StringUtils.isEmpty(ignoreResExtensionStr) ?
                Collections.emptyList() : CollectionUtils.split(ignoreResExtensionStr, LIST_SPLIT_CHAR);
        storageEntity.flutter2Enable = flutter2EnableBox.isSelected();
        storageEntity.resCheckEnable = resCheckEnableBox.isSelected();
        storageEntity.updatePubsepc = updatePubsepcEnableBox.isSelected();
    }
}
