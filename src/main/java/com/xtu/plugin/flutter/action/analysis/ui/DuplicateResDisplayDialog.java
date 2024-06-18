package com.xtu.plugin.flutter.action.analysis.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.xtu.plugin.flutter.utils.ToastUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class DuplicateResDisplayDialog extends DialogWrapper {

    public static void show(@NotNull Project project, @NotNull Map<String, List<File>> duplicateData) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JFrame mainFrame = WindowManager.getInstance().getFrame(project);
            new DuplicateResDisplayDialog(project, mainFrame, duplicateData).show();
        });
    }

    private final Project project;
    private final Map<String, List<File>> duplicateData;
    private final Action copyAction;

    private DuplicateResDisplayDialog(@Nullable Project project, Component mainFrame, Map<String, List<File>> duplicateData) {
        super(project, mainFrame, false, IdeModalityType.PROJECT);
        this.project = project;
        this.duplicateData = duplicateData;
        this.copyAction = new CopyAction();
        this.setHorizontalStretch(2.0f);
        this.setVerticalStretch(1.2f);
        setTitle("Duplicate Resources in Project");
        init();
    }

    @Nullable
    @NonNls
    @Override
    protected String getDimensionServiceKey() {
        return DuplicateResDisplayDialog.class.getSimpleName();
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{copyAction, getOKAction()};
    }

    @Override
    @Nullable
    protected JComponent createCenterPanel() {
        //assemble data
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Duplicate File List");
        for (Map.Entry<String, List<File>> entry : this.duplicateData.entrySet()) {
            DefaultMutableTreeNode duplicateNode = new DefaultMutableTreeNode("md5: " + entry.getKey());
            for (File file : entry.getValue()) {
                duplicateNode.add(new DefaultMutableTreeNode(new DuplicateFile(file)));
            }
            rootNode.add(duplicateNode);
        }
        Tree tree = new Tree(rootNode);
        tree.setCellRenderer(new DefaultTreeCellRenderer());
        return new JBScrollPane(tree);
    }

    private static class DuplicateFile {

        private final File rawFile;

        public DuplicateFile(File file) {
            this.rawFile = file;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "%s(%s)", rawFile.getName(), rawFile.getAbsolutePath());
        }
    }

    private class CopyAction extends DialogWrapperAction {

        protected CopyAction() {
            super("Copy");
        }

        @Override
        protected void doAction(ActionEvent e) {
            StringSelection stringSelection = new StringSelection(getDuplicateDateStr());
            CopyPasteManager.getInstance().setContents(stringSelection);
            ToastUtils.make(project, MessageType.INFO, "copy success");
            close(DialogWrapper.OK_EXIT_CODE);
        }

        private String getDuplicateDateStr() {
            Map<String, List<File>> duplicateData = DuplicateResDisplayDialog.this.duplicateData;
            int index = 1;
            StringBuilder resultBuilder = new StringBuilder();
            for (Map.Entry<String, List<File>> entry : duplicateData.entrySet()) {
                resultBuilder.append(index++).append("、").append(entry.getKey()).append(":").append("\n");
                for (File file : entry.getValue()) {
                    resultBuilder.append("  - ").append(file.getAbsolutePath()).append("\n");
                }
                resultBuilder.append("\n");
            }
            return resultBuilder.toString();
        }
    }
}
