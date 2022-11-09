package com.xtu.plugin.flutter.action.pub.speed.helper;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.xtu.plugin.flutter.action.pub.speed.entity.AndroidMavenInfo;
import com.xtu.plugin.flutter.action.pub.speed.entity.AndroidPluginInfo;
import com.xtu.plugin.flutter.utils.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.path.GrMethodCallExpression;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.literals.GrLiteralImpl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AndroidGradleParser {

    public static void start(@NotNull Project project) {
        ReadAction.run(() -> parseAndroidPluginList(project));
    }

    ///获取带android平台的flutter插件
    private static void parseAndroidPluginList(@NotNull Project project) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        File pluginFile = new File(projectPath, ".flutter-plugins");
        if (!pluginFile.exists()) {
            ToastUtil.make(project, MessageType.ERROR, "请先flutter pub get!");
            return;
        }
        try {
            List<AndroidPluginInfo> androidPluginList = new ArrayList<>();
            //添加插件项目
            List<String> pluginLines = FileUtils.readLines(pluginFile, StandardCharsets.UTF_8);
            for (String pluginLine : pluginLines) {
                String[] pluginMeta = pluginLine.split("=");
                if (pluginMeta.length != 2) continue;
                String pluginName = pluginMeta[0];
                String pluginRootPath = pluginMeta[1];
                File androidDirectory = new File(pluginRootPath, "android");
                if (!androidDirectory.exists()) continue;
                AndroidPluginInfo androidPlugin = new AndroidPluginInfo(pluginName, androidDirectory);
                androidPluginList.add(androidPlugin);
            }
            //添加根项目
            File rootAndroidDirectory = new File(projectPath, "android");
            if (rootAndroidDirectory.exists()) {
                AndroidPluginInfo rootAndroid = new AndroidPluginInfo("Root", rootAndroidDirectory);
                androidPluginList.add(rootAndroid);
            }
            parseGradleFile(project, androidPluginList);
            //修改flutter脚本
            parseFlutterGradleFile(project);
        } catch (Exception e) {
            LogUtils.error("PubSpeedHelper parseAndroidPluginList: " + e.getMessage());
            ToastUtil.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private static void parseFlutterGradleFile(Project project) {
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) {
            ToastUtil.make(project, MessageType.ERROR, "must set flutter.sdk in local.properties");
            return;
        }
        String relativePath = "packages/flutter_tools/gradle/flutter.gradle".replace("/", File.separator);
        File gradleFile = new File(flutterPath, relativePath);
        GroovyFile gradlePsiFile = getGroovyFile(project, gradleFile);
        if (gradlePsiFile == null) return;
        AndroidMavenInfo buildScriptMaven = parseBuildScriptMavenList(gradlePsiFile);
        AndroidMavenInfo rootProjectMaven = parseRootProjectMavenList(gradlePsiFile);
        if (buildScriptMaven == null && rootProjectMaven == null) return;
        AndroidGradleMaker.start(project, gradlePsiFile, buildScriptMaven, rootProjectMaven);
    }

    private static void parseGradleFile(@NotNull Project project, List<AndroidPluginInfo> androidPluginList) {
        for (AndroidPluginInfo pluginInfo : androidPluginList) {
            File gradleFile = new File(pluginInfo.androidDirectory, "build.gradle");
            GroovyFile gradlePsiFile = getGroovyFile(project, gradleFile);
            if (gradlePsiFile == null) continue;
            AndroidMavenInfo buildScriptMaven = parseBuildScriptMavenList(gradlePsiFile);
            AndroidMavenInfo rootProjectMaven = parseRootProjectMavenList(gradlePsiFile);
            if (buildScriptMaven == null && rootProjectMaven == null) return;
            AndroidGradleMaker.start(project, gradlePsiFile, buildScriptMaven, rootProjectMaven);
        }
    }

    @Nullable
    private static GroovyFile getGroovyFile(@NotNull Project project, @NotNull File gradleFile) {
        if (!gradleFile.exists()) return null;
        VirtualFile gradleVirtualFile = VfsUtil.findFileByIoFile(gradleFile, true);
        if (gradleVirtualFile == null) return null;
        return (GroovyFile) PsiManager.getInstance(project).findFile(gradleVirtualFile);
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Nullable
    private static AndroidMavenInfo parseRootProjectMavenList(@NotNull GroovyFile gradlePsiFile) {
        //rootProject.allprojects节点
        GrMethodCallExpression allProjectExpr = PsiUtils.findFirstChild(gradlePsiFile,
                GrMethodCallExpression.class,
                "rootProject.allprojects");
        if (allProjectExpr == null) return null;
        //rootProject.allprojects-repositories节点
        GrClosableBlock buildScriptBlock = PsiUtils.findFirstChildByType(allProjectExpr, GrClosableBlock.class);
        if (buildScriptBlock == null) return null;
        GrMethodCallExpression repositoriesExpr = PsiUtils.findFirstChild(buildScriptBlock,
                GrMethodCallExpression.class,
                "repositories");
        if (repositoriesExpr == null) return null;
        //解析repositories节点
        return parseRepositoriesPsi(repositoriesExpr);
    }

    @Nullable
    private static AndroidMavenInfo parseBuildScriptMavenList(@NotNull GroovyFile gradlePsiFile) {
        //buildScript节点
        GrMethodCallExpression buildScriptExpr = PsiUtils.findFirstChild(gradlePsiFile,
                GrMethodCallExpression.class,
                "buildscript");
        if (buildScriptExpr == null) return null;
        //buildScript-repositories节点
        GrClosableBlock buildScriptBlock = PsiUtils.findFirstChildByType(buildScriptExpr, GrClosableBlock.class);
        if (buildScriptBlock == null) return null;
        GrMethodCallExpression repositoriesExpr = PsiUtils.findFirstChild(buildScriptBlock,
                GrMethodCallExpression.class,
                "repositories");
        if (repositoriesExpr == null) return null;
        //解析repositories节点
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
