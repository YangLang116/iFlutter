package com.xtu.plugin.flutter.action.convert.task;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.xtu.plugin.flutter.annotator.packages.update.PackageInfo;
import com.xtu.plugin.flutter.base.utils.*;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import io.flutter.pub.PubRoot;
import io.flutter.sdk.FlutterSdk;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLElementGenerator;
import org.jetbrains.yaml.psi.YAMLFile;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.util.Map;

public class ConvertDependencyToLocalTask extends Task.Backgroundable {

    private final String packageName;
    private final File outputDirectory;
    private final YAMLKeyValue dependencyElement;

    public ConvertDependencyToLocalTask(@NotNull Project project,
                                        @NotNull String packageName,
                                        @NotNull YAMLKeyValue dependencyElement,
                                        @NotNull File outputDirectory) {
        super(project, "Convert to local dependency");
        this.packageName = packageName;
        this.dependencyElement = dependencyElement;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        execute();
        indicator.setIndeterminate(false);
        indicator.setFraction(1);
    }

    private void execute() {
        Project project = getProject();
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        Map<String, String> pluginPathMap = PubUtils.getPluginPathMap(project);
        if (pluginPathMap == null) {
            ToastUtils.make(project, MessageType.ERROR, "please run `flutter pub get` first");
            return;
        }
        String packageRootPath = pluginPathMap.get(packageName);
        if (StringUtils.isEmpty(packageRootPath)) {
            ToastUtils.make(project, MessageType.ERROR, "failed to convert dependencies to local, try to run `flutter pub get` first");
            return;
        }
        File packageCacheRootDirectory = new File(packageRootPath);
        if (!packageCacheRootDirectory.exists()) {
            ToastUtils.make(project, MessageType.ERROR, "failed to obtain local dependency cache, try to run `flutter pub get` first");
            return;
        }
        try {
            FileUtils.copyDirectoryToDirectory(packageCacheRootDirectory, outputDirectory);
            File localDependencyFile = new File(outputDirectory, packageCacheRootDirectory.getName());
            //修改pubspec.yaml psiTree
            ApplicationManager.getApplication().invokeLater(() ->
                    WriteCommandAction.runWriteCommandAction(project,
                            () -> modifyDependency(project, projectPath, packageName, dependencyElement, localDependencyFile))
            );
        } catch (Exception e) {
            LogUtils.error("ConvertDependencyToLocalTask execute", e);
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    /**
     * 修改psi结构，将远程依赖修改成本地依赖
     *
     * @param project              项目对象
     * @param projectPath          项目根目录
     * @param packageName          当前依赖名
     * @param oldDependencyElement psi关键节点位置
     * @param localDependencyFile  本地依赖位置
     */
    private static void modifyDependency(Project project, String projectPath, String packageName, YAMLKeyValue oldDependencyElement, File localDependencyFile) {
        try {
            //移除提示更新
            removeAnnotator(project, packageName);
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(localDependencyFile);
            String relativePath = com.xtu.plugin.flutter.base.utils.FileUtils.getRelativePath(projectPath, localDependencyFile);
            //创建新的psi，并替换老的依赖方式
            //flutter_custom_calendar:
            //    path: dependencies/flutter_custom_calendar
            YAMLElementGenerator elementGenerator = YAMLElementGenerator.getInstance(project);
            YAMLKeyValue newDependencyElement = elementGenerator.createYamlKeyValue(packageName, "path: " + relativePath);
            PsiElement replaceElement = oldDependencyElement.replace(newDependencyElement);

            PsiFile psiFile = PsiTreeUtil.getParentOfType(replaceElement, YAMLFile.class);
            assert psiFile != null;
            //保存psi修改
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            Document document = psiDocumentManager.getDocument(psiFile);
            if (document != null) psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            //格式化
            CodeStyleManager.getInstance(project).reformat(psiFile);
            //保存文档
            psiDocumentManager.commitAllDocuments();
            FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
            fileDocumentManager.saveAllDocuments();

            runPubGet(packageName, project);
        } catch (Exception e) {
            LogUtils.error("ConvertDependencyToLocalTask modifyDependency", e);
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    //执行pub get
    private static void runPubGet(String packageName, Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            FlutterSdk sdk = FlutterSdk.getFlutterSdk(project);
            PubRoot root = PubRoot.forDirectory(ProjectUtil.guessProjectDir(project));
            if (sdk != null && root != null) {
                sdk.startPubGet(root, project);
            }
            ToastUtils.make(project, MessageType.INFO, packageName + " convert success");
        });
    }

    private static void removeAnnotator(Project project, String packageName) {
        Map<String, PackageInfo> infoMap = ProjectStorageService.getStorage(project).packageInfoMap;
        infoMap.remove(packageName);
    }
}
