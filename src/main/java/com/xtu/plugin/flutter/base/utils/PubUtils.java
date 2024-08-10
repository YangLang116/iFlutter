package com.xtu.plugin.flutter.base.utils;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PubUtils {

    @Nullable
    public static Map<String, String> getPluginPathMap(@NotNull Project project) {
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return null;
        File configFile = new File(projectPath, ".dart_tool/package_config.json".replace("/", File.separator));
        if (configFile.exists()) return getPluginPathMapFromJsonFile(configFile);
        File packageFile = new File(projectPath, ".packages");
        if (packageFile.exists()) return getPluginPathMapFromPackageFile(packageFile);
        File flutterPluginsFile = new File(projectPath, ".flutter-plugins");
        if (flutterPluginsFile.exists()) return getPluginPathMapFromPluginFile(flutterPluginsFile);
        return null;
    }

    @Nullable
    private static Map<String, String> getPluginPathMapFromJsonFile(File configJsonFile) {
        try {
            String fileContent = FileUtils.readFromFile(configJsonFile);
            if (StringUtils.isEmpty(fileContent)) return null;
            final Map<String, String> pluginPathMap = new HashMap<>();
            JSONObject jsonObject = new JSONObject(fileContent);
            JSONArray packageArray = jsonObject.getJSONArray("packages");
            for (int i = 0; i < packageArray.length(); i++) {
                JSONObject packageJson = (JSONObject) packageArray.get(i);
                String pluginName = packageJson.getString("name");
                String pluginUri = packageJson.getString("rootUri");
                if (!pluginUri.startsWith("file://")) continue;
                String pluginPath = pluginUri.replace("file://", "");
                pluginPathMap.put(pluginName, pluginPath);
            }
            return pluginPathMap;
        } catch (Exception e) {
            LogUtils.error("PubUtils getPluginPathMapFromJsonFile", e);
            return null;
        }
    }

    @Nullable
    private static Map<String, String> getPluginPathMapFromPackageFile(File packageConfigFile) {
        BufferedReader bufferedReader = null;
        try {
            final Map<String, String> pluginPathMap = new HashMap<>();
            FileInputStream fileInputStream = new FileInputStream(packageConfigFile);
            InputStreamReader streamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(streamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                int firstSemIndex = line.indexOf(":");
                String pluginName = line.substring(0, firstSemIndex);
                String pluginUri = line.substring(firstSemIndex + 1);
                if (!pluginUri.startsWith("file://")) continue;
                String pluginPath = pluginUri.replace("file://", "")
                        .replace("/lib/", "");
                pluginPathMap.put(pluginName, pluginPath);
            }
            return pluginPathMap;
        } catch (Exception e) {
            LogUtils.error("PubUtils getPluginPathMapFromPackageFile", e);
            return null;
        } finally {
            CloseUtils.close(bufferedReader);
        }
    }

    @Nullable
    private static Map<String, String> getPluginPathMapFromPluginFile(File flutterPluginsFile) {
        BufferedReader bufferedReader = null;
        try {
            final Map<String, String> pluginPathMap = new HashMap<>();
            FileInputStream fileInputStream = new FileInputStream(flutterPluginsFile);
            InputStreamReader streamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(streamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                String[] metaInfo = line.split("=");
                if (metaInfo.length != 2) continue;
                pluginPathMap.put(metaInfo[0], metaInfo[1]);
            }
            return pluginPathMap;
        } catch (Exception e) {
            LogUtils.error("PubUtils getPluginPathMapFromPluginFile", e);
            return null;
        } finally {
            CloseUtils.close(bufferedReader);
        }
    }
}
