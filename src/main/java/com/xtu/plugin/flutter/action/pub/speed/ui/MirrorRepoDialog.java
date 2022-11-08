package com.xtu.plugin.flutter.action.pub.speed.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.action.pub.speed.helper.AndroidGradleMaker;
import com.xtu.plugin.flutter.service.StorageService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MirrorRepoDialog extends DialogWrapper {

    private final Project project;
    private DefaultListModel<String> listModel;
    private JBList<String> listView;
    private final Action addRepoAction = new MirrorRepoDialog.AddRepoAction();
    private final Action confirmAction = new MirrorRepoDialog.ConfirmAction();

    public MirrorRepoDialog(@NotNull Project project) {
        super(project, null, true, IdeModalityType.PROJECT, true);
        this.project = project;
        setTitle("Mirror Repo Setting");
        setVerticalStretch(2);
        init();
    }

    private List<String> getRepoList() {
        StorageService storageService = StorageService.getInstance(project);
        String mirrorRepoStr = storageService.getState().mirrorRepoStr;
        String[] repoList = mirrorRepoStr.split(AndroidGradleMaker.REPO_SPLIT);
        return Arrays.asList(repoList);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        listView = new JBList<>();
        listModel = new DefaultListModel<>();
        listModel.addAll(getRepoList());
        listView.setModel(listModel);
        listView.setCellRenderer(new RepoListRender());
        listView.addMouseListener(new RepoMouseListener());
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
        return new Action[]{addRepoAction, confirmAction};
    }

    public String getRepoStr() {
        List<String> repoList = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            String repo = listModel.elementAt(i);
            repoList.add(repo);
        }
        return StringUtils.join(repoList, AndroidGradleMaker.REPO_SPLIT);
    }

    private void addRepo() {
        String repo = Messages.showInputDialog(project, "", "Add Mirror Repo Url", null);
        if (repo == null) return;
        listModel.addElement(repo);
    }

    private String getRepoByPoint(Point point) {
        int index = listView.locationToIndex(point);
        if (index < 0 || index >= listModel.size()) return null;
        return listModel.elementAt(index);
    }

    private void showContextMenu(Component component, Point point) {
        JPopupMenu popupMenu = new JPopupMenu();
        //添加删除按钮
        JMenuItem deleteItem = new JMenuItem("删除");
        deleteItem.addActionListener(e -> {
            String repo = getRepoByPoint(point);
            if (repo != null) listModel.removeElement(repo);
        });
        popupMenu.add(deleteItem);
        popupMenu.show(component, (int) point.getX(), (int) point.getY());
    }

    private class RepoMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                showContextMenu(e.getComponent(), e.getPoint());
            }
        }
    }

    private static class RepoListRender implements ListCellRenderer<String> {

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            Box rootBox = Box.createVerticalBox();
            //添加Repo展示
            JLabel repoView = new JLabel(value);
            repoView.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
            repoView.setBorder(JBUI.Borders.empty(5, 16));
            rootBox.add(repoView);
            //添加分割线
            JSeparator jSeparator = new JSeparator(SwingConstants.CENTER);
            jSeparator.setPreferredSize(new Dimension(rootBox.getWidth(), 10));
            rootBox.add(jSeparator);
            return rootBox;
        }
    }

    private class AddRepoAction extends DialogWrapper.DialogWrapperAction {

        protected AddRepoAction() {
            super("添加");
        }

        @Override
        protected void doAction(ActionEvent e) {
            addRepo();
        }
    }

    private class ConfirmAction extends DialogWrapper.DialogWrapperAction {

        protected ConfirmAction() {
            super("确认");
        }

        @Override
        protected void doAction(ActionEvent e) {
            close(DialogWrapper.OK_EXIT_CODE);
        }
    }
}
