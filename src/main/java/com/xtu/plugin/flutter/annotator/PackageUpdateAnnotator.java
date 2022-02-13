package com.xtu.plugin.flutter.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.psi.PsiElement;
import com.xtu.plugin.flutter.component.packages.update.PackageInfo;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.CommandUtils;
import com.xtu.plugin.flutter.utils.DartUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import icons.PluginIcons;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import javax.swing.*;
import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PackageUpdateAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof YAMLKeyValue) {
            YAMLKeyValue yamlKeyValue = (YAMLKeyValue) element;
            PsiElement keyElement = yamlKeyValue.getKey();
            if (keyElement == null) return;
            String text = keyElement.getText();
            if (StringUtils.isEmpty(text)) return;
            Project project = element.getProject();
            PackageInfo packageInfo = getPackageInfo(project, text);
            if (packageInfo == null) return;
            holder.newSilentAnnotation(HighlightSeverity.WARNING)
                    .gutterIconRenderer(new PackageUpdateIconRenderer(project, packageInfo))
                    .create();
        }
    }

    private PackageInfo getPackageInfo(Project project, String key) {
        Map<String, PackageInfo> packageInfoMap = StorageService.getInstance(project).getState().packageInfoMap;
        return packageInfoMap.get(key);
    }

    private static class PackageUpdateIconRenderer extends GutterIconRenderer {

        private final PackageInfo packageInfo;
        private final AnAction updateAction;

        private PackageUpdateIconRenderer(Project project, PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
            this.updateAction = new UpdateAction(project, packageInfo);
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

        @Override
        public @Nullable
        AnAction getClickAction() {
            return updateAction;
        }
    }

    private static class UpdateAction extends AnAction {

        private final Project project;
        private final PackageInfo packageInfo;

        private UpdateAction(Project project, PackageInfo packageInfo) {
            this.project = project;
            this.packageInfo = packageInfo;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String projectPath = PluginUtils.getProjectPath(project);
            if (StringUtils.isEmpty(projectPath)) return;
            String flutterPath = DartUtils.getFlutterPath(project);
            if (StringUtils.isEmpty(flutterPath)) return;
            removeAnnotator(packageInfo.name); //remove annotator icon
            String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
            File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
            String command = String.format("%s pub upgrade %s --major-versions", executorFile.getAbsolutePath(), packageInfo.name);
            CommandUtils.executeSync(command, new File(projectPath));
        }

        private void removeAnnotator(String packageName) {
            Map<String, PackageInfo> infoMap = StorageService.getInstance(project).getState().packageInfoMap;
            infoMap.remove(packageName);
        }
    }
}
