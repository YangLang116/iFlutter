package com.xtu.plugin.flutter.action.intl;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.SystemInfo;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.utils.CommandUtils;
import com.xtu.plugin.flutter.utils.DartUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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
                ToastUtil.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
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
                ToastUtil.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
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
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return;
        String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
        File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
        String command = executorFile.getAbsolutePath() + "  --no-color pub global run intl_utils:generate";
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, new File(projectPath), -1);
        if (commandResult.code == CommandUtils.CommandResult.SUCCESS) {
            ToastUtil.make(project, MessageType.INFO, "successfully added");
        } else {
            ToastUtil.make(project, MessageType.ERROR, commandResult.result);
        }
    }
}
