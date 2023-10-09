package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.xtu.plugin.flutter.store.asset.AssetRegisterStorageService;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.*;

import java.util.*;

public class PubspecUtils {

    private static final String NODE_FLUTTER = "flutter";
    private static final String NODE_ASSET = "assets";
    private static final String NODE_FONT = "fonts";
    private static final String NODE_FONT_ASSET = "asset";

    public static String getFileName() {
        return "pubspec.yaml";
    }

    //判断当前文件是否根目录pubspec.yaml文件
    public static boolean isRootPubspecFile(@NotNull Project project, @NotNull VirtualFile virtualFile) {
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

    //读取flutter#font内容
    @Nullable
    private static YAMLSequence getFontSequence(@NotNull Project project) {
        YAMLMapping rootMapping = getTopLevelMapping(project);
        if (rootMapping == null) return null;
        YAMLKeyValue flutterKeyValue = rootMapping.getKeyValueByKey(NODE_FLUTTER);
        if (flutterKeyValue == null) return null;
        YAMLValue flutterValue = flutterKeyValue.getValue();
        if (!(flutterValue instanceof YAMLMapping)) return null;
        YAMLKeyValue assetKeyValue = ((YAMLMapping) flutterValue).getKeyValueByKey(NODE_FONT);
        if (assetKeyValue == null) return null;
        YAMLValue fontValue = assetKeyValue.getValue();
        if (!(fontValue instanceof YAMLSequence)) return null;
        return (YAMLSequence) fontValue;
    }

    @NotNull
    private static String getProjectName(@NotNull Project project) {
        YAMLMapping rootMapping = getTopLevelMapping(project);
        if (rootMapping == null) return project.getName();
        YAMLKeyValue nameKeyValue = rootMapping.getKeyValueByKey("name");
        if (nameKeyValue == null) return project.getName();
        return nameKeyValue.getValueText();
    }

    @NotNull
    private static String getProjectVersion(@NotNull Project project) {
        final String defaultVersion = "1.0.0";
        YAMLMapping rootMapping = getTopLevelMapping(project);
        if (rootMapping == null) return defaultVersion;
        YAMLKeyValue versionKeyValue = rootMapping.getKeyValueByKey("version");
        if (versionKeyValue == null) return defaultVersion;
        return versionKeyValue.getValueText();
    }


    // 读取资源列表
    public static void readAsset(@NotNull Project project, @NotNull YamlReadListener listener) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> WriteAction.run(() -> {
            if (project.isDisposed()) return;
            PsiDocumentManager.getInstance(project).commitAllDocuments();
            readAssetInReadAction(project, listener);
        }));
    }

    private static void readAssetInReadAction(@NotNull Project project, @NotNull YamlReadListener listener) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() -> ReadAction.run(() -> {
            //asset list
            List<String> assetList = new ArrayList<>();
            if (AssetUtils.isFoldRegister(project)) {
                assetList.addAll(AssetRegisterStorageService.getAssetList(project));
            } else {
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
            }
            //font family list
            List<String> fontAssetList = new ArrayList<>();
            YAMLSequence fontSequence = getFontSequence(project);
            if (fontSequence != null) {
                List<YAMLSequenceItem> fontSequenceItems = fontSequence.getItems();
                for (YAMLSequenceItem fontSequenceItem : fontSequenceItems) {
                    YAMLValue fontValue = fontSequenceItem.getValue();
                    if (!(fontValue instanceof YAMLMapping)) continue;
                    YAMLKeyValue inFontKeyValue = ((YAMLMapping) fontValue).getKeyValueByKey(NODE_FONT);
                    if (inFontKeyValue == null) continue;
                    YAMLValue inFontValue = inFontKeyValue.getValue();
                    if (!(inFontValue instanceof YAMLSequence)) continue;
                    List<YAMLSequenceItem> items = ((YAMLSequence) inFontValue).getItems();
                    for (YAMLSequenceItem fontItem : items) {
                        if (!(fontItem.getValue() instanceof YAMLMapping)) continue;
                        YAMLKeyValue fontAssetKeyValue = ((YAMLMapping) fontItem.getValue()).getKeyValueByKey(NODE_FONT_ASSET);
                        if (fontAssetKeyValue == null) continue;
                        String fontAsset = fontAssetKeyValue.getValueText();
                        if (StringUtils.isEmpty(fontAsset)) continue;
                        fontAssetList.add(fontAsset);
                    }
                }
                CollectionUtils.duplicateList(fontAssetList);
                Collections.sort(fontAssetList);
            }
            //project name
            String projectName = getProjectName(project);
            //project version
            String projectVersion = getProjectVersion(project);
            listener.onGet(projectName, projectVersion, assetList, fontAssetList);
        }));
    }

    //更新资源列表
    //该方法必须运行在EDT#ReadAction中
    public static void writeAsset(@NotNull Project project,
                                  @NotNull List<String> assetList,
                                  @NotNull List<String> fontList) {
        //pure asset list
        CollectionUtils.duplicateList(assetList);
        Collections.sort(assetList);
        //pure font list
        CollectionUtils.duplicateList(fontList);
        Collections.sort(fontList);

        YAMLSequence oldAssetSequence = getAssetSequence(project);
        YAMLSequence oldFontSequence = getFontSequence(project);
        WriteAction.run(() -> WriteCommandAction.runWriteCommandAction(project, () -> {
            YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
            //modify asset
            if (modifyAsset(project, assetList, oldAssetSequence, elementGenerator)) return;
            //modify font
            if (modifyFont(project, fontList, oldFontSequence, elementGenerator)) return;
            YAMLFile rootPubspecFile = getRootPubspecFile(project);
            assert rootPubspecFile != null;
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(rootPubspecFile);
            if (document != null) {
                //sync psi - document
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
                //sync psi - vfs
                FileDocumentManager.getInstance().saveDocument(document);
            }
            //refresh UI
            notifyPubspecUpdate(project);
        }));
    }

    private static boolean modifyAsset(@NotNull Project project,
                                       @NotNull List<String> assetList,
                                       @Nullable YAMLSequence oldAssetSequence,
                                       @NotNull YAMLElementGenerator elementGenerator) {
        if (AssetUtils.isFoldRegister(project)) {
            assetList = AssetRegisterStorageService.updateAsset(project, assetList);
        }
        if (oldAssetSequence != null) {
            if (CollectionUtils.isEmpty(assetList)) {
                oldAssetSequence.getParent().delete();
                return false;
            }
            StringBuilder newAssetBuilder = new StringBuilder();
            for (String asset : assetList) {
                newAssetBuilder.append("- ").append(asset).append("\n");
            }
            YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(newAssetBuilder.toString());
            YAMLSequence newAssetSequence = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLSequence.class);
            if (newAssetSequence == null) return true;
            oldAssetSequence.replace(newAssetSequence);
        } else if (!CollectionUtils.isEmpty(assetList)) {
            YAMLMapping topLevelMapping = getTopLevelMapping(project);
            if (topLevelMapping == null) return true;
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
                if (newFlutterKeyValue == null) return true;
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
                if (newAssetKeyValue == null) return true;
                flutterValue.putKeyValue(newAssetKeyValue);
            }
        }
        return false;
    }

    //修复font#font节点
    private static boolean modifyFont(@NotNull Project project,
                                      @NotNull List<String> fontList,
                                      @Nullable YAMLSequence oldFontSequence,
                                      @NotNull YAMLElementGenerator elementGenerator) {
        if (oldFontSequence != null) {
            if (CollectionUtils.isEmpty(fontList)) {
                oldFontSequence.getParent().delete();
                return false;
            }
            //fonts:
            //    - family: test
            //      fonts:
            //        - asset: fonts/test.ttf
            //        - asset: fonts/test@style_italic.ttf
            //          style: italic
            List<FontUtils.FontFamily> fontFamilyList = FontUtils.getFontFamilyList(fontList);
            StringBuilder newFontBuilder = new StringBuilder();
            for (FontUtils.FontFamily family : fontFamilyList) {
                newFontBuilder.append("- family: ").append(family.family).append("\n");
                newFontBuilder.append("  fonts:").append("\n");
                for (FontUtils.FontAsset font : family.fonts) {
                    newFontBuilder.append("    - asset: ").append(font.asset).append("\n");
                    for (Map.Entry<String, String> extras : font.extras.entrySet()) {
                        String extraLine = String.format("      %s: %s", extras.getKey(), extras.getValue());
                        newFontBuilder.append(extraLine).append("\n");
                    }
                }
            }
            YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(newFontBuilder.toString());
            YAMLSequence newFontSequence = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLSequence.class);
            if (newFontSequence == null) return true;
            oldFontSequence.replace(newFontSequence);
        } else if (!CollectionUtils.isEmpty(fontList)) {
            YAMLMapping topLevelMapping = getTopLevelMapping(project);
            if (topLevelMapping == null) return true;
            YAMLKeyValue flutterKeyValue = topLevelMapping.getKeyValueByKey(NODE_FLUTTER);
            if (flutterKeyValue == null || !(flutterKeyValue.getValue() instanceof YAMLMapping)) {
                StringBuilder flutterFontBuilder = new StringBuilder()
                        .append("flutter:").append("\n")
                        .append("  fonts:").append("\n");
                List<FontUtils.FontFamily> fontFamilyList = FontUtils.getFontFamilyList(fontList);
                for (FontUtils.FontFamily family : fontFamilyList) {
                    flutterFontBuilder.append("    - family: ").append(family.family).append("\n");
                    flutterFontBuilder.append("      fonts:").append("\n");
                    for (FontUtils.FontAsset font : family.fonts) {
                        flutterFontBuilder.append("        - asset: ").append(font.asset).append("\n");
                        for (Map.Entry<String, String> extras : font.extras.entrySet()) {
                            String extraLine = String.format("          %s: %s", extras.getKey(), extras.getValue());
                            flutterFontBuilder.append(extraLine).append("\n");
                        }
                    }
                }
                YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(flutterFontBuilder.toString());
                YAMLKeyValue newFlutterKeyValue = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLKeyValue.class);
                if (newFlutterKeyValue == null) return true;
                topLevelMapping.putKeyValue(newFlutterKeyValue);
            } else {
                YAMLMapping flutterValue = ((YAMLMapping) flutterKeyValue.getValue());
//            fonts:
//              - family: DINPro_CondBold
//                fonts:
//                  - asset: assets/fonts/DINPro_CondBold.otf
                StringBuilder newFontBuilder = new StringBuilder();
                newFontBuilder.append("fonts:").append("\n");
                List<FontUtils.FontFamily> fontFamilyList = FontUtils.getFontFamilyList(fontList);
                for (FontUtils.FontFamily family : fontFamilyList) {
                    newFontBuilder.append("  - family: ").append(family.family).append("\n");
                    newFontBuilder.append("    fonts:").append("\n");
                    for (FontUtils.FontAsset font : family.fonts) {
                        newFontBuilder.append("      - asset: ").append(font.asset).append("\n");
                        for (Map.Entry<String, String> extras : font.extras.entrySet()) {
                            String extraLine = String.format("        %s: %s", extras.getKey(), extras.getValue());
                            newFontBuilder.append(extraLine).append("\n");
                        }
                    }
                }
                YAMLFile tempYamlFile = elementGenerator.createDummyYamlWithText(newFontBuilder.toString());
                YAMLKeyValue fontKeyValue = PsiTreeUtil.findChildOfType(tempYamlFile, YAMLKeyValue.class);
                if (fontKeyValue == null) return true;
                flutterValue.putKeyValue(fontKeyValue);
            }
        }
        return false;
    }

    private static void notifyPubspecUpdate(Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            statusBar.setInfo(String.format(Locale.ROOT, "update %s success", getFileName()));
        }
    }

    public interface YamlReadListener {

        void onGet(@NotNull String projectName,
                   @NotNull String projectVersion,
                   @NotNull List<String> assetList,
                   @NotNull List<String> fontList);

    }
}
