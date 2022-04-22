package com.xtu.plugin.flutter.action.intl.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.action.intl.translate.TransApi;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.service.StorageEntity;
import com.xtu.plugin.flutter.service.StorageService;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddIntlDialog extends DialogWrapper {

    private boolean hasTranslate;
    private JCheckBox replaceBox;
    private JTextField keyTextField;
    private Project project;
    private final TransApi transApi;
    private final List<String> localeList;
    private final Map<String, JTextField> localeFieldMap = new HashMap<>();

    public AddIntlDialog(@Nullable Project project, @NotNull List<String> localeList) {
        super(project, null, false, IdeModalityType.PROJECT, true);
        this.project = project;
        this.localeList = localeList;
        this.transApi = initTransApi();
        init();
    }

    private TransApi initTransApi() {
        StorageService storageService = StorageService.getInstance(project);
        StorageEntity storageEntity = storageService.getState();
        String apiId = storageEntity.apiKey;
        String apiSecret = storageEntity.apiSecret;
        return new TransApi(apiId, apiSecret);
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
            localeLabel.setPreferredSize(new Dimension(30, 0));
            localeItemBox.add(localeLabel);
            localeItemBox.add(Box.createHorizontalStrut(10));
            JTextField localeTextField = new JTextField();
            localeTextField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (hasTranslate) return;
                    String inputStr = localeTextField.getText().trim();
                    if (StringUtils.isEmpty(inputStr)) return;
                    hasTranslate = true;
                    translateLocaleList(locale, localeTextField);
                }
            });
            localeItemBox.add(localeTextField);
            localeFieldMap.put(locale, localeTextField);
            rootPanel.add(localeItemBox);
        }
        //replace key
        rootPanel.add(Box.createVerticalStrut(5));
        replaceBox = new JCheckBox();
        replaceBox.setText("Can Replace Key");
        replaceBox.setSelected(false);
        replaceBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        rootPanel.add(replaceBox);
        return rootPanel;
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
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

    public boolean canReplaceKey() {
        return this.replaceBox.isSelected();
    }

    public Map<String, String> getLocaleMap() {
        Map<String, String> localeMap = new HashMap<>();
        for (Map.Entry<String, JTextField> entry : this.localeFieldMap.entrySet()) {
            String value = entry.getValue().getText().trim();
            localeMap.put(entry.getKey(), value);
        }
        return localeMap;
    }

    private void translateLocaleList(@NotNull String sourceLocale,
                                     @NotNull JTextField sourceTextField) {
        String word = sourceTextField.getText().trim();
        for (Map.Entry<String, JTextField> textFieldEntry : localeFieldMap.entrySet()) {
            String locale = textFieldEntry.getKey();
            JTextField textField = textFieldEntry.getValue();
            if (textField == sourceTextField) continue;
            translateLocale(word, sourceLocale, locale, textField);
        }
    }

    private void translateLocale(@NotNull String word,
                                 @NotNull String sourceLocale,
                                 @NotNull String destLocale,
                                 @NotNull JTextField destTextField) {
        String from = IntlUtils.transCodeFromLocale(sourceLocale);
        String to = IntlUtils.transCodeFromLocale(destLocale);
        this.transApi.getTransResult(word, from, to, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //ignore
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body == null) return;
                String content = body.string();
                if (StringUtils.isEmpty(content)) return;
                fillResultToUI(content, destTextField);
            }
        });
    }

    private String parseResult(@NotNull String content) {
        //{"from":"zh","to":"en","trans_result":[{"src":"\u6d4b\u8bd5","dst":"test"}]}
        try {
            JSONObject resultJson = new JSONObject(content);
            JSONArray transArray = resultJson.optJSONArray("trans_result");
            if (transArray == null || transArray.length() <= 0) return null;
            JSONObject transJson = transArray.optJSONObject(0);
            return transJson.optString("dst");
        } catch (JSONException e) {
            return null;
        }
    }

    private void fillResultToUI(@NotNull String content, @NotNull JTextField destTextField) {
        String result = parseResult(content);
        if (StringUtils.isEmpty(result)) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            String oldContent = destTextField.getText().trim();
            if (!StringUtils.isEmpty(oldContent)) return;
            destTextField.setText(result);
        }, ModalityState.any());
    }
}
