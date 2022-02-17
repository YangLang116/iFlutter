package com.xtu.plugin.flutter.annotator;

import com.intellij.ide.BrowserUtil;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.YamlPsiUtils;
import icons.PluginIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import javax.swing.*;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PackageUpdateAnnotator implements Annotator {

//    private static final String TIP_TO_FIX = "Package can Update";

    private String getPackageUrl(String packageName, YAMLValue value) {
        if (value instanceof YAMLMapping) {
            //git依赖
            YAMLKeyValue git = ((YAMLMapping) value).getKeyValueByKey("git");
            if (git != null && git.getValue() instanceof YAMLMapping) {
                YAMLMapping gitValueMap = (YAMLMapping) git.getValue();
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
            if (hosted != null && hosted.getValue() instanceof YAMLMapping) {
                YAMLMapping hostValueMap = (YAMLMapping) hosted.getValue();
                YAMLKeyValue urlKeyValue = hostValueMap.getKeyValueByKey("url");
                if (urlKeyValue == null) return null;
                return urlKeyValue.getValueText();
            }
            return null;
        }
        return String.format(Locale.ROOT, "https://pub.dev/packages/%s/versions", packageName);
    }

    @Override

    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof YAMLKeyValue
                && YamlPsiUtils.isRootPubspec(((YAMLKeyValue) element))
                && YamlPsiUtils.isDependencyElement(((YAMLKeyValue) element))) {
            //element is package dependency psi
            String packageName = ((YAMLKeyValue) element).getKeyText();
            if (StringUtils.isEmpty(packageName)) return;
            Project project = element.getProject();
            PackageInfo packageInfo = getPackageInfo(project, packageName);
            if (packageInfo == null) return;
            YAMLValue dependencyWayPsi = ((YAMLKeyValue) element).getValue();
            String packageUrl = getPackageUrl(packageName, dependencyWayPsi);
            if (StringUtils.isEmpty(packageUrl)) return;
            holder.newSilentAnnotation(HighlightSeverity.WARNING)
                    .range(element)
                    .gutterIconRenderer(new PackageUpdateIconRenderer(packageUrl, packageInfo))
//                    .withFix(new PackageUpdateFix(packageInfo))
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
            return String.format(Locale.ROOT, "%s can update: %s -> %s\n\n 点击查看",
                    packageInfo.name,
                    packageInfo.currentVersion,
                    packageInfo.latestVersion);
        }

        @Override
        @NotNull
        public Icon getIcon() {
            return PluginIcons.LOGO;
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

//    自动修复
//    private static class PackageUpdateFix extends BaseIntentionAction {
//
//        private final PackageInfo packageInfo;
//
//        private PackageUpdateFix(PackageInfo packageInfo) {
//            this.packageInfo = packageInfo;
//        }
//
//        @Override
//        @NotNull
//        @IntentionFamilyName
//        public String getFamilyName() {
//            return TIP_TO_FIX;
//        }
//
//        @Override
//        @IntentionName
//        @NotNull
//        public String getText() {
//            return getTipInfo();
//        }
//
//        private String getTipInfo() {
//            return String.format(Locale.ROOT, "%s can update: %s -> %s",
//                    packageInfo.name,
//                    packageInfo.currentVersion,
//                    packageInfo.latestVersion);
//        }
//
//        @Override
//        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
//            return true;
//        }
//
//        @Override
//        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
//            String projectPath = PluginUtils.getProjectPath(project);
//            if (StringUtils.isEmpty(projectPath)) return;
//            String flutterPath = DartUtils.getFlutterPath(project);
//            if (StringUtils.isEmpty(flutterPath)) return;
//            String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
//            File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
//            final String command = String.format(Locale.ROOT, "%s pub upgrade %s --major-versions",
//                    executorFile.getAbsolutePath(),
//                    packageInfo.name);
//            new Task.Backgroundable(project, getTipInfo(), false) {
//                @Override
//                public void run(@NotNull ProgressIndicator indicator) {
//                    indicator.setIndeterminate(true);
//                    CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, new File(projectPath));
//                    ApplicationManager.getApplication().invokeLater(() -> refreshPsi(commandResult));
//                    indicator.setIndeterminate(false);
//                    indicator.setFraction(1);
//                }
//
//                private void refreshPsi(CommandUtils.CommandResult commandResult) {
//                    if (commandResult.code == CommandUtils.CommandResult.SUCCESS) {
//                        removeAnnotator(project, packageInfo.name); //remove annotator icon
//                        file.getVirtualFile().refresh(true, false,
//                                () -> ToastUtil.make(project, MessageType.INFO, packageInfo.name + " Update Success"));
//                    } else {
//                        ToastUtil.make(project, MessageType.ERROR, packageInfo.name + " Update fail");
//                    }
//                }
//
//                private void removeAnnotator(Project project, String packageName) {
//                    Map<String, PackageInfo> infoMap = StorageService.getInstance(project).getState().packageInfoMap;
//                    infoMap.remove(packageName);
//                }
//            }.queue();
//        }
//    }
}
