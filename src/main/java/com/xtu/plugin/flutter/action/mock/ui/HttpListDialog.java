package com.xtu.plugin.flutter.action.mock.ui;

import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.MessageType;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.store.HttpEntity;
import com.xtu.plugin.flutter.store.StorageService;
import com.xtu.plugin.flutter.utils.StringUtils;
import com.xtu.plugin.flutter.utils.ToastUtils;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class HttpListDialog extends DialogWrapper implements ListSelectionListener {

    private JPanel rootView;
    private JTextField hostView;
    private JScrollPane listViewContainer;
    private JBList<HttpEntity> listView;

    private boolean ignoreDataChanged;
    private final Project project;
    private DefaultListModel<HttpEntity> listModel;
    private final Action createConfigAction = new CreateConfigAction();
    private final Action confirmAction = new ConfirmAction();


    public HttpListDialog(@Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        setTitle("Http Mock");
        setVerticalStretch(2);
        init();
    }

    private List<HttpEntity> getHttpConfigList() {
        return StorageService.getInstance(project).getState().httpEntityList;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        String baseUrl = HttpMockManager.getService(project).getBaseUrl();
        hostView.setText(StringUtils.isEmpty(baseUrl) ? "Http Mock fail" : baseUrl);
        listView = new JBList<>();
        listModel = new DefaultListModel<>();
        listModel.addAll(getHttpConfigList());
        listView.setModel(listModel);
        listView.setCellRenderer(new HttpListRender());
        listView.addListSelectionListener(this);
        listView.addMouseListener(new HttpMouseListener());
        listViewContainer.setViewportView(listView);
        return rootView;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (ignoreDataChanged) return;
        HttpEntity httpEntity = listView.getSelectedValue();
        createOrModifyConfig(httpEntity);
    }

    private void createOrModifyConfig(@Nullable HttpEntity httpEntity) {
        ignoreDataChanged = true;
        HttpConfigDialog httpConfigDialog = new HttpConfigDialog(project, httpEntity);
        boolean isOk = httpConfigDialog.showAndGet();
        if (isOk) {
            HttpEntity resultEntity = httpConfigDialog.getResultEntity();
            List<HttpEntity> httpEntityList = getHttpConfigList();
            HttpEntity oldEntity = null;
            for (HttpEntity entity : httpEntityList) {
                if (StringUtils.equals(entity.path, resultEntity.path)) {
                    oldEntity = entity;
                    break;
                }
            }
            if (oldEntity != null) {
                listModel.removeElement(oldEntity);
                httpEntityList.remove(oldEntity);
            }
            listModel.add(0, resultEntity);
            httpEntityList.add(0, resultEntity);
            //滑动到最顶部
            listViewContainer.getVerticalScrollBar().setValue(0);
        }
        ignoreDataChanged = false;
    }

    private HttpEntity getHttpEntityByPoint(Point point) {
        int index = listView.locationToIndex(point);
        List<HttpEntity> httpConfigList = getHttpConfigList();
        if (index < 0 || index >= httpConfigList.size()) return null;
        return httpConfigList.get(index);
    }

    private void showContextMenu(Component component, Point point) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(createCopyMenuItem(point));
        popupMenu.add(createDeleteMenuItem(point));
        popupMenu.show(component, (int) point.getX(), (int) point.getY());
    }

    @NotNull
    private JMenuItem createDeleteMenuItem(Point point) {
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            ignoreDataChanged = true;
            List<HttpEntity> httpConfigList = getHttpConfigList();
            HttpEntity httpEntity = getHttpEntityByPoint(point);
            if (httpEntity != null) {
                httpConfigList.remove(httpEntity);
                listModel.removeElement(httpEntity);
            }
            ignoreDataChanged = false;
        });
        return deleteItem;
    }

    @NotNull
    private JMenuItem createCopyMenuItem(Point point) {
        JMenuItem copyPathMenu = new JMenuItem("Copy Path");
        copyPathMenu.addActionListener(e -> {
            HttpEntity httpEntity = getHttpEntityByPoint(point);
            if (httpEntity == null) return;
            HttpMockManager mockManager = HttpMockManager.getService(project);
            String url = mockManager.getUrl(httpEntity.path);
            if (StringUtils.isEmpty(url)) {
                ToastUtils.make(project, MessageType.ERROR, "mock server fail");
            } else {
                ToastUtils.make(project, MessageType.INFO, "copy path success");
                CopyPasteManager.getInstance().setContents(new StringSelection(url));
                close(DialogWrapper.OK_EXIT_CODE);
            }
        });
        return copyPathMenu;
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
        return new Action[]{createConfigAction, confirmAction};
    }

    private class HttpMouseListener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                showContextMenu(e.getComponent(), e.getPoint());
            }
        }
    }

    static class HttpListRender implements ListCellRenderer<HttpEntity> {

        @Override
        public Component getListCellRendererComponent(JList<? extends HttpEntity> list,
                                                      HttpEntity value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            Box rootBox = Box.createVerticalBox();
            //内容区域
            Box contentBox = Box.createVerticalBox();
            contentBox.setBorder(JBUI.Borders.empty(5, 16));
            //添加path展示
            JLabel pathView = new JLabel(value.path);
            pathView.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
            contentBox.add(pathView);
            //添加说明信息
            contentBox.add(Box.createVerticalStrut(3));
            JLabel descriptionView = new JLabel(value.description);
            descriptionView.setFont(new Font(Font.DIALOG, Font.PLAIN, 12));
            contentBox.add(descriptionView);

            rootBox.add(contentBox);
            //添加分割线
            JSeparator jSeparator = new JSeparator(SwingConstants.CENTER);
            jSeparator.setPreferredSize(new Dimension(rootBox.getWidth(), 10));
            rootBox.add(jSeparator);

            return rootBox;
        }
    }

    private class CreateConfigAction extends DialogWrapper.DialogWrapperAction {

        protected CreateConfigAction() {
            super("Create");
        }

        @Override
        protected void doAction(ActionEvent e) {
            createOrModifyConfig(null);
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
