package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResManagerRootPanel extends JPanel implements ListCellRenderer<File> {

    private JLabel fileCountLabel;
    private JLabel fileSizeLabel;
    private JBList<File> listComponent;
    private final Map<String, ResRowComponent> componentCache = new HashMap<>();

    public ResManagerRootPanel(@NotNull Project project) {
        setLayout(new BorderLayout());
        addTitleBar();
        addListView(project);
    }

    private void addTitleBar() {
        Box container = Box.createHorizontalBox();
        container.setBorder(JBUI.Borders.empty(10));

        Box fileLabelContainer = Box.createVerticalBox();
        this.fileCountLabel = new JLabel();
        this.fileCountLabel.setFont(new Font(null, Font.PLAIN, JBUI.scaleFontSize(14f)));
        this.fileSizeLabel = new JLabel();
        this.fileSizeLabel.setFont(new Font(null, Font.PLAIN, JBUI.scaleFontSize(14f)));
        fileLabelContainer.add(fileCountLabel);
        fileLabelContainer.add(Box.createVerticalStrut(3));
        fileLabelContainer.add(fileSizeLabel);
        container.add(fileLabelContainer);

        add(container, BorderLayout.NORTH);
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
        DefaultListModel<File> listModel = new DefaultListModel<>();
        listModel.addAll(resList);
        this.listComponent.setModel(listModel);
        this.fileCountLabel.setText(String.format("File Count: %d", resList.size()));
        Long totalSize = resList.stream().map((File::length)).reduce(0L, Long::sum);
        String totalSizeStr = StringUtil.formatFileSize(totalSize);
        this.fileSizeLabel.setText(String.format("Total Size: %s", totalSizeStr));
    }

    public void cleanResList() {
        for (Map.Entry<String, ResRowComponent> entry : componentCache.entrySet()) {
            ResRowComponent jComponent = entry.getValue();
            jComponent.dispose();
        }
        componentCache.clear();
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends File> list, File assetFile, int index, boolean isSelected, boolean cellHasFocus) {
        String path = assetFile.getAbsolutePath();
        if (!componentCache.containsKey(path)) {
            componentCache.put(path, new ResRowComponent(assetFile));
        }
        return componentCache.get(path);
    }
}
