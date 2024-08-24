package com.xtu.plugin.flutter.action.remove;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class CommentRemoveAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        final Presentation presentation = e.getPresentation();
        presentation.setVisible(project != null && psiFile != null && isSupportFile(psiFile));
    }

    @Override
    @NotNull
    public ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isSupportFile(@NotNull PsiFile psiFile) {
        return psiFile instanceof DartFile || psiFile instanceof YAMLFile;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        assert project != null;
        DumbService dumbService = DumbService.getInstance(project);
        dumbService.smartInvokeLater(() -> {
            final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            if (psiFile == null) return;
            Collection<PsiComment> psiComments = PsiTreeUtil.findChildrenOfType(psiFile, PsiComment.class);
            if (psiComments.isEmpty()) return;
            removeAllComments(project, psiFile, psiComments);
        });
    }

    private void removeAllComments(@NotNull Project project,
                                   @NotNull PsiFile psiFile,
                                   @NotNull Collection<PsiComment> psiComments) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (PsiComment psiComment : psiComments) {
                psiComment.delete();
            }
            if (psiFile instanceof YAMLFile) {
                YAMLDocument document = ((YAMLFile) psiFile).getDocuments().get(0);
                YAMLValue rootElement = document.getTopLevelValue();
                assert rootElement != null;
                removeEmptyLines(project, rootElement);
            }
            save(project, psiFile);
        });
    }

    private void removeEmptyLines(@NotNull Project project, @NotNull PsiElement rootElement) {
        String originContent = rootElement.getText();
        String newContent = Arrays.stream(originContent.split("\n"))
                .filter(line -> !line.trim().isEmpty())
                .collect(Collectors.joining("\n"));
        YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
        YAMLFile yamlFile = elementGenerator.createDummyYamlWithText(newContent);
        YAMLMapping newRootElement = PsiTreeUtil.findChildOfType(yamlFile, YAMLMapping.class);
        if (newRootElement == null) return;
        rootElement.replace(newRootElement);
    }

    private void save(@NotNull Project project, @NotNull PsiFile psiFile) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiFile);
        if (document != null) {
            //sync psi - document
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            //sync psi - vfs
            FileDocumentManager.getInstance().saveDocument(document);
        }
    }
}
