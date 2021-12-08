package com.xtu.plugin.flutter.configuration;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.Collections;

public final class SettingsConfiguration implements SearchableConfigurable {

    private static final String LIST_SPLIT_CHAR = ",";

    private final Project project;

    private JPanel rootPanel;
    private JTextField resDetectField;
    private JTextField imgDetectField;

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
                || !CollectionUtils.join(storageEntity.imageDir, LIST_SPLIT_CHAR).equals(imgDetectField.getText().trim());
    }

    @Override
    public void reset() {
        StorageEntity storageEntity = getStorageEntity();
        String resDirListStr = CollectionUtils.join(storageEntity.resDir, LIST_SPLIT_CHAR);
        resDetectField.setText(resDirListStr);
        String imgDirListStr = CollectionUtils.join(storageEntity.imageDir, LIST_SPLIT_CHAR);
        imgDetectField.setText(imgDirListStr);
    }

    @Override
    public void apply() throws ConfigurationException {
        StorageEntity storageEntity = getStorageEntity();
        String resStr = resDetectField.getText().trim();
        storageEntity.resDir = StringUtils.isEmpty(resStr) ?
                Collections.emptyList() : CollectionUtils.split(resStr, LIST_SPLIT_CHAR);
        String imgStr = imgDetectField.getText().trim();
        storageEntity.imageDir = StringUtils.isEmpty(imgStr) ?
                Collections.emptyList() : CollectionUtils.split(imgStr, LIST_SPLIT_CHAR);
    }
}
