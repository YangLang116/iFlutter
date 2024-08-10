package com.xtu.plugin.flutter.action.generate.res;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.action.FlutterProjectAction;
import com.xtu.plugin.flutter.base.entity.AssetResultEntity;
import com.xtu.plugin.flutter.base.utils.AssetUtils;
import com.xtu.plugin.flutter.base.utils.FontUtils;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.component.assets.code.DartFontFileGenerator;
import com.xtu.plugin.flutter.component.assets.code.DartRFileGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GenerateResAction extends FlutterProjectAction {

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
        DartRFileGenerator.getInstance().resetCache();
        DartFontFileGenerator.getInstance().resetCache();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PubSpecUtils.writeAssetList(project, newAssetList, newFontList);
        });
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> {
            AssetResultEntity assetResult = PubSpecUtils.readAssetList(project);
            DartRFileGenerator.getInstance().generate(project, assetResult);
            DartFontFileGenerator.getInstance().generate(project, assetResult);
        });
    }
}
