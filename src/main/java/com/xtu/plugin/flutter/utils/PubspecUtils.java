package com.xtu.plugin.flutter.utils;

import com.amihaiemil.eoyaml.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStreamWriter;
import java.util.*;

public class PubspecUtils {

    private static final String NODE_FLUTTER = "flutter";
    private static final String NODE_ASSET = "assets";

    public static String getFileName() {
        return "pubspec.yaml";
    }

    public static boolean isRootPubspecFile(PsiFile psiFile) {
        if (psiFile == null) return false;
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return false;
        Project project = psiFile.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        String rootPubspecPath = projectPath + "/" + getFileName();
        return StringUtils.equals(rootPubspecPath, virtualFile.getPath());
    }

    @Nullable
    private static VirtualFile getPubspecFile(@NotNull Project project) {
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return null;
        return projectDir.findChild(getFileName());
    }

    private static YamlMapping readYamlContent(@NotNull VirtualFile pubSpecFile) throws Exception {
        return Yaml.createYamlInput(pubSpecFile.getInputStream())
                .readYamlMapping();
    }

    @NotNull
    private static List<String> getAssetList(@NotNull Project project) throws Exception {
        VirtualFile pubSpecFile = getPubspecFile(project);
        if (pubSpecFile == null) throw new IllegalStateException("未识别当前项目");
        return getAssetList(pubSpecFile);
    }

    @NotNull
    private static List<String> getAssetList(@NotNull VirtualFile pubSpecFile) throws Exception {
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

    private static void updateAssetList(@NotNull Project project, @NotNull List<String> assetList, @NotNull YamlWriterListener listener) throws Exception {
        VirtualFile pubSpecFile = getPubspecFile(project);
        if (pubSpecFile == null) throw new IllegalStateException("未识别当前项目");
        //pure assetList
        CollectionUtils.duplicateList(assetList);
        Collections.sort(assetList);
        ReadAction.run(() -> {
            YamlMapping originYamlContent = readYamlContent(pubSpecFile);
            YamlMappingBuilder resultBuilder = Yaml.createYamlMappingBuilder();
            //fill origin key:value
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

            //writer yaml
            final YamlMapping resultMapping = resultBuilder.build();
            WriteAction.run(() -> {
                OutputStreamWriter writer = new OutputStreamWriter(pubSpecFile.getOutputStream(project));
                //custom yaml printer, because of https://github.com/decorators-squad/eo-yaml/issues/437
                YamlPrinter yamlPrinter = new ExtensionYamlPrinter(writer);
                yamlPrinter.print(resultMapping);

                pubSpecFile.refresh(false, false, listener::complete);
            });
        });
    }

    // read asset list from pubspec file
    public static void readAssetAtReadAction(@NotNull Project project, @NotNull YamlReadListener listener) {
        ApplicationManager.getApplication()
                .invokeLater(() -> WriteAction.run(() -> {
                    //保存所有修改
                    PsiDocumentManager.getInstance(project).commitAllDocuments();
                    ReadAction.run(() -> {
                        try {
                            List<String> assetList = getAssetList(project);
                            listener.onGet(assetList);
                        } catch (Exception exception) {
                            LogUtils.error("PubspecUtils readAssetAtReadAction -> " + exception.getMessage());
                            ToastUtil.make(project, MessageType.ERROR, exception.getMessage());
                        }
                    });
                }));
    }

    // write asset to pubspec file
    public static void writeAssetAtWriteAction(@NotNull Project project, @NotNull List<String> assetList) {
        // This invokeLater is required. The problem is setFile does a commit to PSI, but setFile is
        // invoked inside PSI change event. It causes an Exception like "Changes to PSI are not allowed inside event processing"
        DumbService.getInstance(project).smartInvokeLater(() -> {
            try {
                updateAssetList(project, assetList, () -> {
                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    if (statusBar != null) {
                        statusBar.setInfo(String.format(Locale.ROOT, "update %s success", getFileName()));
                    }
                });
            } catch (Exception e) {
                LogUtils.error("PubspecUtils writeAssetAtWriteAction -> " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            }
        });
    }

    public interface YamlReadListener {

        void onGet(@NotNull List<String> assetList);

    }

    public interface YamlWriterListener {

        void complete();

    }
}
