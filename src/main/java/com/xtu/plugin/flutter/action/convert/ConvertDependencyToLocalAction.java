package com.xtu.plugin.flutter.action.convert;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.xtu.plugin.flutter.action.convert.task.ConvertDependencyToLocalTask;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import com.xtu.plugin.flutter.utils.YamlPsiUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.util.List;

public class ConvertDependencyToLocalAction extends AnAction {

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

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (editor == null || psiFile == null) return;
        int offset = editor.getCaretModel().getOffset();
        PsiElement yamlPsiElement = psiFile.findElementAt(offset);
        if (yamlPsiElement == null) return;
        String packageName = ((YAMLKeyValue) yamlPsiElement.getParent()).getKeyText();
        if (StringUtils.isEmpty(packageName)) return;
        FileChooserDescriptor chooserDescriptor = new FileChooserDescriptor(false, true,
                false, false, false, false);
        chooserDescriptor.setTitle("选择依赖存放位置");
        VirtualFile chooseDirectory = FileChooser.chooseFile(chooserDescriptor, project, ProjectUtil.guessProjectDir(project));
        if (chooseDirectory == null) return;
        String filePath = chooseDirectory.getPath();
        if (!filePath.startsWith(projectPath)) {
            ToastUtil.make(project, MessageType.ERROR, "请选择当前项目下的文件目录存放依赖");
            return;
        }
        //disallow res dir or lib
        if (filePath.startsWith(projectPath + "/lib")) {
            ToastUtil.make(project, MessageType.ERROR, "lib目录下不允许存放本地依赖");
            return;
        }
        List<String> assetFoldNameList = PluginUtils.supportAssetFoldName(project);
        for (String foldName : assetFoldNameList) {
            if (filePath.startsWith(projectPath + "/" + foldName)) {
                ToastUtil.make(project, MessageType.ERROR, "资源目录下不允许存放本地依赖");
                return;
            }
        }

        File outputDirectory = new File(filePath);
        new ConvertDependencyToLocalTask(project, packageName,
                ((YAMLKeyValue) yamlPsiElement.getParent()), outputDirectory)
                .queue();
    }
}
