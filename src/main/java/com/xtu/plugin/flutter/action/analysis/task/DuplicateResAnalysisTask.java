package com.xtu.plugin.flutter.action.analysis.task;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.analysis.ui.DuplicateResDisplayDialog;
import com.xtu.plugin.flutter.utils.FileUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateResAnalysisTask implements Runnable {

    private final Project project;
    private final ProgressIndicator indicator;
    private final File assetDirectory;

    public DuplicateResAnalysisTask(@NotNull Project project, @NotNull ProgressIndicator indicator, @NotNull File assetDirectory) {
        this.project = project;
        this.indicator = indicator;
        this.assetDirectory = assetDirectory;
    }

    @Override
    public void run() {
        this.indicator.setIndeterminate(true);
        final Map<String, List<File>> md5FileListMap = new HashMap<>();
        FileUtils.scanDirectory(this.assetDirectory, file -> fillMd5ToMap(file, md5FileListMap));
        //display analysis result
        this.indicator.setIndeterminate(false);
        this.indicator.setFraction(1);
        showAnalysisResult(md5FileListMap);
    }

    private void fillMd5ToMap(File file, Map<String, List<File>> listMap) {
        //show indicator text
        String relativePath = FileUtils.getAssetPath(this.assetDirectory.getAbsolutePath(), file);
        this.indicator.setText(relativePath);
        //fill data
        String fileMd5 = FileUtils.getMd5(file);
        if (fileMd5 == null) return;
        if (!listMap.containsKey(fileMd5)) {
            listMap.put(fileMd5, new ArrayList<>());
        }
        listMap.get(fileMd5).add(file);
    }

    private void showAnalysisResult(@NotNull Map<String, List<File>> resultMap) {
        final Map<String, List<File>> pureMap = new HashMap<>();
        for (Map.Entry<String, List<File>> entry : resultMap.entrySet()) {
            String fileMd5 = entry.getKey();
            List<File> fileList = entry.getValue();
            if (fileList.size() >= 2) {
                pureMap.put(fileMd5, fileList);
            }
        }
        if (pureMap.size() == 0) {
            ToastUtil.make(project, MessageType.INFO, "暂未发现重复资源");
            return;
        }
        //show data with JBTree
        DuplicateResDisplayDialog.show(project, pureMap);
    }
}
