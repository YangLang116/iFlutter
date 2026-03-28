package com.xtu.plugin.flutter.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.BrowserUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.xtu.plugin.flutter.annotator.packages.update.PackageInfo;
import com.xtu.plugin.flutter.base.utils.PubSpecUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import icons.PluginIcons;
import io.flutter.pub.PubRoot;
import io.flutter.sdk.FlutterSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.*;

import javax.swing.*;
import java.util.*;

public class PackageUpdateAnnotator extends ExternalAnnotator<
        PackageUpdateAnnotator.CollectInfo,
        List<PackageUpdateAnnotator.AnnotationData>> {

    private static final String TIP_TO_FIX = "Update Package";
    private static final List<String> DEPENDENCY_KEYS = Arrays.asList(
            "dependencies", "dev_dependencies", "dependency_overrides");

    // 在 EDT 中收集的包 PSI 信息（PSI 指针 + 纯数据）
    static class PackagePsiInfo {
        final SmartPsiElementPointer<YAMLKeyValue> pointer;
        final String packageName;
        final String codeVersion;
        final String packageUrl;

        PackagePsiInfo(SmartPsiElementPointer<YAMLKeyValue> pointer,
                       String packageName, String codeVersion, String packageUrl) {
            this.pointer = pointer;
            this.packageName = packageName;
            this.codeVersion = codeVersion;
            this.packageUrl = packageUrl;
        }
    }

    // collectInformation() 返回类型
    static class CollectInfo {
        final List<PackagePsiInfo> packages;
        final Map<String, PackageInfo> packageInfoMap;

        CollectInfo(List<PackagePsiInfo> packages, Map<String, PackageInfo> packageInfoMap) {
            this.packages = packages;
            this.packageInfoMap = packageInfoMap;
        }
    }

    // doAnnotate() 返回类型（不含 PSI 的纯数据 + 指针）
    static class AnnotationData {
        final SmartPsiElementPointer<YAMLKeyValue> pointer;
        final PackageInfo packageInfo;
        final String packageUrl;

        AnnotationData(SmartPsiElementPointer<YAMLKeyValue> pointer,
                       PackageInfo packageInfo, String packageUrl) {
            this.pointer = pointer;
            this.packageInfo = packageInfo;
            this.packageUrl = packageUrl;
        }
    }

    // ---- Step 1: 在 EDT 中每个文件执行一次 ----

    @Override
    @Nullable
    public CollectInfo collectInformation(@NotNull PsiFile file) {
        if (!(file instanceof YAMLFile)) return null;
        Project project = file.getProject();
        if (!PubSpecUtils.isRootPubSpecFile(project, file.getVirtualFile())) return null;

        Map<String, PackageInfo> packageInfoMap = ProjectStorageService.getStorage(project).packageInfoMap;
        if (packageInfoMap.isEmpty()) return null;

        List<PackagePsiInfo> packages = collectPackages((YAMLFile) file);
        if (packages.isEmpty()) return null;

        // 复制 packageInfoMap 快照，确保后台线程安全访问
        return new CollectInfo(packages, new HashMap<>(packageInfoMap));
    }

    private List<PackagePsiInfo> collectPackages(YAMLFile yamlFile) {
        List<YAMLDocument> documents = yamlFile.getDocuments();
        if (documents.isEmpty()) return Collections.emptyList();

        YAMLValue topLevelValue = documents.get(0).getTopLevelValue();
        if (!(topLevelValue instanceof YAMLMapping)) return Collections.emptyList();
        YAMLMapping topLevelMapping = (YAMLMapping) topLevelValue;

        List<PackagePsiInfo> result = new ArrayList<>();
        SmartPointerManager pointerManager = SmartPointerManager.getInstance(yamlFile.getProject());

        for (String depKey : DEPENDENCY_KEYS) {
            YAMLKeyValue depSection = topLevelMapping.getKeyValueByKey(depKey);
            if (depSection == null) continue;
            if (!(depSection.getValue() instanceof YAMLMapping depMapping)) continue;

            for (YAMLKeyValue packageEntry : depMapping.getKeyValues()) {
                String packageName = packageEntry.getKeyText();
                if (StringUtils.isEmpty(packageName)) continue;

                YAMLValue depValue = packageEntry.getValue();
                // path 依赖不是远程依赖，跳过
                if (depValue instanceof YAMLMapping
                        && ((YAMLMapping) depValue).getKeyValueByKey("path") != null) {
                    continue;
                }

                String packageUrl = getPackageUrl(packageName, depValue);
                if (StringUtils.isEmpty(packageUrl)) continue;

                String codeVersion = getCodeVersion(depValue);
                SmartPsiElementPointer<YAMLKeyValue> pointer =
                        pointerManager.createSmartPsiElementPointer(packageEntry);
                result.add(new PackagePsiInfo(pointer, packageName, codeVersion, packageUrl));
            }
        }
        return result;
    }

    // ---- Step 2: 在后台线程中执行 ----

    @Override
    @NotNull
    public List<AnnotationData> doAnnotate(CollectInfo info) {
        List<AnnotationData> result = new ArrayList<>();
        for (PackagePsiInfo pkg : info.packages) {
            PackageInfo packageInfo = info.packageInfoMap.get(pkg.packageName);
            if (packageInfo == null) continue;
            if (pkg.codeVersion == null) continue;
            if (pkg.codeVersion.contains(packageInfo.latestVersion)) continue;
            if (Objects.equals(packageInfo.currentVersion, packageInfo.latestVersion)) continue;
            result.add(new AnnotationData(pkg.pointer, packageInfo, pkg.packageUrl));
        }
        return result;
    }

    // ---- Step 3: 在 EDT 中应用注解 ----

    @Override
    public void apply(@NotNull PsiFile file, @NotNull List<AnnotationData> results,
                      @NotNull AnnotationHolder holder) {
        for (AnnotationData data : results) {
            YAMLKeyValue element = data.pointer.getElement();
            if (element == null || !element.isValid()) continue;
            holder.newSilentAnnotation(HighlightSeverity.WARNING)
                    .range(element)
                    .gutterIconRenderer(new PackageUpdateIconRenderer(data.packageUrl, data.packageInfo))
                    .withFix(new PackageUpdateFix(data.packageInfo, element))
                    .create();
        }
    }

    // ---- 辅助方法（仅在 collectInformation() / EDT 中调用）----

    private String getPackageUrl(String packageName, YAMLValue value) {
        if (value instanceof YAMLMapping mapping) {
            // git 依赖
            YAMLKeyValue git = mapping.getKeyValueByKey("git");
            if (git != null && git.getValue() instanceof YAMLMapping gitValueMap) {
                YAMLKeyValue urlKeyValue = gitValueMap.getKeyValueByKey("url");
                if (urlKeyValue == null) return null;
                String url = urlKeyValue.getValueText();
                if (StringUtils.isEmpty(url)) return url;
                url = url.replace(".git", "");
                if (url.startsWith("git@")) {
                    url = url.replace("git@", "https://").replace(":", "/");
                }
                return url;
            }
            // hosted 依赖
            YAMLKeyValue hosted = mapping.getKeyValueByKey("hosted");
            if (hosted != null && hosted.getValue() instanceof YAMLMapping hostValueMap) {
                YAMLKeyValue urlKeyValue = hostValueMap.getKeyValueByKey("url");
                if (urlKeyValue == null) return null;
                return urlKeyValue.getValueText();
            }
            return null;
        }
        return String.format("https://pub.dev/packages/%s/versions", packageName);
    }

    @Nullable
    private String getCodeVersion(YAMLValue value) {
        if (value instanceof YAMLScalar) {
            return ((YAMLScalar) value).getTextValue();
        } else if (value instanceof YAMLMapping mapping) {
            YAMLKeyValue versionElement = mapping.getKeyValueByKey("version");
            if (versionElement == null) return null;
            return versionElement.getValueText();
        }
        return null;
    }

    // ---- 内部类：Gutter 图标 ----

    private static class PackageUpdateIconRenderer extends GutterIconRenderer {

        private final AnAction updaterAction;
        private final PackageInfo packageInfo;

        private PackageUpdateIconRenderer(String packageUrl, PackageInfo packageInfo) {
            this.updaterAction = new PackageUpdaterAction(packageUrl);
            this.packageInfo = packageInfo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PackageUpdateIconRenderer that = (PackageUpdateIconRenderer) o;
            return packageInfo.equals(that.packageInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(packageInfo);
        }

        @Override
        @Nullable
        public String getTooltipText() {
            return String.format("%s can update: %s -> %s\n\n Click to view",
                    packageInfo.name, packageInfo.currentVersion, packageInfo.latestVersion);
        }

        @Override
        @NotNull
        public Icon getIcon() {
            return PluginIcons.DOWNLOAD;
        }

        @Override
        @Nullable
        public AnAction getClickAction() {
            return this.updaterAction;
        }
    }

    private static class PackageUpdaterAction extends AnAction {

        private final String packageUrl;

        private PackageUpdaterAction(String packageUrl) {
            this.packageUrl = packageUrl;
        }

        @Override
        @NotNull
        public ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (StringUtils.isEmpty(packageUrl)) return;
            BrowserUtil.open(packageUrl);
        }
    }

    // ---- 内部类：QuickFix ----

    private static class PackageUpdateFix extends BaseIntentionAction {

        private final PackageInfo packageInfo;
        private final SmartPsiElementPointer<YAMLKeyValue> oldDependencyPointer;

        private PackageUpdateFix(PackageInfo packageInfo, YAMLKeyValue yamlKeyValue) {
            this.packageInfo = packageInfo;
            this.oldDependencyPointer = SmartPointerManager.getInstance(yamlKeyValue.getProject())
                    .createSmartPsiElementPointer(yamlKeyValue);
        }

        @Override
        @NotNull
        @IntentionFamilyName
        public String getFamilyName() {
            return TIP_TO_FIX;
        }

        @Override
        @IntentionName
        @NotNull
        public String getText() {
            return TIP_TO_FIX;
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return oldDependencyPointer.getElement() != null;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file)
                throws IncorrectOperationException {
            YAMLKeyValue oldDependencyElement = oldDependencyPointer.getElement();
            if (oldDependencyElement == null) return;
            YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
            YAMLValue value = oldDependencyElement.getValue();
            if (value instanceof YAMLScalar) {
                YAMLKeyValue newDependencyElement = elementGenerator
                        .createYamlKeyValue(packageInfo.name, packageInfo.latestVersion);
                oldDependencyElement.replace(newDependencyElement);
            } else if (value instanceof YAMLMapping mapping) {
                YAMLKeyValue oldVersionElement = mapping.getKeyValueByKey("version");
                if (oldVersionElement == null) return;
                YAMLKeyValue newVersionElement = elementGenerator
                        .createYamlKeyValue("version", packageInfo.latestVersion);
                oldVersionElement.replace(newVersionElement);
            }
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(file);
            if (document != null) {
                psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            }
            psiDocumentManager.commitAllDocuments();
            runPubGet(project);
        }

        private void runPubGet(Project project) {
            ApplicationManager.getApplication().invokeLater(() -> {
                FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
                PubRoot root = PubRoot.forDirectory(ProjectUtil.guessProjectDir(project));
                if (sdk != null && root != null) {
                    sdk.startPubGet(root, project);
                }
            });
        }
    }
}
