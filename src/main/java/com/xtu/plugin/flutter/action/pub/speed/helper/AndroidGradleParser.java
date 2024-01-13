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

    public static void start(@NotNull Project project) {
        ReadAction.run(() -> parseAndroidPluginList(project));
    }

    ///获取带android平台的flutter插件
    private static void parseAndroidPluginList(@NotNull Project project) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        List<AndroidPluginInfo> androidPluginList = new ArrayList<>();
        File rootAndroidDirectory = new File(projectPath, "android");
        if (rootAndroidDirectory.exists() && rootAndroidDirectory.isDirectory()) {
            //添加根项目
            AndroidPluginInfo rootAndroid = new AndroidPluginInfo("Root", rootAndroidDirectory);
            androidPluginList.add(rootAndroid);
            //添加App项目
            File appDirectory = new File(rootAndroidDirectory, "app");
            if (appDirectory.exists() && appDirectory.isDirectory()) {
                AndroidPluginInfo appAndroid = new AndroidPluginInfo("App", appDirectory);
                androidPluginList.add(appAndroid);
            }
        }
        //添加Plugin项目
        Map<String, String> pluginPathMap = PubUtils.getPluginPathMap(project);
        if (pluginPathMap == null) {
            ToastUtils.make(project, MessageType.ERROR, "please run `flutter pub get` first");
            return;
        }
        for (Map.Entry<String, String> entry : pluginPathMap.entrySet()) {
            String pluginName = entry.getKey();
            String pluginRootPath = entry.getValue();
            File androidDirectory = new File(pluginRootPath, "android");
            if (!androidDirectory.exists() || !androidDirectory.isDirectory()) continue;
            AndroidPluginInfo androidPlugin = new AndroidPluginInfo(pluginName, androidDirectory);
            androidPluginList.add(androidPlugin);
        }
        //修改build.gradle文件
        parseBuildGradleFile(project, androidPluginList);
        //修改flutter.gradle文件
        parseFlutterGradleFile(project);
        //完成通知
        ToastUtils.make(project, MessageType.INFO, "mirror repo inject success");
    }


    private static void parseBuildGradleFile(@NotNull Project project, List<AndroidPluginInfo> androidPluginList) {
        for (AndroidPluginInfo pluginInfo : androidPluginList) {
            File gradleFile = new File(pluginInfo.androidDirectory, "build.gradle");
            parseGradleFile(project, gradleFile);
        }
    }

    private static void parseFlutterGradleFile(@NotNull Project project) {
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return;
        String relativePath = "packages/flutter_tools/gradle/flutter.gradle".replace("/", File.separator);
        File gradleFile = new File(flutterPath, relativePath);
        parseGradleFile(project, gradleFile);
    }

    private static void parseGradleFile(@NotNull Project project, @NotNull File gradleFile) {
        GroovyFile gradlePsiFile = FileUtils.getGroovyFile(project, gradleFile);
        if (gradlePsiFile == null) return;
        AndroidMavenInfo projectRepository = findRepositoriesPsi(gradlePsiFile);
        AndroidMavenInfo buildScriptRepository = findRepositoriesPsiFromMethod(gradlePsiFile, "buildscript");
        AndroidMavenInfo rootProjectRepository = parseRootProjectRepository(gradlePsiFile);
        if (projectRepository == null && buildScriptRepository == null && rootProjectRepository == null) return;
        AndroidGradleMaker.start(project, gradlePsiFile,
                buildScriptRepository, rootProjectRepository, projectRepository);
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
