package com.xtu.plugin.flutter.action.intl.utils;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class IntlUtils {

    private static final Map<String, String> sIntl2TransCode = new HashMap<>();

    static {
        sIntl2TransCode.put("vi", "vie"); //越南语
        sIntl2TransCode.put("es", "spa");  //西班牙语
        sIntl2TransCode.put("ja", "jp"); //日语
        sIntl2TransCode.put("ko", "kor"); //韩语
        sIntl2TransCode.put("fr", "fra"); //法语
        sIntl2TransCode.put("ar", "ara"); //阿拉伯语
    }

    @Nullable
    public static String getLocaleByFileName(@NotNull String fileName) {
        //intl_en.arb
        int firstIndex = fileName.indexOf("_");
        if (firstIndex < 0) return null;
        int lastIndex = fileName.indexOf(".");
        if (lastIndex < 0) return null;
        return fileName.substring(firstIndex + 1, lastIndex);
    }

    @NotNull
    public static String getLocaleFileName(@NotNull String locale) {
        return String.format("intl_%s.arb", locale);
    }

    public static boolean isIntlDir(@NotNull VirtualFile virtualFile) {
        if (!virtualFile.isDirectory()) return false;
        return Arrays.stream(virtualFile.getChildren()).anyMatch((file) -> file.getName().endsWith(".arb"));
    }

    @Nullable
    public static VirtualFile getRootIntlDir(@NotNull Project project) {
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) return null;
        VirtualFile libDir = projectDir.findChild("lib");
        if (libDir == null) return null;
        return libDir.findChild("l10n");
    }

    @Nullable
    public static VirtualFile getLocalFile(@NotNull VirtualFile intlDir, @NotNull String locale) {
        String fileName = getLocaleFileName(locale);
        return intlDir.findChild(fileName);
    }

    @Nullable
    public static List<String> getLocaleList(@NotNull VirtualFile virtualDir) {
        VirtualFile[] children = virtualDir.getChildren();
        if (children == null || children.length == 0) return null;
        List<String> localeList = new ArrayList<>();
        for (VirtualFile child : children) {
            String locale = getLocaleByFileName(child.getName());
            if (StringUtils.isEmpty(locale)) continue;
            localeList.add(locale);
        }
        return localeList;
    }

    public static void addLocaleValue(@NotNull Project project,
                                      @NotNull VirtualFile intlDir,
                                      @NotNull String locale,
                                      @NotNull String key,
                                      @NotNull String value,
                                      boolean canReplaceKey) throws Throwable {
        ReadAction.run(() -> {
            VirtualFile localeFile = getLocalFile(intlDir, locale);
            if (localeFile == null) return;
            String content = new String(localeFile.contentsToByteArray(), StandardCharsets.UTF_8);
            JSONObject resultJson = new JSONObject(content);
            if (!canReplaceKey && resultJson.keySet().contains(key)) {
                throw new RuntimeException(String.format("%s: %s is existed", locale, key));
            }
            resultJson.put(key, value);
            byte[] resultBytes = resultJson.toString(2).getBytes();
            WriteCommandAction.runWriteCommandAction(project, (ThrowableComputable<Void, Throwable>) () -> {
                localeFile.setBinaryContent(resultBytes);
                return null;
            });
        });
    }

    public static void removeLocaleValue(@NotNull Project project,
                                         @NotNull VirtualFile intlDir,
                                         @NotNull String locale,
                                         @NotNull String key) throws Throwable {
        ReadAction.run(() -> {
            VirtualFile localeFile = getLocalFile(intlDir, locale);
            if (localeFile == null) return;
            String content = new String(localeFile.contentsToByteArray(), StandardCharsets.UTF_8);
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

    public static String transCodeFromLocale(@NotNull String locale) {
        int underLineIndex = locale.indexOf("_"); //兼容 en_GB 和 en
        if (underLineIndex >= 0) locale = locale.substring(0, underLineIndex);
        return sIntl2TransCode.getOrDefault(locale, locale);
    }
}
