package com.xtu.plugin.flutter.window.res.action;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.TinyUtils;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CompressAction extends AnAction {

    private final ResManagerRootPanel rootPanel;

    public CompressAction(@NotNull ResManagerRootPanel rootPanel) {
        super(AllIcons.Actions.IntentionBulbGrey);
        this.rootPanel = rootPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        List<File> resList = this.rootPanel.getResList();
        if (CollectionUtils.isEmpty(resList)) return;
        List<File> resultList = new ArrayList<>();
        for (File file : resList) {
            if (!TinyUtils.isSupport(file)) continue;
            resultList.add(file);
        }
        TinyUtils.compressImage(project, resultList, (success) -> {
            if (success) this.rootPanel.reloadResList(resultList);
        });
    }
}