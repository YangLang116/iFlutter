package com.xtu.plugin.flutter.action.j2d.ui;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.action.j2d.generate.J2DGenerator;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class J2DDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile selectDirectory;
    private JTextField classNameFiled;

    public J2DDialog(@Nullable Project project, VirtualFile selectDirectory) {
        super(project, null, false, IdeModalityType.PROJECT, false);
        setHorizontalStretch(3);
        setVerticalStretch(2);
        this.project = project;
        this.selectDirectory = selectDirectory;
        init();
    }

    @Override
    protected @Nullable
    String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return classNameFiled;
    }

    @Override
    protected @Nullable
    JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(JBUI.Borders.empty(5));
        //编辑类名
        Box classNameContainer = Box.createHorizontalBox();
        classNameContainer.add(new JLabel("ClassName:"));
        classNameContainer.add(Box.createHorizontalStrut(10));
        classNameFiled = new JTextField();
        classNameFiled.setToolTipText("采用驼峰命名");
        classNameContainer.add(classNameFiled);
        mainPanel.add(classNameContainer, BorderLayout.NORTH);
        //添加json数据
        Box jsonContainer = Box.createVerticalBox();
        jsonContainer.setBorder(JBUI.Borders.empty(10, 0, 5, 0));
        JLabel jsonLabel = new JLabel("Json Data：");
        jsonLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsonContainer.add(jsonLabel);
        jsonContainer.add(Box.createVerticalStrut(10));
        final JTextPane jsonArea = new JTextPane();
        JBScrollPane scrollPane = new JBScrollPane(jsonArea);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        jsonContainer.add(scrollPane);
        mainPanel.add(jsonContainer, BorderLayout.CENTER);
        //确认按钮
        Box bottomContainer = Box.createHorizontalBox();
        bottomContainer.add(Box.createHorizontalGlue());
        JButton formatButton = new JButton("Format");
        formatButton.addActionListener(e -> {
            String jsonData = jsonArea.getText();
            if (StringUtils.isEmpty(jsonData)) return;
            try {
                String formatData;
                if (jsonData.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(jsonData);
                    formatData = jsonArray.toString(4);
                } else {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    formatData = jsonObject.toString(4);
                }
                jsonArea.setText(formatData);
            } catch (JSONException ex) {
                LogUtils.error("J2DDialog createCenterPanel: " + ex.getMessage());
                ToastUtil.make(project, MessageType.ERROR, ex.getMessage());
            }
        });
        bottomContainer.add(formatButton);
        bottomContainer.add(Box.createHorizontalStrut(10));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            String className = classNameFiled.getText();
            String jsonData = jsonArea.getText();
            boolean enableFlutter2 = StorageService.getInstance(project)
                    .getState()
                    .flutter2Enable;
            generateDart(enableFlutter2, className, jsonData);
            close(OK_EXIT_CODE);
        });
        bottomContainer.add(okButton);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);
        return mainPanel;
    }

    private void generateDart(boolean enableFlutter2, String className, String jsonData) {
        if (StringUtils.isEmpty(className) || StringUtils.isEmpty(jsonData)) {
            ToastUtil.make(project, MessageType.WARNING, "ClassName or Json Data is Empty");
            return;
        }
        if (jsonData.startsWith("[")) {
            ToastUtil.make(project, MessageType.ERROR, "Can not Support jsonArray to Dart");
            return;
        }
        String fileName = StringUtil.splashName(className) + ".dart";
        VirtualFile childFile = selectDirectory.findChild(fileName);
        if (childFile != null) {
            ToastUtil.make(project, MessageType.ERROR, "Dart Bean is Exist");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            J2DGenerator generator = new J2DGenerator(enableFlutter2);
            final String result = generator.generate(className, jsonObject);
            WriteAction.run(() -> writeTask(project, selectDirectory, fileName, result));
        } catch (Exception e) {
            ToastUtil.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private void writeTask(Project project, VirtualFile selectDirectory, String childFileName, String content) {
        DartUtils.createDartFile(project, selectDirectory, childFileName, content, new DartUtils.OnCreateDartFileListener() {
            @Override
            public void onSuccess(@NotNull VirtualFile virtualFile) {
                //更新交互UI
                StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                if (statusBar != null) {
                    statusBar.setInfo("Dart Entity Create Completed");
                }
                OpenFileDescriptor descriptor = new OpenFileDescriptor(project, virtualFile);
                FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            }

            @Override
            public void onFail(String message) {
                ToastUtil.make(project, MessageType.ERROR, message);
            }
        });
    }
}
