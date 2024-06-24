package com.xtu.plugin.flutter.action.pub.speed.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidRepoHelper;
import com.xtu.plugin.flutter.action.pub.speed.menu.RepoMenuGroup;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MirrorRepoDialog extends DialogWrapper {

    private final Project project;
    private final JBList<String> listView = new JBList<>();
    private final AddRepoAction addRepoAction = new AddRepoAction();
    private final ConfirmAction confirmAction = new ConfirmAction();

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
        listView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) return;
                Point point = e.getPoint();
                int index = listView.locationToIndex(point);
                if (index < 0 || index >= listModel.size()) return;
                String repo = listModel.get(index);
                RelativePoint showPoint = new RelativePoint(listView, point);
                RepoMenuGroup menuGroup = new RepoMenuGroup(repo, listModel);
                DataContext dataContext = DataManager.getInstance().getDataContext(listView);
                JBPopupFactory.ActionSelectionAid speedSearch = JBPopupFactory.ActionSelectionAid.SPEEDSEARCH;
                JBPopupFactory.getInstance()
                        .createActionGroupPopup(null, menuGroup, dataContext, speedSearch, false)
                        .show(showPoint);
            }
        });
        return listView;
    }

    @Nullable
    @NonNls
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{addRepoAction, confirmAction};
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
