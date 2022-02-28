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
    private static PsiFile getRootPubspecFile(@NotNull Project project) {
        VirtualFile rootProjectDir = ProjectUtil.guessProjectDir(project);
        if (rootProjectDir == null) return null;
        VirtualFile rootPubspecFile = rootProjectDir.findChild(getFileName());
        if (rootPubspecFile == null) return null;
        return PsiManager.getInstance(project).findFile(rootPubspecFile);
    }

    @Nullable
    private static YAMLSequence getAssetSequence(@NotNull Project project) {
        PsiFile rootPubspecFile = getRootPubspecFile(project);
        if (rootPubspecFile == null) return null;
        YAMLFile yamlFile = (YAMLFile) rootPubspecFile;
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
            YAMLSequence assetSequence = getAssetSequence(project);
            if (assetSequence == null) return;
            List<YAMLSequenceItem> assetSequenceItems = assetSequence.getItems();
            List<String> assetList = new ArrayList<>();
            for (YAMLSequenceItem assetSequenceItem : assetSequenceItems) {
                YAMLValue itemValue = assetSequenceItem.getValue();
                if (itemValue == null) continue;
                assetList.add(itemValue.getText());
            }
            CollectionUtils.duplicateList(assetList);
            Collections.sort(assetList);
            listener.onGet(assetList);
        });
    }

    public static void writeAsset(@NotNull Project project,
                                  @NotNull List<String> assetList) {
        ReadAction.run(() -> {
            YAMLSequence assetSequence = getAssetSequence(project);
            if (assetSequence == null) return;
            CollectionUtils.duplicateList(assetList);
            Collections.sort(assetList);
            ApplicationManager.getApplication()
                    .invokeLater(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
                        StringBuilder newAssetBuilder = new StringBuilder();
                        for (String asset : assetList) {
                            newAssetBuilder.append("- ").append(asset).append("\n");
                        }
                        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
                        YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(newAssetBuilder.toString());
                        YAMLSequence newAssetSequence = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLSequence.class);
                        if (newAssetSequence == null) return;
                        PsiElement placeElement = assetSequence.replace(newAssetSequence);
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
