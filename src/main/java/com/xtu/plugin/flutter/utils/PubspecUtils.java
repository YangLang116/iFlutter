package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PubspecUtils {

    private static final String NODE_FLUTTER = "flutter";
    private static final String NODE_ASSET = "assets";

    public static String getFileName() {
        return "pubspec.yaml";
    }

    //判断当前文件是否根目录pubspec.yaml文件
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

    //获取根目录pubspec.yaml文件psi
    @Nullable
    private static YAMLFile getRootPubspecFile(@NotNull Project project) {
        VirtualFile rootProjectDir = ProjectUtil.guessProjectDir(project);
        if (rootProjectDir == null) return null;
        VirtualFile rootPubspecFile = rootProjectDir.findChild(getFileName());
        if (rootPubspecFile == null) return null;
        return (YAMLFile) PsiManager.getInstance(project).findFile(rootPubspecFile);
    }

    //读取pubspec.yaml文件顶级YAMLValue内容
    @Nullable
    private static YAMLMapping getTopLevelMapping(@NotNull Project project) {
        YAMLFile yamlFile = getRootPubspecFile(project);
        if (yamlFile == null) return null;
        YAMLDocument document = yamlFile.getDocuments().get(0);
        return (YAMLMapping) document.getTopLevelValue();
    }

    //读取flutter#asset内容
    @Nullable
    private static YAMLSequence getAssetSequence(@NotNull Project project) {
        YAMLMapping rootMapping = getTopLevelMapping(project);
        if (rootMapping == null) return null;
        YAMLKeyValue flutterKeyValue = rootMapping.getKeyValueByKey(NODE_FLUTTER);
        if (flutterKeyValue == null) return null;
        YAMLValue flutterValue = flutterKeyValue.getValue();
        if (!(flutterValue instanceof YAMLMapping)) return null;
        YAMLKeyValue assetKeyValue = ((YAMLMapping) flutterValue).getKeyValueByKey(NODE_ASSET);
        if (assetKeyValue == null) return null;
        YAMLValue assetValue = assetKeyValue.getValue();
        if (!(assetValue instanceof YAMLSequence)) return null;
        return (YAMLSequence) assetValue;
    }

    // 读取资源列表
    public static void readAsset(@NotNull Project project, @NotNull YamlReadListener listener) {
        ApplicationManager.getApplication()
                .invokeLater(() -> WriteAction.run(() -> {
                    PsiDocumentManager.getInstance(project).commitAllDocuments();
                    readAssetInReadAction(project, listener);
                }));
    }

    private static void readAssetInReadAction(@NotNull Project project, @NotNull YamlReadListener listener) {
        ReadAction.run(() -> {
            List<String> assetList = new ArrayList<>();
            YAMLSequence assetSequence = getAssetSequence(project);
            if (assetSequence != null) {
                List<YAMLSequenceItem> assetSequenceItems = assetSequence.getItems();
                for (YAMLSequenceItem assetSequenceItem : assetSequenceItems) {
                    YAMLValue itemValue = assetSequenceItem.getValue();
                    if (itemValue == null) continue;
                    String itemText = itemValue.getText();
                    if (StringUtils.isEmpty(itemText)) continue;
                    assetList.add(itemText);
                }
                CollectionUtils.duplicateList(assetList);
                Collections.sort(assetList);
            }
            listener.onGet(assetList);
        });
    }

    //更新资源列表
    public static void writeAsset(@NotNull Project project,
                                  @NotNull List<String> assetList) {
        //pure asset list
        CollectionUtils.duplicateList(assetList);
        Collections.sort(assetList);
        ReadAction.run(() -> {
            YAMLSequence oldAssetSequence = getAssetSequence(project);
            ApplicationManager.getApplication()
                    .invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
                        //asset is configuration
                        if (oldAssetSequence != null) {
                            YAMLSequence newAssetSequence;
                            if (CollectionUtils.isEmpty(assetList)) {
                                newAssetSequence = elementGenerator.createEmptySequence();
                            } else {
                                StringBuilder newAssetBuilder = new StringBuilder();
                                for (String asset : assetList) {
                                    newAssetBuilder.append("- ").append(asset).append("\n");
                                }
                                YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(newAssetBuilder.toString());
                                newAssetSequence = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLSequence.class);
                            }
                            if (newAssetSequence == null) return;
                            oldAssetSequence.replace(newAssetSequence);
                        } else {
                            YAMLMapping topLevelMapping = getTopLevelMapping(project);
                            if (topLevelMapping == null) return;
                            YAMLKeyValue flutterKeyValue = topLevelMapping.getKeyValueByKey(NODE_FLUTTER);
                            if (flutterKeyValue == null || !(flutterKeyValue.getValue() instanceof YAMLMapping)) {
                                //add flutter psi
                                StringBuilder flutterPsiBuilder = new StringBuilder()
                                        .append("flutter:\n  assets:\n");
                                for (String asset : assetList) {
                                    flutterPsiBuilder.append("    - ").append(asset).append("\n");
                                }
                                YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(flutterPsiBuilder.toString());
                                YAMLKeyValue newFlutterKeyValue = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLKeyValue.class);
                                if (newFlutterKeyValue == null) return;
                                topLevelMapping.putKeyValue(newFlutterKeyValue);
                            } else {
                                //add flutter#asset psi
                                YAMLMapping flutterValue = (YAMLMapping) flutterKeyValue.getValue();
                                StringBuilder assetPsiBuilder = new StringBuilder("assets:\n");
                                for (String asset : assetList) {
                                    assetPsiBuilder.append("  - ").append(asset).append("\n");
                                }
                                YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(assetPsiBuilder.toString());
                                YAMLKeyValue newAssetKeyValue = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLKeyValue.class);
                                if (newAssetKeyValue == null) return;
                                flutterValue.putKeyValue(newAssetKeyValue);
                            }
                        }
                        YAMLFile rootPubspecFile = getRootPubspecFile(project);
                        assert rootPubspecFile != null;
                        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                        Document document = psiDocumentManager.getDocument(rootPubspecFile);
                        //sync psi - document
                        if (document != null) {
                            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                        }
                        CodeStyleManager.getInstance(project).reformat(rootPubspecFile);
                        psiDocumentManager.commitAllDocuments();
                        //refresh UI
                        notifyPubspecUpdate(project);
                    }));
        });
    }

    private static void notifyPubspecUpdate(Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            statusBar.setInfo(String.format(Locale.ROOT, "update %s success", getFileName()));
        }
    }

    public interface YamlReadListener {

        void onGet(@NotNull List<String> assetList);

    }
}
