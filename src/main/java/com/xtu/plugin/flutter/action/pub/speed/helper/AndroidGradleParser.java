package com.xtu.plugin.flutter.action.pub.speed.helper;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.psi.PsiElement;
import com.xtu.plugin.flutter.action.pub.speed.entity.AndroidMavenInfo;
import com.xtu.plugin.flutter.action.pub.speed.entity.AndroidPluginInfo;
import com.xtu.plugin.flutter.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.literals.GrLiteralImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AndroidGradleParser {

    public static void start(@NotNull Project project,
                             boolean injectProject,
                             boolean injectPlugin,
                             boolean injectFlutterGradle) {
        List<String> mavenMirrorRepo = AndroidRepoHelper.getRepoList(project);
        if (CollectionUtils.isEmpty(mavenMirrorRepo)) return;
        ReadAction.run(() -> {
            String projectPath = PluginUtils.getProjectPath(project);
            if (StringUtils.isEmpty(projectPath)) return;
            if (injectPlugin) {
                boolean result = parsePluginGradle(project, mavenMirrorRepo);
                if (!result) return;
            }
            if (injectProject) {
                parseProjectGradle(project, projectPath, mavenMirrorRepo);
            }
            if (injectFlutterGradle) {
                parseFlutterGradleFile(project, mavenMirrorRepo);
            }
            //完成通知
            ToastUtils.make(project, MessageType.INFO, "mirror repo inject success");
        });
    }

    //解析插件Gradle
    private static boolean parsePluginGradle(@NotNull Project project, @NotNull List<String> mavenMirrorRepo) {
        Map<String, String> pluginPathMap = PubUtils.getPluginPathMap(project);
        if (pluginPathMap == null) {
            ToastUtils.make(project, MessageType.ERROR, "please run `flutter pub get` first");
            return false;
        }
        final List<AndroidPluginInfo> pluginList = new ArrayList<>();
        for (Map.Entry<String, String> entry : pluginPathMap.entrySet()) {
            String pluginName = entry.getKey();
            String pluginRootPath = entry.getValue();
            File androidDirectory = new File(pluginRootPath, "android");
            if (!androidDirectory.exists() || !androidDirectory.isDirectory()) continue;
            AndroidPluginInfo androidPlugin = new AndroidPluginInfo(pluginName, androidDirectory);
            pluginList.add(androidPlugin);
        }
        parseBuildGradleFile(project, pluginList, mavenMirrorRepo);
        return true;
    }

    //解析主工程Gradle
    private static void parseProjectGradle(@NotNull Project project,
                                           @NotNull String projectPath,
                                           @NotNull List<String> mavenMirrorRepo) {
        final List<AndroidPluginInfo> projectPluginList = new ArrayList<>();
        File rootAndroidDirectory = new File(projectPath, "android");
        if (rootAndroidDirectory.exists() && rootAndroidDirectory.isDirectory()) {
            AndroidPluginInfo rootAndroid = new AndroidPluginInfo("Root", rootAndroidDirectory);
            projectPluginList.add(rootAndroid); //添加根项目
            File appDirectory = new File(rootAndroidDirectory, "app");  //添加App项目
            if (appDirectory.exists() && appDirectory.isDirectory()) {
                AndroidPluginInfo appAndroid = new AndroidPluginInfo("App", appDirectory);
                projectPluginList.add(appAndroid);
            }
        }
        parseBuildGradleFile(project, projectPluginList, mavenMirrorRepo);
    }

    private static void parseBuildGradleFile(@NotNull Project project,
                                             @NotNull List<AndroidPluginInfo> androidPluginList,
                                             @NotNull List<String> mavenMirrorRepo) {
        for (AndroidPluginInfo pluginInfo : androidPluginList) {
            File gradleFile = new File(pluginInfo.androidDirectory, "build.gradle");
            parseGradleFile(project, gradleFile, mavenMirrorRepo);
        }
    }

    private static void parseFlutterGradleFile(@NotNull Project project, @NotNull List<String> mavenMirrorRepo) {
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return;
        String relativePath = "packages/flutter_tools/gradle/flutter.gradle".replace("/", File.separator);
        File gradleFile = new File(flutterPath, relativePath);
        parseGradleFile(project, gradleFile, mavenMirrorRepo);
    }

    private static void parseGradleFile(@NotNull Project project, @NotNull File gradleFile, @NotNull List<String> mavenMirrorRepo) {
        GroovyFile gradlePsiFile = FileUtils.getGroovyFile(project, gradleFile);
        if (gradlePsiFile == null) return;
        List<AndroidMavenInfo> repositoryList = new ArrayList<>();
        CollectionUtils.addIfNotEmpty(repositoryList, findRepositoriesPsi(gradlePsiFile));
        CollectionUtils.addIfNotEmpty(repositoryList, findRepositoriesPsiFromMethod(gradlePsiFile, "buildscript"));
        CollectionUtils.addIfNotEmpty(repositoryList, parseRootProjectRepository(gradlePsiFile));
        if (repositoryList.isEmpty()) return;
        AndroidGradleMaker.start(project, gradlePsiFile, mavenMirrorRepo, repositoryList);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Nullable
    private static AndroidMavenInfo parseRootProjectRepository(@NotNull GroovyFile gradlePsiFile) {
        List<GrMethodCallExpression> allprojects = PsiUtils.findChildren(gradlePsiFile, GrMethodCallExpression.class, "allprojects");
        for (GrMethodCallExpression allprojectPsi : allprojects) {
            GrClosableBlock block = PsiUtils.findFirstChildByType(allprojectPsi, GrClosableBlock.class);
            if (block == null) continue;
            AndroidMavenInfo mavenInfo = findRepositoriesPsi(block);
            if (mavenInfo != null) return mavenInfo;
        }
        return findRepositoriesPsiFromMethod(gradlePsiFile, "rootProject.allprojects");
    }

    @Nullable
    private static AndroidMavenInfo findRepositoriesPsiFromMethod(@NotNull PsiElement outElement, @NotNull String methodName) {
        GrMethodCallExpression callExpr = PsiUtils.findFirstChild(outElement, GrMethodCallExpression.class, methodName);
        if (callExpr == null) return null;
        GrClosableBlock callBlock = PsiUtils.findFirstChildByType(callExpr, GrClosableBlock.class);
        if (callBlock == null) return null;
        return findRepositoriesPsi(callBlock);
    }

    @Nullable
    private static AndroidMavenInfo findRepositoriesPsi(@NotNull PsiElement outElement) {
        GrMethodCallExpression repositoriesExpr = PsiUtils.findFirstChild(outElement, GrMethodCallExpression.class, "repositories");
        if (repositoriesExpr == null) return null;
        return parseRepositoriesPsi(repositoriesExpr);
    }

    @Nullable
    private static AndroidMavenInfo parseRepositoriesPsi(@NotNull GrMethodCallExpression repositoriesExpr) {
        GrClosableBlock repositoriesBlock = PsiUtils.findFirstChildByType(repositoriesExpr, GrClosableBlock.class);
        if (repositoriesBlock == null) return null;
        List<GrMethodCallExpression> mavenMethodList = new ArrayList<>();
        for (PsiElement psiElement : repositoriesBlock.getChildren()) {
            if (!(psiElement instanceof GrMethodCallExpression)) continue;
            String psiText = psiElement.getText().trim();
            if (!psiText.startsWith("maven")) continue;
            //maven{ url: ''} 格式/mavenLocal
            mavenMethodList.add((GrMethodCallExpression) psiElement);
        }
        List<String> mavenUrlList = new ArrayList<>();
        for (GrMethodCallExpression mavenCallExpr : mavenMethodList) {
            GrClosableBlock block = PsiUtils.findFirstChildByType(mavenCallExpr, GrClosableBlock.class);
            if (block == null) continue;
            GrApplicationStatement maven = PsiUtils.findFirstChildByType(block, GrApplicationStatement.class);
            if (maven == null) continue;
            GrCommandArgumentList argumentList = PsiUtils.findFirstChildByType(maven, GrCommandArgumentList.class);
            if (argumentList == null) continue;
            PsiElement firstArgument = argumentList.getFirstChild();
            if (!(firstArgument instanceof GrLiteralImpl)) continue;
            Object value = ((GrLiteralImpl) firstArgument).getValue();
            if (value == null) continue;
            mavenUrlList.add(value.toString());
        }
        return new AndroidMavenInfo(mavenUrlList, repositoriesBlock);
    }
}
