package com.xtu.plugin.flutter.utils;

import com.amihaiemil.eoyaml.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class PubspecUtils {

    private static final String NODE_FLUTTER = "flutter";
    private static final String NODE_ASSET = "assets";

    public static String getFileName() {
        return "pubspec.yaml";
    }

    @Nullable
    private static File getPubspecFile(@NotNull Project project) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return null;
        return new File(projectPath, getFileName());
    }

    private static YamlMapping readYamlContent(@NotNull File pubSpecFile) throws Exception {
        return Yaml.createYamlInput(pubSpecFile)
                .readYamlMapping();
    }

    @NotNull
    private static List<String> getAssetList(@NotNull Project project) throws Exception {
        File pubSpecFile = getPubspecFile(project);
        if (pubSpecFile == null) throw new IllegalStateException("未识别当前项目");
        return getAssetList(pubSpecFile);
    }

    @NotNull
    private static List<String> getAssetList(@NotNull File pubSpecFile) throws Exception {
        List<String> result = new ArrayList<>();
        YamlMapping yamlContent = readYamlContent(pubSpecFile);
        YamlSequence assetSequence = yamlContent.yamlMapping(NODE_FLUTTER).yamlSequence(NODE_ASSET);
        if (assetSequence != null && !assetSequence.isEmpty()) {
            for (YamlNode yamlNode : assetSequence) {
                result.add(yamlNode.asScalar().value());
            }
            //pure assetList
            CollectionUtils.duplicateList(result);
            Collections.sort(result);
        }
        return result;
    }

    private static void updateAssetList(@NotNull Project project, @NotNull List<String> assetList) throws Exception {
        File pubSpecFile = getPubspecFile(project);
        if (pubSpecFile == null) throw new IllegalStateException("未识别当前项目");
        //pure assetList
        YamlMappingBuilder resultBuilder = Yaml.createYamlMappingBuilder();
        CollectionUtils.duplicateList(assetList);
        Collections.sort(assetList);
        //fill origin key:value
        YamlMapping originYamlContent = readYamlContent(pubSpecFile);
        Set<YamlNode> keys = originYamlContent.keys();
        for (YamlNode key : keys) {
            resultBuilder = resultBuilder.add(key, originYamlContent.value(key));
        }
        //modify flutter > assets YamlNode
        YamlMappingBuilder newFlutterMappingBuilder = Yaml.createYamlMappingBuilder();
        YamlMapping originFlutterMapping = originYamlContent.yamlMapping(NODE_FLUTTER);
        Set<YamlNode> originFlutterKeys = originFlutterMapping.keys();
        for (YamlNode originFlutterKey : originFlutterKeys) {
            newFlutterMappingBuilder = newFlutterMappingBuilder.add(originFlutterKey, originFlutterMapping.value(originFlutterKey));
        }
        YamlSequenceBuilder assetSequenceBuilder = Yaml.createYamlSequenceBuilder();
        for (String asset : assetList) {
            assetSequenceBuilder = assetSequenceBuilder.add(asset);
        }
        //replace asset node
        newFlutterMappingBuilder = newFlutterMappingBuilder.add(NODE_ASSET, assetSequenceBuilder.build());
        //replace flutter node
        resultBuilder = resultBuilder.add(NODE_FLUTTER, newFlutterMappingBuilder.build());
        //writer to file
        FileWriter fileWriter = new FileWriter(pubSpecFile);
        //custom yaml printer, because of https://github.com/decorators-squad/eo-yaml/issues/437
        YamlPrinter yamlPrinter = new ExtensionYamlPrinter(fileWriter);
        yamlPrinter.print(resultBuilder.build());
    }

    // read asset list from pubspec file
    public static void readAssetAtReadAction(@NotNull Project project, @NotNull YamlReadListener listener) {
        ReadAction.run(() -> {
            try {
                List<String> assetList = getAssetList(project);
                listener.onGet(assetList);
            } catch (Exception exception) {
                LogUtils.error("PubspecUtils readAtReadAction -> " + exception.getMessage());
                ToastUtil.make(project, MessageType.ERROR, exception.getMessage());
            }
        });
    }

    // write asset to pubspec file
    public static void writeAssetAtWriteAction(@NotNull Project project, @NotNull List<String> assetList) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            writeAssetAtDispatchThreadInWriteAction(project, assetList);
        } else {
            SwingUtilities.invokeLater(() -> writeAssetAtDispatchThreadInWriteAction(project, assetList));
        }
    }

    private static void writeAssetAtDispatchThreadInWriteAction(@NotNull Project project, @NotNull List<String> assetList) {
        WriteAction.run(() -> {
            try {
                updateAssetList(project, assetList);
                File pubSpecFile = getPubspecFile(project);
                assert pubSpecFile != null;
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(pubSpecFile);
                virtualFile.refresh(false, false, () -> {
                    //update status bar text tip
                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    if (statusBar != null) {
                        statusBar.setInfo(String.format(Locale.ROOT, "update %s success", getFileName()));
                    }
                });
            } catch (Exception e) {
                LogUtils.error("PubspecUtils writeAssetAtDispatchThreadInWriteAction -> " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }
        });
    }

    public interface YamlReadListener {

        void onGet(@NotNull List<String> assetList);

    }
}
