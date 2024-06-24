package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.utils.*;
import com.xtu.plugin.flutter.window.res.adapter.ResListRender;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import com.xtu.plugin.flutter.window.res.menu.ResMenuGroup;
import com.xtu.plugin.flutter.window.res.sort.SortType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ResRootPanel extends JPanel implements IResRootPanel {

    private final Project project;
    private JLabel titleBar;
    private JComponent searchBar;
    private JTextField searchField;
    private JBList<File> listComponent;
    private ResListRender listAdapter;

    private String keyword;
    private SortType sortType;
    private List<File> resList;
    private boolean showSearchBar;

    public ResRootPanel(@NotNull Project project, boolean showSearchBar, @NotNull SortType sortType) {
        this.project = project;
        this.sortType = sortType;
        this.setLayout(new BorderLayout());
        this.registerKeyboardListener();
        this.showTopBar(showSearchBar);
        this.addListView();
    }

    private void registerKeyboardListener() {
        KeyStroke escKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        this.registerKeyboardAction(e -> {
            if (!this.showSearchBar) return;
            this.searchRes();
        }, escKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
        int modifiers = SystemInfo.isMac ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
        KeyStroke searchKey = KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers);
        this.registerKeyboardAction(e -> {
            if (this.showSearchBar) return;
            this.searchRes();
        }, searchKey, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void showTopBar(boolean showSearchBar) {
        this.showSearchBar = showSearchBar;
        if (this.titleBar == null) this.titleBar = createTitleBar();
        if (this.searchBar == null) this.searchBar = createSearchBar();
        this.remove(this.titleBar);
        this.remove(this.searchBar);
        if (showSearchBar) {
            this.add(this.searchBar, BorderLayout.NORTH);
        } else {
            this.add(this.titleBar, BorderLayout.NORTH);
            this.searchField.setText(null);
        }
    }

    private JComponent createSearchBar() {
        Box searchBar = Box.createHorizontalBox();
        searchBar.setBorder(JBUI.Borders.empty(10));
        this.searchField = new JTextField();
        this.searchField.setFont(new Font(null, Font.PLAIN, JBUI.scaleFontSize(12f)));
        Document document = this.searchField.getDocument();
        document.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String input = searchField.getText().trim().toLowerCase(Locale.ROOT);
                if (StringUtils.equals(keyword, input)) return;
                keyword = input;
                refreshList();
            }
        });
        searchBar.add(this.searchField);
        return searchBar;
    }

    private JLabel createTitleBar() {
        JLabel titleBar = new JLabel();
        titleBar.setBorder(JBUI.Borders.empty(10));
        titleBar.setFont(new Font(null, Font.PLAIN, JBUI.scaleFontSize(12f)));
        return titleBar;
    }

    private void addListView() {
        this.listComponent = new JBList<>();
        this.listAdapter = new ResListRender();
        this.listComponent.setCellRenderer(listAdapter);
        this.listComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                File imageFile = getSelectImageFile(point);
                if (imageFile == null) return;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    PluginUtils.openFile(project, imageFile);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    ResMenuGroup menuGroup = new ResMenuGroup(imageFile, ResRootPanel.this);
                    DataContext dataContext = DataManager.getInstance().getDataContext(listComponent);
                    JBPopupFactory.ActionSelectionAid selectionAid = JBPopupFactory.ActionSelectionAid.SPEEDSEARCH;
                    RelativePoint showPoint = new RelativePoint(listComponent, e.getPoint());
                    JBPopupFactory.getInstance()
                            .createActionGroupPopup(null, menuGroup, dataContext, selectionAid, false)
                            .show(showPoint);
                }
            }
        });
        JBScrollPane scrollPane = new JBScrollPane(this.listComponent);
        add(scrollPane, BorderLayout.CENTER);
    }

    @Nullable
    private File getSelectImageFile(@NotNull Point point) {
        int selectIndex = listComponent.locationToIndex(point);
        if (selectIndex < 0) return null;
        ListModel<File> model = listComponent.getModel();
        if (model == null) return null;
        if (selectIndex >= model.getSize()) return null;
        return model.getElementAt(selectIndex);
    }


    @Override
    public void loadResList(@NotNull List<File> resList) {
        Long totalSize = resList.stream().map((File::length)).reduce(0L, Long::sum);
        String totalSizeStr = StringUtil.formatFileSize(totalSize);
        this.titleBar.setText(String.format("File Count: %d / Total Size: %s", resList.size(), totalSizeStr));
        this.resList = resList;
        this.refreshList();
        this.updateFocus();
    }

    @Override
    public void reloadItems(@NotNull List<File> changeFileList) {
        this.listAdapter.resetItems(changeFileList);
        this.loadResList(this.resList);
    }

    @Override
    public void cleanResList() {
        this.listAdapter.cleanResList();
    }

    @Override
    public void sortRes(@NotNull SortType sort) {
        this.sortType = sort;
        this.refreshList();
    }

    @Override
    public void searchRes() {
        showTopBar(!showSearchBar);
        validate();
        repaint();
        updateFocus();
    }

    @Override
    public void locateRes(@NotNull String resName) {
        ListModel<File> listModel = this.listComponent.getModel();
        if (listModel == null) return;
        for (int i = 0; i < listModel.getSize(); i++) {
            File file = listModel.getElementAt(i);
            if (StringUtils.equals(resName, file.getName())) {
                this.listComponent.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    @Override
    public void compressRes() {
        if (CollectionUtils.isEmpty(resList)) return;
        List<File> resultList = new ArrayList<>();
        for (File file : resList) {
            if (!TinyUtils.isSupport(file)) continue;
            resultList.add(file);
        }
        TinyUtils.compressImage(project, resultList, (success) -> {
            if (success) reloadItems(resultList);
        });
    }

    @Override
    public JComponent asComponent() {
        return this;
    }

    private void refreshList() {
        if (CollectionUtils.isEmpty(this.resList)) return;
        List<File> resultList = new ArrayList<>(this.resList);
        //filter with keyword
        if (StringUtil.isNotEmpty(this.keyword)) {
            resultList = resultList.stream().filter(file -> {
                String fileName = FileUtils.getFileName(file).toLowerCase(Locale.ROOT);
                return fileName.contains(this.keyword);
            }).collect(Collectors.toList());
        }
        //sort
        resultList.sort(this.sortType.comparator);

        DefaultListModel<File> listModel = new DefaultListModel<>();
        listModel.addAll(resultList);
        this.listComponent.setModel(listModel);
    }


    private void updateFocus() {
        if (this.showSearchBar) {
            this.searchField.requestFocus();
        } else {
            this.listComponent.requestFocus();
        }
    }
}
