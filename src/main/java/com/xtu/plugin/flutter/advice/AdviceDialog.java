package com.xtu.plugin.flutter.advice;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AdviceDialog extends DialogWrapper {

    private JPanel rootPanel;
    private JLabel titleLabel;
    private JTextField titleField;
    private JLabel contentLabel;
    private JTextArea contentField;

    public static void show(@NotNull Project project) {
        AdviceDialog dialog = new AdviceDialog(project);
        boolean isOk = dialog.showAndGet();
        if (!isOk) return;
        String title = dialog.getAdviceTitle();
        String content = dialog.getAdviceContent();
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(content)) {
            ToastUtils.make(project, MessageType.ERROR, "Title or Content is Empty ~");
            return;
        }
        AdviceManager.getInstance().submitAdvice(project, title, content);
    }

    private AdviceDialog(@Nullable Project project) {
        super(project, null, false, IdeModalityType.IDE);
        setHorizontalStretch(1.5f);
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
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return this.titleField;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        return this.rootPanel;
    }

    private String getAdviceTitle() {
        return this.titleField.getText();
    }

    private String getAdviceContent() {
        return this.contentField.getText();
    }
}
