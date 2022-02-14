package com.xtu.plugin.flutter.annotator;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.*;
import icons.PluginIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PackageUpdateAnnotator implements Annotator {

    private static final String TIP_TO_FIX = "Package can Update";

    private boolean isRootPubspec(@NotNull PsiElement element) {
        YAMLFile yamlFile = PsiTreeUtil.getParentOfType(element, YAMLFile.class);
        if (yamlFile == null) return false;
        VirtualFile yamlVirtualFile = yamlFile.getVirtualFile();
        if (yamlVirtualFile == null) return false;
        Project project = element.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        return StringUtils.equals(yamlVirtualFile.getPath(), projectPath + "/" + PubspecUtils.getFileName());
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (isRootPubspec(element) && element instanceof YAMLKeyValue) {
            YAMLKeyValue yamlKeyValue = (YAMLKeyValue) element;
            PsiElement keyElement = yamlKeyValue.getKey();
            if (keyElement == null) return;
            String text = keyElement.getText();
            if (StringUtils.isEmpty(text)) return;
            Project project = element.getProject();
            PackageInfo packageInfo = getPackageInfo(project, text);
            if (packageInfo == null) return;
            holder.newAnnotation(HighlightSeverity.WARNING, TIP_TO_FIX)
                    .range(keyElement)
                    .gutterIconRenderer(new PackageUpdateIconRenderer(packageInfo))
                    .withFix(new PackageUpdateFix(packageInfo))
                    .create();
        }
    }

    private PackageInfo getPackageInfo(Project project, String key) {
        Map<String, PackageInfo> packageInfoMap = StorageService.getInstance(project).getState().packageInfoMap;
        return packageInfoMap.get(key);
    }

    private static class PackageUpdateIconRenderer extends GutterIconRenderer {

        private final PackageInfo packageInfo;

        private PackageUpdateIconRenderer(PackageInfo packageInfo) {
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
        public @Nullable
        String getTooltipText() {
            return String.format(Locale.ROOT, "%s can update: %s -> %s",
                    packageInfo.name,
                    packageInfo.currentVersion,
                    packageInfo.latestVersion);
        }

        @Override
        public @NotNull
        Icon getIcon() {
            return PluginIcons.LOGO;
        }
    }

    //自动修复
    private static class PackageUpdateFix extends BaseIntentionAction {

        private final PackageInfo packageInfo;

        public PackageUpdateFix(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
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
            return getTipInfo();
        }

        private String getTipInfo() {
            return String.format(Locale.ROOT, "%s can update: %s -> %s",
                    packageInfo.name,
                    packageInfo.currentVersion,
                    packageInfo.latestVersion);
        }

        @Override
        public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
            return true;
        }

        @Override
        public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
            String projectPath = PluginUtils.getProjectPath(project);
            if (StringUtils.isEmpty(projectPath)) return;
            String flutterPath = DartUtils.getFlutterPath(project);
            if (StringUtils.isEmpty(flutterPath)) return;
            String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
            File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
            final String command = String.format(Locale.ROOT, "%s pub upgrade %s --major-versions",
                    executorFile.getAbsolutePath(),
                    packageInfo.name);
            new Task.Backgroundable(project, getTipInfo(), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, new File(projectPath));
                    ApplicationManager.getApplication().invokeLater(() -> refreshPsi(commandResult));
                    indicator.setIndeterminate(false);
                    indicator.setFraction(1);
                }

                private void refreshPsi(CommandUtils.CommandResult commandResult) {
                    if (commandResult.code == CommandUtils.CommandResult.SUCCESS) {
                        removeAnnotator(project, packageInfo.name); //remove annotator icon
                        file.getVirtualFile().refresh(true, false,
                                () -> ToastUtil.make(project, MessageType.INFO, packageInfo.name + " Update Success"));
                    } else {
                        ToastUtil.make(project, MessageType.ERROR, packageInfo.name + " Update fail");
                    }
                }

                private void removeAnnotator(Project project, String packageName) {
                    Map<String, PackageInfo> infoMap = StorageService.getInstance(project).getState().packageInfoMap;
                    infoMap.remove(packageName);
                }
            }.queue();
        }
    }
}
