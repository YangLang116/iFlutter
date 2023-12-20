package com.xtu.plugin.flutter.action.analysis.ui;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.xtu.plugin.flutter.utils.ToastUtil;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class AnalysisResultDialog extends DialogWrapper {

    private final Project project;
    private final String message;
    private final String searchTitle;
    private final String searchMessage;
    private final JTextPane contentComponent;
    private final Color defaultTextColor;
    private final Action copyAction;

    public AnalysisResultDialog(@Nullable Project project, String message, String searchTitle, String searchMessage) {
        super(project, null, true, IdeModalityType.PROJECT, true);
        this.project = project;
        this.message = message;
        this.searchTitle = searchTitle;
        this.searchMessage = searchMessage;
        this.copyAction = new CopyAction();
        //content component
        this.contentComponent = new JTextPane();
        contentComponent.setEditable(false);
        contentComponent.setText(message);
        contentComponent.registerKeyboardAction(e -> searchKeyword(contentComponent),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, (SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK)),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        this.defaultTextColor = this.contentComponent.getForeground();
        init();
    }

    @Override
    @Nullable
    @NonNls
    protected String getDimensionServiceKey() {
        return getClass().getSimpleName();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return contentComponent;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{copyAction, getOKAction()};
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return new JBScrollPane(contentComponent);
    }

    private void searchKeyword(JTextPane textPane) {
        String keyWord = Messages.showInputDialog(textPane, this.searchMessage, this.searchTitle, null);
        if (StringUtils.isEmpty(keyWord)) return;
        int keyLen = keyWord.length();
        int fromIndex = 0;
        List<Pair<Integer, Integer>> keyPointList = new ArrayList<>();
        while (true) {
            int index = this.message.indexOf(keyWord, fromIndex);
            if (index == -1) break;
            keyPointList.add(new Pair<>(index, keyLen));
            fromIndex = index + keyLen;
        }
        if (keyPointList.size() < 1) return;
        StyledDocument styledDocument = textPane.getStyledDocument();
        //清除高亮
        AttributeSet resetAttribute = getAttributeSet(false);
        styledDocument.setCharacterAttributes(0, this.message.length(), resetAttribute, false);
        //设置搜索高亮
        AttributeSet selectAttribute = getAttributeSet(true);
        for (Pair<Integer, Integer> keyPoint : keyPointList) {
            styledDocument.setCharacterAttributes(keyPoint.first, keyPoint.second, selectAttribute, false);
        }
    }

    private AttributeSet getAttributeSet(boolean isSelect) {
        SimpleAttributeSet attributeSet = new SimpleAttributeSet();
        StyleConstants.setForeground(attributeSet, isSelect ? JBColor.BLUE : defaultTextColor);
        StyleConstants.setBold(attributeSet, isSelect);
        return attributeSet;
    }

    private class CopyAction extends DialogWrapperAction {

        protected CopyAction() {
            super("Copy");
        }

        @Override
        protected void doAction(ActionEvent e) {
            StringSelection stringSelection = new StringSelection(AnalysisResultDialog.this.message);
            CopyPasteManager.getInstance().setContents(stringSelection);
            ToastUtil.make(AnalysisResultDialog.this.project, MessageType.INFO, "copy success");
            AnalysisResultDialog.this.close(OK_EXIT_CODE);
        }
    }
}
