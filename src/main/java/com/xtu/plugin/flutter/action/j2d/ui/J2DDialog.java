package com.xtu.plugin.flutter.action.j2d.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.action.j2d.handler.J2DHandler;
import com.xtu.plugin.flutter.base.utils.*;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.ProjectStorageEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class J2DDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile selectDirectory;

    private JTextComponent classNameFiled;
    private JTextComponent jsonDataField;

    public J2DDialog(@NotNull Project project, @NotNull VirtualFile selectDirectory) {
        super(project, null, false, IdeModalityType.PROJECT, false);
        this.project = project;
        this.selectDirectory = selectDirectory;
        setHorizontalStretch(3);
        setVerticalStretch(2);
        init();
    }

    @Override
    @Nullable
    protected String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return classNameFiled;
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(JBUI.Borders.empty(5));
        mainPanel.add(createClassNameBox(), BorderLayout.NORTH);  //编辑类名
        mainPanel.add(createJsonDataBox(), BorderLayout.CENTER);  //添加json数据
        mainPanel.add(createBtnList(), BorderLayout.SOUTH); //操作按钮
        return mainPanel;
    }

    @NotNull
    private Box createClassNameBox() {
        Box classNameContainer = Box.createHorizontalBox();
        classNameContainer.add(new JLabel("ClassName:"));
        classNameContainer.add(Box.createHorizontalStrut(10));
        classNameFiled = new JTextField();
        classNameFiled.setToolTipText("Using Camel-Case");
        classNameContainer.add(classNameFiled);
        return classNameContainer;
    }

    @NotNull
    private Box createJsonDataBox() {
        Box jsonContainer = Box.createVerticalBox();
        jsonContainer.setBorder(JBUI.Borders.empty(10, 0, 5, 0));
        JLabel jsonLabel = new JLabel("Json Data: ");
        jsonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsonContainer.add(jsonLabel);
        jsonContainer.add(Box.createVerticalStrut(10));
        jsonDataField = new JTextPane();
        JBScrollPane scrollPane = new JBScrollPane(jsonDataField);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsonContainer.add(scrollPane);
        return jsonContainer;
    }

    @NotNull
    private Box createBtnList() {
        Box btnBox = Box.createHorizontalBox();
        btnBox.add(Box.createHorizontalGlue());
        btnBox.add(createBtn("Format", this::toFormat));
        btnBox.add(Box.createHorizontalStrut(10));
        btnBox.add(createBtn("OK", this::genCode));
        return btnBox;
    }

    @NotNull
    private JComponent createBtn(@NotNull String btnText, @NotNull OnBtnClickListener clickListener) {
        JButton btn = new JButton(btnText);
        btn.addActionListener(e -> {
            String className = classNameFiled.getText();
            String jsonData = jsonDataField.getText();
            clickListener.onClick(className, jsonData);
        });
        return btn;
    }

    private void toFormat(@Nullable String className, @Nullable String jsonData) {
        if (StringUtils.isEmpty(jsonData)) {
            ToastUtils.make(project, MessageType.ERROR, "json data is empty");
            return;
        }
        try {
            String formatJson = J2DHandler.formatJson(jsonData.trim());
            jsonDataField.setText(formatJson);
        } catch (Exception ex) {
            LogUtils.error("J2DDialog format: ", ex);
            ToastUtils.make(project, MessageType.ERROR, ex.getMessage());
        }
    }

    private void genCode(@Nullable String className, @Nullable String jsonData) {
        if (StringUtils.isEmpty(className) || StringUtils.isEmpty(jsonData)) {
            ToastUtils.make(project, MessageType.ERROR, "className or json data is empty");
            return;
        }
        className = className.trim();
        jsonData = jsonData.trim();
        if (jsonData.startsWith("[")) {
            ToastUtils.make(project, MessageType.ERROR, "can not support jsonArray to dart");
            return;
        }
        String fileName = ClassUtils.splashName(className) + ".dart";
        VirtualFile childFile = selectDirectory.findChild(fileName);
        if (childFile != null) {
            ToastUtils.make(project, MessageType.ERROR, "dart bean is exist");
            return;
        }
        ProjectStorageEntity storage = ProjectStorageService.getStorage(project);
        J2DHandler handler = new J2DHandler(storage.supportNullSafety, storage.isUnModifiableFromJson);
        try {
            String code = handler.genCode(className, jsonData);
            genDartFile(fileName, code);
            close(OK_EXIT_CODE);
        } catch (Exception e) {
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private void genDartFile(@NotNull String fileName, @NotNull String code) {
        DartUtils.createDartFile(project, selectDirectory, fileName, code, virtualFile -> {
            //更新交互UI
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) statusBar.setInfo("Dart Entity Create Completed");
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        });
    }

    interface OnBtnClickListener {
        void onClick(@Nullable String className, @Nullable String jsonData);
    }
}
