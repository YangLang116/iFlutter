package com.xtu.plugin.flutter.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.BrowserUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;
import com.xtu.plugin.flutter.store.StorageService;
import com.xtu.plugin.flutter.utils.PubSpecUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import icons.PluginIcons;
import io.flutter.pub.PubRoot;
import io.flutter.sdk.FlutterSdk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.swing.*;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PackageUpdateAnnotator implements Annotator {

    private static final String TIP_TO_FIX = "Update Package";

    private String getPackageUrl(String packageName, YAMLValue value) {
        if (value instanceof YAMLMapping) {
            //git依赖
            YAMLKeyValue git = ((YAMLMapping) value).getKeyValueByKey("git");
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
            //host 依赖
            YAMLKeyValue hosted = ((YAMLMapping) value).getKeyValueByKey("hosted");
            if (hosted != null && hosted.getValue() instanceof YAMLMapping hostValueMap) {
                YAMLKeyValue urlKeyValue = hostValueMap.getKeyValueByKey("url");
                if (urlKeyValue == null) return null;
                return urlKeyValue.getValueText();
            }
            return null;
        }
        return String.format(Locale.ROOT, "https://pub.dev/packages/%s/versions", packageName);
    }

    @Nullable
    private String getCodeVersion(YAMLValue value) {
        if (value instanceof YAMLScalar) {
            return ((YAMLScalar) value).getTextValue();
        } else if (value instanceof YAMLMapping) {
            YAMLKeyValue versionElement = ((YAMLMapping) value).getKeyValueByKey("version");
            if (versionElement == null) return null;
            return versionElement.getValueText();
        }
        return null;
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof YAMLKeyValue
                && PubSpecUtils.isRootPubSpecFile(((YAMLKeyValue) element))
                && PubSpecUtils.isDependencyElement(((YAMLKeyValue) element))) {
            //element is package dependency psi
            String packageName = ((YAMLKeyValue) element).getKeyText();
            if (StringUtils.isEmpty(packageName)) return;
            Project project = element.getProject();
            PackageInfo packageInfo = getPackageInfo(project, packageName);
            if (packageInfo == null) return;
            YAMLValue dependencyWayPsi = ((YAMLKeyValue) element).getValue();
            String codeVersion = getCodeVersion(dependencyWayPsi);
            if (codeVersion == null
                    || codeVersion.contains(packageInfo.latestVersion)
                    || Objects.equals(packageInfo.currentVersion, packageInfo.latestVersion)) return;
            String packageUrl = getPackageUrl(packageName, dependencyWayPsi);
            if (StringUtils.isEmpty(packageUrl)) return;
            holder.newSilentAnnotation(HighlightSeverity.WARNING)
                    .range(element)
                    .gutterIconRenderer(new PackageUpdateIconRenderer(packageUrl, packageInfo))
                    .withFix(new PackageUpdateFix(packageInfo, (YAMLKeyValue) element))
                    .create();
        }
    }

    private PackageInfo getPackageInfo(Project project, String packageName) {
        Map<String, PackageInfo> packageInfoMap = StorageService.getInstance(project).getState().packageInfoMap;
        return packageInfoMap.get(packageName);
    }

    //左侧icon引导
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
            return String.format(Locale.ROOT, "%s can update: %s -> %s\n\n Click to view",
                    packageInfo.name,
                    packageInfo.currentVersion,
                    packageInfo.latestVersion);
        }

        @Override
        @NotNull
        public Icon getIcon() {
            return PluginIcons.DOWNLOAD;
        }

        @Override
        public @Nullable
        AnAction getClickAction() {
            return this.updaterAction;
        }
    }

    private static class PackageUpdaterAction extends AnAction {

        private final String packageUrl;

        private PackageUpdaterAction(String packageUrl) {
            this.packageUrl = packageUrl;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (StringUtils.isEmpty(packageUrl)) return;
            BrowserUtil.open(packageUrl);
        }
    }

    // 自动修复
    private static class PackageUpdateFix extends BaseIntentionAction {

        private final PackageInfo packageInfo;
        private final YAMLKeyValue oldDependencyElement;

        private PackageUpdateFix(PackageInfo packageInfo, YAMLKeyValue yamlKeyValue) {
            this.packageInfo = packageInfo;
            this.oldDependencyElement = yamlKeyValue;
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
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
            YAMLValue value = oldDependencyElement.getValue();
            if (value instanceof YAMLScalar) {
                YAMLKeyValue newDependencyElement = elementGenerator.createYamlKeyValue(packageInfo.name, packageInfo.latestVersion);
                oldDependencyElement.replace(newDependencyElement);
            } else if (value instanceof YAMLMapping) {
                YAMLKeyValue oldVersionElement = ((YAMLMapping) value).getKeyValueByKey("version");
                if (oldVersionElement == null) return;
                YAMLKeyValue newVersionElement = elementGenerator.createYamlKeyValue("version", packageInfo.latestVersion);
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

        //执行pub get
        private void runPubGet(Project project) {
            ApplicationManager.getApplication()
                    .invokeLater(() -> {
                        FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
                        if (sdk != null) {
                            PubRoot root = PubRoot.forDirectory(ProjectUtil.guessProjectDir(project));
                            if (root != null) {
                                sdk.startPubGet(root, project);
                            }
                        }
                    });
        }
    }
}
