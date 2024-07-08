package com.xtu.plugin.flutter.base.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.utils.PubSpecUtils;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

//PubSpec.yaml Dependency operation action
public abstract class YamlDependencyAction extends FlutterProjectAction {

    @Override
    public void whenUpdate(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            e.getPresentation().setVisible(false);
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof YAMLFile && PubSpecUtils.isRootPubSpecFile(((YAMLFile) psiFile)))) {
            e.getPresentation().setVisible(false);
            return;
        }
        int offset = editor.getCaretModel().getOffset();
        boolean isDependencyNode = isDependencyElement(psiFile.findElementAt(offset));
        e.getPresentation().setVisible(isDependencyNode);
    }

    //select packageName element
    private boolean isDependencyElement(@Nullable PsiElement yamlPsiElement) {
        if (yamlPsiElement == null) return false;
        PsiElement parentNode = yamlPsiElement.getParent();
        if (!(parentNode instanceof YAMLKeyValue)) return false;
        return PubSpecUtils.isDependencyElement(((YAMLKeyValue) parentNode));
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
