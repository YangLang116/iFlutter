package com.xtu.plugin.flutter.advice;

import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.upgrader.NetworkManager;
import com.xtu.plugin.flutter.utils.ToastUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdviceDialog extends DialogWrapper {

    private static final String APP_KEY = "iFlutter";
    private static final String sURL = "https://iflutter.toolu.cn/api/advice";

    private JPanel rootPanel;
    private JLabel titleLabel;
    private JTextField titleField;
    private JLabel contentLabel;
    private JTextArea contentField;

    public static void show(Project project) {
        AdviceDialog dialog = new AdviceDialog(project);
        boolean isOk = dialog.showAndGet();
        if (!isOk) return;
        String title = dialog.getAdviceTitle();
        String content = dialog.getAdviceContent();
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(content)) {
            ToastUtil.make(project, MessageType.ERROR, "Title or Content is Empty ~");
            return;
        }
        final Gson gson = new Gson();
        final Map<String, String> params = new HashMap<>();
        params.put("app_key", APP_KEY);
        params.put("title", title);
        params.put("content", content);
        NetworkManager.getInstance().post(sURL, gson.toJson(params), new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                ToastUtil.make(project, MessageType.INFO, "thank you for submitting ~");
            }
        });
    }

    private AdviceDialog(@Nullable Project project) {
        super(project, null, false, IdeModalityType.PROJECT);
        setTitle("Suggestion & Feedback");
        initUI();
        init();
    }

    private void initUI() {
        this.rootPanel.setBorder(JBUI.Borders.empty(5));
        this.titleLabel.setBorder(JBUI.Borders.emptyBottom(3));
        this.contentLabel.setBorder(JBUI.Borders.empty(5, 0, 3, 0));
        this.contentField.setBorder(JBUI.Borders.empty(3, 5));
    }

    @Override
    protected @Nullable
    @NonNls String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return this.titleField;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.rootPanel;
    }

    private String getAdviceTitle() {
        return this.titleField.getText();
    }

    private String getAdviceContent() {
        return this.contentField.getText();
    }
}
