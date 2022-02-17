package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLPsiElement;

import java.util.List;

public class YamlPsiUtils {

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
        if (parentMappingPsi == null || !(parentMappingPsi.getParent() instanceof YAMLKeyValue)) return false;
        YAMLKeyValue dependencePsi = (YAMLKeyValue) parentMappingPsi.getParent();
        return isDependencyParentPsi(dependencePsi);
    }

    //判断当前YAML文件是否根pubspec.yaml文件
    public static boolean isRootPubspec(@NotNull YAMLPsiElement element) {
        YAMLFile yamlFile = PsiTreeUtil.getParentOfType(element, YAMLFile.class);
        return isRootPubspec(yamlFile);
    }

    public static boolean isRootPubspec(YAMLFile yamlFile) {
        if (yamlFile == null) return false;
        VirtualFile yamlVirtualFile = yamlFile.getVirtualFile();
        if (yamlVirtualFile == null) return false;
        Project project = yamlFile.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return false;
        return StringUtils.equals(yamlVirtualFile.getPath(), projectPath + "/" + PubspecUtils.getFileName());
    }

}
