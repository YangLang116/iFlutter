package com.xtu.plugin.flutter.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.YamlPsiUtils;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

//pubspec.yaml Dependency operation action
public abstract class BaseDependencyAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (project == null || editor == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof YAMLFile && YamlPsiUtils.isRootPubspec(((YAMLFile) psiFile)))) {
            e.getPresentation().setVisible(false);
            return;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement yamlPsiElement = psiFile.findElementAt(offset);
        if (yamlPsiElement != null
                && yamlPsiElement.getParent() instanceof YAMLKeyValue
                && YamlPsiUtils.isDependencyElement(((YAMLKeyValue) yamlPsiElement.getParent()))) {
            //select packageName element
            e.getPresentation().setVisible(true);
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    @Nullable
    protected Pair<String, YAMLKeyValue> getPackageInfo(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) return null;
        int offset = editor.getCaretModel().getOffset();
        PsiElement yamlPsiElement = psiFile.findElementAt(offset);
        if (yamlPsiElement == null) return null;
        YAMLKeyValue dependencyPsi = (YAMLKeyValue) yamlPsiElement.getParent();
        return new Pair<>(dependencyPsi.getKeyText(), dependencyPsi);
    }
}
