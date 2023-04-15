package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ResManagerRootPanel extends JPanel implements ListCellRenderer<File> {

    private JLabel titleBar;
    private JComponent searchBar;
    private JTextField searchField;
    private JBList<File> listComponent;

    private String keyword;
    private SortType sortType;
    private List<File> resList;
    private boolean showSearchBar;

    private final Map<String, ResRowComponent> componentCache = new HashMap<>();


    public ResManagerRootPanel(@NotNull Project project, boolean showSearchBar, @NotNull SortType sortType) {
        this.sortType = sortType;
        this.registerKeyboardAction(e -> {
                    if (!this.showSearchBar) return;
                    this.toggleTopBar();
                }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        setLayout(new BorderLayout());
        switchTopBar(showSearchBar);
        addListView(project);
    }

    private void switchTopBar(boolean showSearchBar) {
        this.showSearchBar = showSearchBar;
        if (this.titleBar == null) this.titleBar = createTitleBar();
        if (this.searchBar == null) this.searchBar = createSearchBar();
        this.remove(this.titleBar);
        this.remove(this.searchBar);
        if (showSearchBar) {
            this.add(this.searchBar, BorderLayout.NORTH);
            this.searchField.requestFocus();
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
                String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
                updateKeyword(keyword);
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

    private void addListView(@NotNull Project project) {
        this.listComponent = new JBList<>();
        this.listComponent.setCellRenderer(this);
        this.listComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openImageFile(project, e.getPoint());
            }
        });
        JBScrollPane scrollPane = new JBScrollPane(this.listComponent);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void openImageFile(@NotNull Project project, @NotNull Point point) {
        int selectIndex = this.listComponent.locationToIndex(point);
        if (selectIndex < 0) return;
        ListModel<File> model = this.listComponent.getModel();
        if (model == null) return;
        if (selectIndex >= model.getSize()) return;
        File imageFile = model.getElementAt(selectIndex);
        VirtualFile needOpenFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
        if (needOpenFile == null) return;
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        editorManager.openFile(needOpenFile, true);
    }

    public void refreshResList(@NotNull List<File> resList) {
        Long totalSize = resList.stream().map((File::length)).reduce(0L, Long::sum);
        String totalSizeStr = StringUtil.formatFileSize(totalSize);
        this.titleBar.setText(String.format("File Count: %d / Total Size: %s", resList.size(), totalSizeStr));
        this.resList = resList;
        this.refreshList();
    }

    public void cleanResList() {
        for (Map.Entry<String, ResRowComponent> entry : componentCache.entrySet()) {
            ResRowComponent jComponent = entry.getValue();
            jComponent.dispose();
        }
        componentCache.clear();
    }

    public void toggleTopBar() {
        switchTopBar(!showSearchBar);
        validate();
        repaint();
    }

    private void updateKeyword(String keyword) {
        if (StringUtils.equals(keyword, this.keyword)) return;
        this.keyword = keyword;
        this.refreshList();
    }

    public void sort(@NotNull SortType sort) {
        this.sortType = sort;
        this.refreshList();
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends File> list, File assetFile, int index, boolean isSelected, boolean cellHasFocus) {
        String path = assetFile.getAbsolutePath();
        if (!componentCache.containsKey(path)) {
            componentCache.put(path, new ResRowComponent(assetFile));
        }
        return componentCache.get(path);
    }

    private void refreshList() {
        if (CollectionUtils.isEmpty(this.resList)) return;
        List<File> resultList = new ArrayList<>(this.resList);
        //sort
        resultList.sort(sortType == SortType.SORT_AZ ?
                Comparator.comparing(File::getName) :
                (a, b) -> (int) (b.length() - a.length()));
        //filter with keyword
        if (StringUtil.isNotEmpty(this.keyword)) {
            resultList = resultList.stream().filter(file -> {
                String fileName = FileUtils.getFileName(file).toLowerCase(Locale.ROOT);
                return fileName.contains(this.keyword);
            }).collect(Collectors.toList());
        }
        DefaultListModel<File> listModel = new DefaultListModel<>();
        listModel.addAll(resultList);
        this.listComponent.setModel(listModel);
    }
}
