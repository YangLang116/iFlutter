package com.xtu.plugin.flutter.action.intl.utils;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.intl.exception.IntlException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IntlUtils {

    @Nullable
    public static String getLocale(@NotNull String fileName) {
        //intl_en.arb
        int firstIndex = fileName.indexOf("_");
        if (firstIndex < 0) return null;
        int lastIndex = fileName.indexOf(".");
        if (lastIndex < 0) return null;
        return fileName.substring(firstIndex + 1, lastIndex);
    }

    @NotNull
    public static String getFileName(String locale) {
        return String.format(Locale.ROOT, "intl_%s.arb", locale);
    }

    @Nullable
    public static VirtualFile getIntlDir(@Nullable Project project) {
        if (project == null) return null;
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return null;
        VirtualFile libDir = projectDir.findChild("lib");
        if (libDir == null) return null;
        return libDir.findChild("l10n");
    }

    @Nullable
    private static VirtualFile getLocalFile(@Nullable Project project, @NotNull String locale) {
        VirtualFile intlDir = getIntlDir(project);
        if (intlDir == null) return null;
        String fileName = getFileName(locale);
        return intlDir.findChild(fileName);
    }

    @Nullable
    public static List<String> getLocaleList(@Nullable Project project) {
        VirtualFile intlDir = getIntlDir(project);
        if (intlDir == null) return null;
        VirtualFile[] children = intlDir.getChildren();
        if (children == null || children.length <= 0) return null;
        List<String> localeList = new ArrayList<>();
        for (VirtualFile child : children) {
            String locale = getLocale(child.getName());
            if (StringUtils.isEmpty(locale)) continue;
            localeList.add(locale);
        }
        return localeList;
    }


    public static void addLocaleValue(@Nullable Project project,
                                      @NotNull String locale,
                                      @NotNull String key,
                                      @NotNull String value) throws Throwable {
        ReadAction.run(() -> {
            VirtualFile localeFile = getLocalFile(project, locale);
            if (localeFile == null) return;
            String content = new String(localeFile.contentsToByteArray());
            JSONObject resultJson = new JSONObject(content);
            if (resultJson.keySet().contains(key)) {
                throw new IntlException(key + "已经存在");
            }
            resultJson.put(key, value);
            byte[] resultBytes = resultJson.toString(2).getBytes();
            WriteCommandAction.runWriteCommandAction(project, (ThrowableComputable<Void, Throwable>) () -> {
                localeFile.setBinaryContent(resultBytes);
                return null;
            });
        });
    }

    public static void removeLocaleValue(@Nullable Project project,
                                         @NotNull String locale,
                                         @NotNull String key) throws Throwable {
        ReadAction.run(() -> {
            VirtualFile localeFile = getLocalFile(project, locale);
            if (localeFile == null) return;
            String content = new String(localeFile.contentsToByteArray());
            JSONObject resultJson = new JSONObject(content);
            if (!resultJson.keySet().contains(key)) return;
            resultJson.remove(key);
            byte[] resultBytes = resultJson.toString(2).getBytes();
            WriteCommandAction.runWriteCommandAction(project, (ThrowableComputable<Void, Throwable>) () -> {
                localeFile.setBinaryContent(resultBytes);
                return null;
            });
        });
    }
}
