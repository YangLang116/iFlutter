package com.xtu.plugin.flutter.action.intl;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.utils.CommandUtils;
import com.xtu.plugin.flutter.utils.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class IntlResManager {

    public static void addLocaleAssetList(@NotNull Project project,
                                          @NotNull String key,
                                          @NotNull Map<String, String> localeMap,
                                          boolean canReplaceKey) {
        boolean isSuccess = true;
        for (Map.Entry<String, String> entry : localeMap.entrySet()) {
            String locale = entry.getKey();
            String content = entry.getValue();
            try {
                IntlUtils.addLocaleValue(project, locale, key, content, canReplaceKey);
            } catch (Throwable e) {
                isSuccess = false;
                String fileName = IntlUtils.getFileName(locale);
                ToastUtils.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
            }
        }
        if (isSuccess) {
            generateIfNeed(project);
        }
    }

    public static void removeLocaleAssetList(@NotNull Project project,
                                             @NotNull String key,
                                             @NotNull List<String> localeList) {
        boolean isSuccess = true;
        for (String locale : localeList) {
            try {
                IntlUtils.removeLocaleValue(project, locale, key);
            } catch (Throwable e) {
                isSuccess = false;
                String fileName = IntlUtils.getFileName(locale);
                ToastUtils.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
            }
        }
        if (isSuccess) {
            generateIfNeed(project);
        }
    }

    private static boolean hasFlutterIntlPlugin() {
        PluginManager pluginManager = PluginManager.getInstance();
        PluginId id = PluginId.getId("com.localizely.flutter-intl");
        IdeaPluginDescriptor plugin = pluginManager.findEnabledPlugin(id);
        return plugin != null;
    }

    public static void generateIfNeed(@NotNull Project project) {
        if (hasFlutterIntlPlugin()) return;
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(
                project, "--no-color pub global run intl_utils:generate", -1);
        if (commandResult.code == CommandUtils.CommandResult.SUCCESS) {
            ToastUtils.make(project, MessageType.INFO, "successfully added");
        } else {
            ToastUtils.make(project, MessageType.ERROR, commandResult.result);
        }
    }
}
