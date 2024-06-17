package com.xtu.plugin.flutter.utils;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;

import org.jetbrains.annotations.NotNull;

public class VersionUtils {

    public static String getPluginVersion() {
        PluginId pluginId = PluginId.getId("com.xtu.plugins.flutter");
        IdeaPluginDescriptor pluginDescriptor = PluginManagerCore.getPlugin(pluginId);
        if (pluginDescriptor != null) return pluginDescriptor.getVersion();
        return "0.0.0";
    }

    public static int compareVersion(@NotNull String first, @NotNull String second) {
        String[] firstSplit = first.split("\\.");
        String[] secondSplit = second.split("\\.");
        for (int i = 0, j = Math.min(secondSplit.length, firstSplit.length); i < j; i++) {
            int firstNum = Integer.parseInt(firstSplit[i]);
            int secondNum = Integer.parseInt(secondSplit[i]);
            if (secondNum == firstNum) continue;
            return secondNum - firstNum;
        }
        return second.length() - first.length();
    }
}
