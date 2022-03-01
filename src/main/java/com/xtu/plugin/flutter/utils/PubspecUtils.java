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
import com.intellij.psi.PsiElement;
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
    private static YAMLFile getRootPubspecFile(@NotNull Project project) {
        VirtualFile rootProjectDir = ProjectUtil.guessProjectDir(project);
        if (rootProjectDir == null) return null;
        VirtualFile rootPubspecFile = rootProjectDir.findChild(getFileName());
        if (rootPubspecFile == null) return null;
        return (YAMLFile) PsiManager.getInstance(project).findFile(rootPubspecFile);
    }

    @Nullable
    private static YAMLSequence getAssetSequence(@NotNull Project project) {
        YAMLFile yamlFile = getRootPubspecFile(project);
        if (yamlFile == null) return null;
        YAMLDocument document = yamlFile.getDocuments().get(0);
        YAMLMapping rootMapping = (YAMLMapping) document.getTopLevelValue();
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

    // read asset list from pubspec file
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
                        PsiElement placeElement = mountNewAssetSequence(project, newAssetSequence, elementGenerator, oldAssetSequence);
                        if (placeElement == null) return;
                        PsiFile psiFile = PsiTreeUtil.getParentOfType(placeElement, PsiFile.class);
                        assert psiFile != null;
                        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
                        Document document = psiDocumentManager.getDocument(psiFile);
                        //sync psi - document
                        if (document != null) {
                            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                        }
                        CodeStyleManager.getInstance(project).reformat(psiFile);
                        psiDocumentManager.commitAllDocuments();
                        //refresh UI
                        notifyPubspecUpdate(project);
                    }));
        });
    }

    @Nullable
    private static PsiElement mountNewAssetSequence(@NotNull Project project,
                                                    @NotNull YAMLSequence newAssetSequence,
                                                    @NotNull YAMLElementGenerator elementGenerator,
                                                    @Nullable YAMLSequence oldAssetSequence) {
        if (oldAssetSequence != null) {
            return oldAssetSequence.replace(newAssetSequence);
        }
        //add psiElement to Psi Tree
        YAMLFile psiFile = getRootPubspecFile(project);
        if (psiFile == null) return null;
        YAMLDocument document = psiFile.getDocuments().get(0);
        YAMLValue topLevelValue = document.getTopLevelValue();
        if (!(topLevelValue instanceof YAMLMapping)) return null;
        YAMLKeyValue flutterKeyValue = ((YAMLMapping) topLevelValue).getKeyValueByKey(NODE_FLUTTER);
        //ensure flutter psi exist
        if (flutterKeyValue == null || flutterKeyValue.getValue() == null) {
            flutterKeyValue = elementGenerator.createYamlKeyValue(NODE_FLUTTER, NODE_ASSET + ":temp");
            ((YAMLMapping) topLevelValue).putKeyValue(flutterKeyValue);
            YAMLMapping flutterValue = ((YAMLMapping) flutterKeyValue.getValue());
            assert flutterValue != null;
            YAMLKeyValue assetKeyValue = flutterValue.getKeyValueByKey(NODE_ASSET);
            assert assetKeyValue != null;
            assetKeyValue.setValue(newAssetSequence);
            return newAssetSequence;
        }
        YAMLMapping flutterValue = (YAMLMapping) flutterKeyValue.getValue();
        YAMLKeyValue assetKeyValue = elementGenerator.createYamlKeyValue(NODE_ASSET, "");
        assetKeyValue.setValue(newAssetSequence);
        flutterValue.putKeyValue(assetKeyValue);
        return newAssetSequence;
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
