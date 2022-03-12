package com.xtu.plugin.flutter.action.generate.res;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.*;
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
        List<File> assetFileList = PluginUtils.getAllAssetFile(project);
        List<String> newAssetList = new ArrayList<>();
        List<String> newFontList = new ArrayList<>();
        for (File assetFile : assetFileList) {
            String assetPath = AssetUtils.getAssetPath(projectPath, assetFile);
            if (StringUtils.isEmpty(assetPath)) continue;
            if (FontUtils.isFontAsset(assetPath)) {
                newFontList.add(assetPath);
            } else {
                newAssetList.add(assetPath);
            }
        }
        //duplicate and sort asset resource
        CollectionUtils.duplicateList(newAssetList);
        Collections.sort(newAssetList);
        //duplicate and sort font resource
        CollectionUtils.duplicateList(newFontList);
        Collections.sort(newFontList);

        PubspecUtils.readAsset(project, (assetList, fontList) -> {
            //force generate Res File
            if (assetList.equals(newAssetList) && fontList.equals(newFontList)) {
                DartRFileGenerator.getInstance().generate(project, newAssetList);
                DartFontFileGenerator.getInstance().generate(project, newFontList);
            } else {
                PubspecUtils.writeAsset(project, newAssetList, newFontList);
            }
        });
    }
}
