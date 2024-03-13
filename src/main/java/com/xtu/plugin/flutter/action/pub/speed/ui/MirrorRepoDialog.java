package com.xtu.plugin.flutter.action.pub.speed.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidRepoHelper;
import com.xtu.plugin.flutter.action.pub.speed.menu.RepoMouseListener;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MirrorRepoDialog extends DialogWrapper {

    private final Project project;
    private final JBList<String> listView = new JBList<>();

    public MirrorRepoDialog(@NotNull Project project, @NotNull Component parentComponent) {
        super(project, parentComponent, true, IdeModalityType.PROJECT, true);
        this.project = project;
        setTitle("Mirror Repo Setting");
        setVerticalStretch(2);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<String> repoList = AndroidRepoHelper.getRepoList(project);
        if (!CollectionUtils.isEmpty(repoList)) {
            listModel.addAll(repoList);
        }
        listView.setModel(listModel);
        listView.setCellRenderer(new RepoListRender());
        listView.addMouseListener(new RepoMouseListener(listView));
        return listView;
    }

    @Nullable
    @NonNls
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    @NotNull
    protected Action[] createActions() {
        return new Action[]{
                new MirrorRepoDialog.AddRepoAction(),
                new MirrorRepoDialog.ConfirmAction()
        };
    }

    public String getRepoStr() {
        DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
        if (model == null) return "";
        List<String> repoList = new ArrayList<>();
        for (int i = 0; i < model.size(); i++) {
            String repo = model.getElementAt(i);
            if (StringUtils.isEmpty(repo)) continue;
            repoList.add(repo);
        }
        return AndroidRepoHelper.getRepoStr(repoList);
    }


    private class AddRepoAction extends DialogWrapper.DialogWrapperAction {

        protected AddRepoAction() {
            super("Add");
        }

        @Override
        protected void doAction(ActionEvent e) {
            String repo = Messages.showInputDialog(project, "", "Add Mirror Repo Url", null);
            if (StringUtils.isEmpty(repo)) return;
            DefaultListModel<String> model = (DefaultListModel<String>) listView.getModel();
            if (model == null) return;
            model.addElement(repo);
        }
    }

    private class ConfirmAction extends DialogWrapper.DialogWrapperAction {

        protected ConfirmAction() {
            super("OK");
        }

        @Override
        protected void doAction(ActionEvent e) {
            close(DialogWrapper.OK_EXIT_CODE);
        }
    }
}
