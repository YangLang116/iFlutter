package com.xtu.plugin.flutter.action.pub.speed.helper;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.xtu.plugin.flutter.action.pub.speed.entity.AndroidMavenInfo;
import com.xtu.plugin.flutter.utils.ArrayUtils;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElementFactory;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;

import java.util.List;

public class AndroidGradleMaker {

    public static void start(@NotNull Project project,
                             @NotNull GroovyFile gradlePsiFile,
                             @NotNull List<String> mavenMirrorRepo,
                             @NotNull List<AndroidMavenInfo> mavenInfoList) {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(() ->
                WriteAction.run(() ->
                        WriteCommandAction.runWriteCommandAction(project,
                                () -> make(project, gradlePsiFile, mavenMirrorRepo, mavenInfoList))));
    }

    public static void make(@NotNull Project project,
                            @NotNull GroovyFile gradlePsiFile,
                            @NotNull List<String> mavenMirrorRepo,
                            @NotNull List<AndroidMavenInfo> mavenInfoList) {
        for (AndroidMavenInfo androidMavenInfo : mavenInfoList) {
            modifyRepositoryPsi(project, mavenMirrorRepo, androidMavenInfo);
        }
        //刷新文件
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(gradlePsiFile);
        if (document != null) {
            //sync psi - document
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            //sync psi - vfs
            FileDocumentManager.getInstance().saveDocument(document);
        }
    }

    private static void modifyRepositoryPsi(@NotNull Project project,
                                            @NotNull List<String> mavenMirrorRepo,
                                            @Nullable AndroidMavenInfo repoInfo) {
        if (repoInfo == null) return;
        List<String> addMavenList = ArrayUtils.sub(mavenMirrorRepo, repoInfo.mavenList);
        if (CollectionUtils.isEmpty(addMavenList)) return;
        //添加节点：maven { url 'https://maven.aliyun.com/repository/public' }
        PsiElement repositories = repoInfo.repositories;
        GroovyPsiElementFactory elementFactory = GroovyPsiElementFactory.getInstance(project);
        for (int i = addMavenList.size() - 1; i >= 0; i--) {
            String maven = addMavenList.get(i);
            GrExpression expression = elementFactory.createExpressionFromText("maven { url '" + maven + "' }");
            repositories.addAfter(expression, repositories.getFirstChild());
        }
    }
}
