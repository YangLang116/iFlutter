package com.xtu.plugin.flutter.action.generate.res;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.FileUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GenerateResAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (PluginUtils.isNotFlutterProject(project)) {
            e.getPresentation().setVisible(false);
            return;
        }
        e.getPresentation().setVisible(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        List<File> assetFileList = new ArrayList<>();
        List<String> assetFoldNameList = PluginUtils.supportAssetFoldName(project);
        for (String assetFoldName : assetFoldNameList) {
            File assetDirectory = new File(projectPath, assetFoldName);
            FileUtils.scanDirectory(assetDirectory, (file -> {
                String fileName = file.getName();
                //修复隐藏文件导致，生成的res.dart文件异常，如mac下的.DS_Store文件
                if (!fileName.startsWith(".")) {
                    assetFileList.add(file);
                }
            }));
        }
        List<String> newAssetNameList = new ArrayList<>();
        for (File assetFile : assetFileList) {
            String relativePath = FileUtils.getRelativePath(projectPath, assetFile);
            newAssetNameList.add(relativePath);
        }
        //duplicate and sort resource
        CollectionUtils.duplicateList(newAssetNameList);
        Collections.sort(newAssetNameList);
        PubspecUtils.readAssetAtReadAction(project, assetList -> {
            //force refresh R file
            if (newAssetNameList.equals(assetList)) {
                DartRFileGenerator.getInstance().generate(project, assetList);
            } else { //update asset
                PubspecUtils.writeAssetAtWriteAction(project, newAssetNameList);
            }
        });
    }
}
