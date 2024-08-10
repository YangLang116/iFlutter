package com.xtu.plugin.flutter.action.mock.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.xtu.plugin.flutter.action.mock.callback.IHttpListComponent;
import com.xtu.plugin.flutter.action.mock.manager.HttpMockManager;
import com.xtu.plugin.flutter.action.mock.menu.HttpMenuGroup;
import com.xtu.plugin.flutter.action.mock.ui.render.HttpListRender;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import com.xtu.plugin.flutter.store.project.entity.HttpEntity;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

public class HttpListDialog extends DialogWrapper implements IHttpListComponent {

    private JPanel rootView;
    private JTextField hostView;
    private JPanel listContainer;
    private final JBList<HttpEntity> listView = new JBList<>();
    private final DefaultListModel<HttpEntity> listModel = new DefaultListModel<>();
    private final Project project;
    private final Action createConfigAction = new CreateConfigAction();
    private final Action confirmAction = new ConfirmAction();

    public HttpListDialog(@Nullable Project project) {
        super(project, true, IdeModalityType.PROJECT);
        this.project = project;
        setTitle("HTTP Mock");
        setVerticalStretch(2);
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        String baseUrl = HttpMockManager.getService(project).getBaseUrl();
        hostView.setText(StringUtils.isEmpty(baseUrl) ? "HTTP Mock fail" : baseUrl);
        List<HttpEntity> httpConfigList = getHttpConfigList();
        listView.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                int index = listView.locationToIndex(point);
                if (index < 0 || index >= httpConfigList.size()) return;
                HttpEntity httpEntity = httpConfigList.get(index);
                if (SwingUtilities.isLeftMouseButton(e)) {
                    modifyHttpConfig(httpEntity);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    RelativePoint showPoint = new RelativePoint(listView, point);
                    HttpMenuGroup menuGroup = new HttpMenuGroup(httpEntity, HttpListDialog.this);
                    DataContext dataContext = DataManager.getInstance().getDataContext(listView);
                    JBPopupFactory.ActionSelectionAid speedSearch = JBPopupFactory.ActionSelectionAid.SPEEDSEARCH;
                    JBPopupFactory.getInstance()
                            .createActionGroupPopup(null, menuGroup, dataContext, speedSearch, false)
                            .show(showPoint);
                }
            }
        });
        listView.setCellRenderer(new HttpListRender());
        listModel.addAll(httpConfigList);
        listView.setModel(listModel);
        listContainer.add(new JBScrollPane(listView), BorderLayout.CENTER);
        return rootView;
    }

    @Override
    @NotNull
    public List<HttpEntity> getHttpConfigList() {
        return ProjectStorageService.getStorage(project).httpEntityList;
    }

    @Override
    public void modifyHttpConfig(@Nullable HttpEntity httpEntity) {
        HttpConfigDialog httpConfigDialog = new HttpConfigDialog(project, httpEntity);
        boolean isOk = httpConfigDialog.showAndGet();
        if (!isOk) return;
        HttpEntity resultEntity = httpConfigDialog.getResultEntity();
        Optional<HttpEntity> findEntityOption = getHttpConfigList()
                .stream().filter(entity -> StringUtils.equals(entity.path, resultEntity.path))
                .findFirst();
        findEntityOption.ifPresent(this::deleteHttpConfig);
        addHttpConfig(resultEntity);
        listView.ensureIndexIsVisible(0); //滑动到最顶部
    }

    @Override
    public void deleteHttpConfig(@NotNull HttpEntity httpEntity) {
        listModel.removeElement(httpEntity);
        getHttpConfigList().remove(httpEntity);
    }

    @Override
    public void addHttpConfig(@NotNull HttpEntity httpEntity) {
        listModel.add(0, httpEntity);
        getHttpConfigList().add(0, httpEntity);
    }

    @Nullable
    @NonNls
    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[]{createConfigAction, confirmAction};
    }

    private class CreateConfigAction extends DialogWrapper.DialogWrapperAction {

        protected CreateConfigAction() {
            super("Create");
        }

        @Override
        protected void doAction(ActionEvent e) {
            modifyHttpConfig(null);
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
