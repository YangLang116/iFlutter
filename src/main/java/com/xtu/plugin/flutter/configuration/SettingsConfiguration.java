package com.xtu.plugin.flutter.configuration;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.xtu.plugin.flutter.action.pub.speed.ui.MirrorRepoDialog;
import com.xtu.plugin.flutter.advice.AdviceDialog;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import icons.PluginIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Objects;

public final class SettingsConfiguration implements SearchableConfigurable {

    private static final String sGithubUrl = "https://github.com/YangLang116/iFlutter";
    private static final String sPluginUrl = "https://plugins.jetbrains.com/plugin/18457-iflutter";
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
    private JLabel githubLabel;
    private JLabel starLabel;
    private String mirrorRepoStr;
    private JButton mirrorRepoBtn;
    private JCheckBox withPackageNameBox;
    private JCheckBox isUnModifiableFromJson;
    private JTextField tinyApiKeyField;
    private JLabel tinyQuestion;
    private JLabel adviceLabel;
    private JCheckBox autoTinyImage;
    private JCheckBox enableSizeMonitor;

    public SettingsConfiguration(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
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
        //添加Github地址
        githubLabel.setIcon(PluginIcons.GITHUB);
        githubLabel.setText("<html><u>Source Code</u></html>");
        githubLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        githubLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.open(sGithubUrl);
            }
        });
        //添加评分地址
        starLabel.setIcon(PluginIcons.STAR);
        starLabel.setText("<html><u>Star Plugin</u></html>");
        starLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        starLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.open(sPluginUrl);
            }
        });
        //建议与反馈
        adviceLabel.setIcon(PluginIcons.NOTE);
        adviceLabel.setText("<html><u>Suggestion & Feedback</u></html>");
        adviceLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AdviceDialog.show(project);
            }
        });
        //修改镜像仓库
        mirrorRepoBtn.addActionListener(e -> {
            MirrorRepoDialog mirrorRepoDialog = new MirrorRepoDialog(project, rootPanel);
            boolean isOk = mirrorRepoDialog.showAndGet();
            if (!isOk) return;
            mirrorRepoStr = mirrorRepoDialog.getRepoStr();
        });
        //图片尺寸开关
        enableSizeMonitor.addActionListener(e -> {
            boolean isSelected = enableSizeMonitor.isSelected();
            JTextField[] fieldList = new JTextField[]{maxPicSizeField, maxPicWidthField, maxPicHeightField};
            for (JTextField field : fieldList) {
                field.setEnabled(isSelected);
            }
        });
        //tiny
        tinyQuestion.setText("<html><u>get your api key</u></html>");
        tinyQuestion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                BrowserUtil.open("https://tinypng.com/developers");
            }
        });
        return rootPanel;
    }

    private ProjectStorageEntity getStorageEntity() {
        ProjectStorageService storageService = ProjectStorageService.getInstance(project);
        return storageService.getState();
    }

    @Override
    public boolean isModified() {
        ProjectStorageEntity storageEntity = getStorageEntity();
        return !CollectionUtils.join(storageEntity.resDir, LIST_SPLIT_CHAR).equals(resDetectField.getText().trim())
                || !CollectionUtils.join(storageEntity.ignoreResExtension, LIST_SPLIT_CHAR).equals(ignoreResField.getText().trim())
                || storageEntity.flutter2Enable != flutter2EnableBox.isSelected()
                || storageEntity.resCheckEnable != resCheckEnableBox.isSelected()
                || storageEntity.foldRegisterEnable != foldRegisterBox.isSelected()
                || storageEntity.registerResWithPackage != withPackageNameBox.isSelected()
                || storageEntity.isUnModifiableFromJson != isUnModifiableFromJson.isSelected()
                || !Objects.equals(storageEntity.mirrorRepoStr, mirrorRepoStr)
                || !Objects.equals(storageEntity.apiKey, apiKeyField.getText().trim())
                || !Objects.equals(storageEntity.apiSecret, apiSecretField.getText().trim())
                || storageEntity.enableSizeMonitor != enableSizeMonitor.isSelected()
                || storageEntity.maxPicSize != Integer.parseInt(maxPicSizeField.getText().trim())
                || storageEntity.maxPicWidth != Integer.parseInt(maxPicWidthField.getText().trim())
                || storageEntity.maxPicHeight != Integer.parseInt(maxPicHeightField.getText().trim())
                || !Objects.equals(storageEntity.tinyApiKey, tinyApiKeyField.getText().trim())
                || storageEntity.autoTinyImage != autoTinyImage.isSelected();

    }

    @Override
    public void reset() {
        ProjectStorageEntity storageEntity = getStorageEntity();
        String resDirListStr = CollectionUtils.join(storageEntity.resDir, LIST_SPLIT_CHAR);
        resDetectField.setText(resDirListStr);
        String ignoreResExtensionStr = CollectionUtils.join(storageEntity.ignoreResExtension, LIST_SPLIT_CHAR);
        ignoreResField.setText(ignoreResExtensionStr);
        flutter2EnableBox.setSelected(storageEntity.flutter2Enable);
        resCheckEnableBox.setSelected(storageEntity.resCheckEnable);
        foldRegisterBox.setSelected(storageEntity.foldRegisterEnable);
        withPackageNameBox.setSelected(storageEntity.registerResWithPackage);
        isUnModifiableFromJson.setSelected(storageEntity.isUnModifiableFromJson);
        apiKeyField.setText(storageEntity.apiKey);
        apiSecretField.setText(storageEntity.apiSecret);
        enableSizeMonitor.setSelected(storageEntity.enableSizeMonitor);
        maxPicSizeField.setText(String.valueOf(storageEntity.maxPicSize));
        maxPicWidthField.setText(String.valueOf(storageEntity.maxPicWidth));
        maxPicHeightField.setText(String.valueOf(storageEntity.maxPicHeight));
        mirrorRepoStr = storageEntity.mirrorRepoStr;
        tinyApiKeyField.setText(storageEntity.tinyApiKey);
        autoTinyImage.setSelected(storageEntity.autoTinyImage);
    }

    @Override
    public void apply() {
        ProjectStorageEntity storageEntity = getStorageEntity();
        String resStr = resDetectField.getText().trim();
        storageEntity.resDir = StringUtils.isEmpty(resStr) ?
                Collections.emptyList() : CollectionUtils.split(resStr, LIST_SPLIT_CHAR);
        String ignoreResExtensionStr = ignoreResField.getText().trim();
        storageEntity.ignoreResExtension = StringUtils.isEmpty(ignoreResExtensionStr) ?
                Collections.emptyList() : CollectionUtils.split(ignoreResExtensionStr, LIST_SPLIT_CHAR);
        storageEntity.flutter2Enable = flutter2EnableBox.isSelected();
        storageEntity.resCheckEnable = resCheckEnableBox.isSelected();
        storageEntity.registerResWithPackage = withPackageNameBox.isSelected();
        storageEntity.isUnModifiableFromJson = isUnModifiableFromJson.isSelected();
        storageEntity.apiKey = apiKeyField.getText().trim();
        storageEntity.apiSecret = apiSecretField.getText().trim();
        storageEntity.enableSizeMonitor = enableSizeMonitor.isSelected();
        storageEntity.maxPicSize = Integer.parseInt(maxPicSizeField.getText().trim());
        storageEntity.maxPicWidth = Integer.parseInt(maxPicWidthField.getText().trim());
        storageEntity.maxPicHeight = Integer.parseInt(maxPicHeightField.getText().trim());
        storageEntity.mirrorRepoStr = mirrorRepoStr;
        //检查是否更新了注册方式
        if (storageEntity.foldRegisterEnable != foldRegisterBox.isSelected()) {
            storageEntity.foldRegisterEnable = foldRegisterBox.isSelected();
            reStartIDE();
        }
        storageEntity.tinyApiKey = tinyApiKeyField.getText().trim();
        storageEntity.autoTinyImage = autoTinyImage.isSelected();
    }

    private void reStartIDE() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            Messages.showMessageDialog(project, "Resource registration method has been modified, IDEA needs to be restarted", "iFlutter Tip", null);
            application.restart();
        }, ModalityState.NON_MODAL);
    }
}
