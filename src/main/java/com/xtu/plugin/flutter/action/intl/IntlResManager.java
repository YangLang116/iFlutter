package com.xtu.plugin.flutter.action.intl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.base.utils.CommandUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.ToastUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class IntlResManager {

    public static void addLocaleAssetList(@NotNull Project project,
                                          @NotNull VirtualFile intlDir,
                                          @NotNull String key,
                                          @NotNull Map<String, String> localeMap,
                                          boolean canReplaceKey) {
        try {
            for (Map.Entry<String, String> entry : localeMap.entrySet()) {
                IntlUtils.addLocaleValue(project, intlDir, entry.getKey(), key, entry.getValue(), canReplaceKey);
            }
            generateIntlRes(project);
        } catch (Throwable e) {
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    public static void removeLocaleAssetList(@NotNull Project project,
                                             @NotNull VirtualFile intlDir,
                                             @Nullable List<String> localeList,
                                             @NotNull String key) {
        if (localeList == null || localeList.isEmpty()) return;
        try {
            for (String locale : localeList) {
                IntlUtils.removeLocaleValue(project, intlDir, locale, key);
            }
            generateIntlRes(project);
        } catch (Throwable e) {
            ToastUtils.make(project, MessageType.ERROR, e.getMessage());
        }
    }

    private static void generateIntlRes(@NotNull Project project) {
        if (PluginUtils.isPluginAvailable("com.localizely.flutter-intl")) return;
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(
                project, "--no-color pub global run intl_utils:generate", -1);
        if (commandResult.code == CommandUtils.CommandResult.SUCCESS) {
            ToastUtils.make(project, MessageType.INFO, "successfully added");
        } else {
            ToastUtils.make(project, MessageType.ERROR, commandResult.result);
        }
    }
}
