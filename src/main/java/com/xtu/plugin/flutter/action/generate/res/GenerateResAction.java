package com.xtu.plugin.flutter.action.generate.res;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import com.xtu.plugin.flutter.utils.AssetUtils;
import com.xtu.plugin.flutter.utils.FontUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateResAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        boolean isFlutterProject = PluginUtils.isFlutterProject(project);
        e.getPresentation().setVisible(isFlutterProject);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        List<File> assetFileList = AssetUtils.getAllAssetFile(project);
        List<String> newAssetList = new ArrayList<>();
        List<String> newFontList = new ArrayList<>();
        for (File assetFile : assetFileList) {
            String assetPath = AssetUtils.getAssetFilePath(project, assetFile);
            if (StringUtils.isEmpty(assetPath)) continue;
            if (FontUtils.isFontAsset(assetPath)) {
                newFontList.add(assetPath);
            } else {
                newAssetList.add(assetPath);
            }
        }
        PubspecUtils.writeAssetSafe(project, newAssetList, newFontList);
        //force generate Res File
        PubspecUtils.readAssetSafe(project, (name, version, assetList, fontList) -> {
            String resPrefix = PluginUtils.getResPrefix(project, name);
            DartRFileGenerator.getInstance().generateSafe(project, name, version, resPrefix, assetList, true);
            DartFontFileGenerator.getInstance().generateSafe(project, resPrefix, fontList, true);
        });
    }
}
