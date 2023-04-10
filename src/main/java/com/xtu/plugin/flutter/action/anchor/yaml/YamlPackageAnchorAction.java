package com.xtu.plugin.flutter.action.anchor.yaml;

import com.intellij.ide.SelectInManager;
import com.intellij.ide.SelectInTarget;
import com.intellij.ide.SmartSelectInContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.xtu.plugin.flutter.action.BaseDependencyAction;
import com.xtu.plugin.flutter.utils.PubspecUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import kotlin.Pair;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class YamlPackageAnchorAction extends BaseDependencyAction {

    private static final String LIBRARY_DART = "Dart Packages";
    private static final String LIBRARY_FLUTTER = "Flutter Plugins";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;
        Pair<String, YAMLKeyValue> packageInfo = getPackageInfo(e);
        if (packageInfo == null) return;
        String packageName = packageInfo.getFirst();
        LibraryTable libraryTable = LibraryTablesRegistrar.getInstance().getLibraryTable(project);
        Library[] libraries = libraryTable.getLibraries();
        //从Flutter Plugins、Dart Packages中搜索Package路径
        searchPackageFromLibraries(project, libraries, packageName);
    }

    private static PsiDirectory url2PsiFile(@NotNull Project project, @NotNull String libraryUrl) {
        URL url = null;
        try {
            url = new URL(libraryUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        if (url == null) return null;
        File file = new File(url.getFile());
        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        VirtualFile virtualFile = localFileSystem.refreshAndFindFileByIoFile(file);
        if (virtualFile == null) return null;
        return PsiManager.getInstance(project).findDirectory(virtualFile);
    }

    //根据名字搜索指定库
    @Nullable
    private static PsiDirectory searchFileByName(@NotNull Project project,
                                                 @NotNull Library[] libraries,
                                                 @NotNull String libraryName,
                                                 @NotNull String packageName) {
        for (Library library : libraries) {
            if (StringUtils.equals(library.getName(), libraryName)) {
                String[] libraryUrls = library.getUrls(OrderRootType.CLASSES);
                for (String libraryUrl : libraryUrls) {
                    if (libraryUrl.contains("/" + packageName + "-")) {
                        return url2PsiFile(project, libraryUrl);
                    }
                }
                break;
            }
        }
        return null;
    }

    private static void searchPackageFromLibraries(@NotNull Project project,
                                                   @NotNull Library[] libraries,
                                                   @NotNull String packageName) {
        PsiDirectory flutterPsiDirectory = searchFileByName(project, libraries, LIBRARY_FLUTTER, packageName);
        if (flutterPsiDirectory != null) {
            anchorPsiFile(project, flutterPsiDirectory);
            return;
        }
        PsiDirectory dartPsiDirectory = searchFileByName(project, libraries, LIBRARY_DART, packageName);
        if (dartPsiDirectory != null) {
            anchorPsiFile(project, dartPsiDirectory);
            return;
        }
        ToastUtil.make(project, MessageType.ERROR, "anchor positioning failed");
    }

    @Nullable
    private static PsiFile getSpecificFile(@NotNull PsiDirectory psiDirectory) {
        PsiFile psiFile = psiDirectory.findFile(PubspecUtils.getFileName());
        if (psiFile != null) return psiFile;
        return PsiTreeUtil.getChildOfType(psiDirectory, PsiFile.class);
    }

    private static void anchorPsiFile(@NotNull Project project, @NotNull PsiDirectory psiDirectory) {
        SelectInTarget selectInTarget = SelectInManager.findSelectInTarget(ToolWindowId.PROJECT_VIEW, project);
        if (selectInTarget == null) return;
        PsiFile psiFile = getSpecificFile(psiDirectory);
        if (psiFile == null) return;
        SmartSelectInContext selectInContext = new SmartSelectInContext(psiFile, psiFile);
        selectInTarget.selectIn(selectInContext, true);
    }
}
