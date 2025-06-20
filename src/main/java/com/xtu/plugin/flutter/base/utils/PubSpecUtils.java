package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.xtu.plugin.flutter.base.entity.AssetInfo;
import com.xtu.plugin.flutter.base.entity.AssetInfoMeta;
import com.xtu.plugin.flutter.store.project.AssetRegisterStorageService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@SuppressWarnings("SpellCheckingInspection")
public class PubSpecUtils {

    private static final String NODE_FLUTTER = "flutter";
    private static final String NODE_ASSET = "assets";
    private static final String NODE_FONT = "fonts";
    private static final String NODE_FONT_ASSET = "asset";

    private static final List<String> dependencyList = List.of(
            "dependencies",
            "dev_dependencies",
            "dependency_overrides");

    private static boolean isDependencyParentPsi(YAMLKeyValue psiElement) {
        PsiElement key = psiElement.getKey();
        if (key == null) return false;
        return dependencyList.contains(key.getText());
    }

    //判断当前Element是否是包依赖
    public static boolean isDependencyElement(YAMLKeyValue element) {
        if (element == null) return false;
        YAMLMapping parentMappingPsi = (YAMLMapping) element.getParent();
        if (parentMappingPsi == null || !(parentMappingPsi.getParent() instanceof YAMLKeyValue dependencePsi)) {
            return false;
        }
        return isDependencyParentPsi(dependencePsi);
    }

    public static String getFileName() {
        return "pubspec.yaml";
    }

    //判断当前YAML文件是否根pubspec.yaml文件
    public static boolean isRootPubSpecFile(@NotNull YAMLPsiElement element) {
        YAMLFile yamlFile = PsiTreeUtil.getParentOfType(element, YAMLFile.class);
        return isRootPubSpecFile(yamlFile);
    }

    public static boolean isRootPubSpecFile(@Nullable YAMLFile yamlFile) {
        if (yamlFile == null) return false;
        VirtualFile yamlVirtualFile = yamlFile.getVirtualFile();
        if (yamlVirtualFile == null) return false;
        return isRootPubSpecFile(yamlFile.getProject(), yamlVirtualFile);
    }

    //判断当前文件是否根目录pubspec.yaml文件
    public static boolean isRootPubSpecFile(@NotNull Project project, @Nullable VirtualFile virtualFile) {
        if (virtualFile == null) return false;
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        String rootPubSpecPath = projectPath + "/" + getFileName();
        return StringUtils.equals(rootPubSpecPath, virtualFile.getPath());
    }

    @Nullable
    public static VirtualFile getRootPubSpecFile(@NotNull Project project) {
        VirtualFile rootProjectDir = ProjectUtil.guessProjectDir(project);
        if (rootProjectDir == null) return null;
        return rootProjectDir.findChild(getFileName());
    }

    //获取根目录pubspec.yaml文件psi
    @Nullable
    private static YAMLFile getRootPubSpecPsiFile(@NotNull Project project) {
        VirtualFile rootPubSpecFile = getRootPubSpecFile(project);
        if (rootPubSpecFile == null) return null;
        return (YAMLFile) PsiManager.getInstance(project).findFile(rootPubSpecFile);
    }

    //读取pubspec.yaml文件顶级YAMLValue内容
    @Nullable
    private static YAMLMapping getTopLevelMapping(@NotNull Project project) {
        YAMLFile yamlFile = getRootPubSpecPsiFile(project);
        if (yamlFile == null) return null;
        YAMLDocument document = yamlFile.getDocuments().get(0);
        return (YAMLMapping) document.getTopLevelValue();
    }

    //读取pubspec.yaml文件中flutter节点
    private static YAMLMapping getFlutterMapping(@NotNull Project project) {
        YAMLMapping rootMapping = getTopLevelMapping(project);
        if (rootMapping == null) return null;
        YAMLKeyValue flutterKeyValue = rootMapping.getKeyValueByKey(NODE_FLUTTER);
        if (flutterKeyValue == null) return null;
        YAMLValue flutterValue = flutterKeyValue.getValue();
        if (flutterValue == null) return null;
        return ((YAMLMapping) flutterValue);
    }

    //读取flutter节点中sequence内容
    @Nullable
    private static YAMLSequence getFlutterSequence(@NotNull Project project, String key) {
        YAMLMapping flutterMapping = getFlutterMapping(project);
        if (flutterMapping == null) return null;
        YAMLKeyValue keyValue = flutterMapping.getKeyValueByKey(key);
        if (keyValue == null) return null;
        YAMLValue value = keyValue.getValue();
        if (value == null) return null;
        return ((YAMLSequence) value);
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
    public static AssetInfo readAssetList(@NotNull Project project) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        //asset list
        List<String> assetList = new ArrayList<>();
        if (AssetUtils.isFoldRegister(project)) {
            assetList.addAll(AssetRegisterStorageService.getAssetList(project));
        } else {
            YAMLSequence assetSequence = getFlutterSequence(project, NODE_ASSET);
            if (assetSequence != null) {
                List<YAMLSequenceItem> assetSequenceItems = assetSequence.getItems();
                for (YAMLSequenceItem assetSequenceItem : assetSequenceItems) {
                    YAMLValue itemValue = assetSequenceItem.getValue();
                    if (itemValue == null) continue;
                    String itemText = itemValue.getText();
                    if (StringUtils.isEmpty(itemText)) continue;
                    assetList.add(itemText);
                }
                CollectionUtils.standardList(assetList);
            }
        }
        //font family list
        List<String> fontList = new ArrayList<>();
        YAMLSequence fontSequence = getFlutterSequence(project, NODE_FONT);
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
                    fontList.add(fontAsset);
                }
            }
            CollectionUtils.standardList(fontList);
        }
        String projectName = getProjectName(project);
        String projectVersion = getProjectVersion(project);
        AssetInfoMeta metaEntity = new AssetInfoMeta(projectName, projectVersion);
        return new AssetInfo(metaEntity, assetList, fontList);
    }

    //更新资源列表
    public static void writeAssetList(@NotNull Project project,
                                      @NotNull List<String> assetList,
                                      @NotNull List<String> fontList) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        CollectionUtils.standardList(assetList);
        CollectionUtils.standardList(fontList);
        YAMLSequence oldAssetSequence = getFlutterSequence(project, NODE_ASSET);
        YAMLSequence oldFontSequence = getFlutterSequence(project, NODE_FONT);
        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
        //modify asset and font
        boolean assetModifyFail = modifyAsset(project, assetList, oldAssetSequence, elementGenerator);
        boolean fontModifyFail = modifyFont(project, fontList, oldFontSequence, elementGenerator);
        if (assetModifyFail && fontModifyFail) return;
        //save document
        YAMLFile rootPubspecFile = getRootPubSpecPsiFile(project);
        assert rootPubspecFile != null;
        PsiUtils.savePsiFile(project, rootPubspecFile);
        //refresh UI
        notifyPubSpecUpdate(project);
    }

    private static boolean modifyAsset(@NotNull Project project,
                                       @NotNull List<String> assetList,
                                       @Nullable YAMLSequence oldAssetSequence,
                                       @NotNull YAMLElementGenerator elementGenerator) {
        if (AssetUtils.isFoldRegister(project)) {
            String projectName = getProjectName(project);
            String projectVersion = getProjectVersion(project);
            assetList = AssetRegisterStorageService.updateAsset(project, projectName, projectVersion, assetList);
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
            if (flutterKeyValue == null || !(flutterKeyValue.getValue() instanceof YAMLMapping flutterValue)) {
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
            if (flutterKeyValue == null || !(flutterKeyValue.getValue() instanceof YAMLMapping flutterValue)) {
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

    private static void notifyPubSpecUpdate(Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            statusBar.setInfo(String.format("update %s success", getFileName()));
        }
    }
}
