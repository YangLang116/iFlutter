package com.xtu.plugin.flutter.window.res.ui;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
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

    private final JBList<File> listComponent = new JBList<>();
    private final Map<String, ResRowComponent> componentCache = new HashMap<>();

    public ResManagerRootPanel(@NotNull Project project) {
        setLayout(new BorderLayout());
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

    public void refreshResList(@NotNull List<File> resList) {
        DefaultListModel<File> listModel = new DefaultListModel<>();
        listModel.addAll(resList);
        this.listComponent.setModel(listModel);
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
}
