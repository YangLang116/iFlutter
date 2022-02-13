package com.xtu.plugin.flutter.component.packages.update;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.xtu.plugin.flutter.service.StorageService;
import com.xtu.plugin.flutter.utils.CommandUtils;
import com.xtu.plugin.flutter.utils.DartUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class FlutterPackageUpdater {

    private final Project project;
    private final Timer latestVersionChecker;

    public FlutterPackageUpdater(@NotNull Project project) {
        this.project = project;
        this.latestVersionChecker = new Timer("Flutter Package Version Updater", true);
    }

    public void attach() {
        LogUtils.info("FlutterPackageUpdater attach");
        //定时拉取最新版本，间隔10分钟
        this.latestVersionChecker.schedule(new TimerTask() {
            @Override
            public void run() {
                pullLatestVersion();
            }
        }, 0, 5 * 60 * 1000);
    }

    public void detach() {
        LogUtils.info("FlutterPackageUpdater detach");
        this.latestVersionChecker.cancel();
    }

    private void pullLatestVersion() {
        LogUtils.info("FlutterPackageUpdater pullLatestVersion");
        String projectPath = PluginUtils.getProjectPath(project);
        if (StringUtils.isEmpty(projectPath)) return;
        String flutterPath = DartUtils.getFlutterPath(project);
        if (StringUtils.isEmpty(flutterPath)) return;
        String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
        File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
        String command = executorFile.getAbsolutePath() + " pub outdated --dependency-overrides --dev-dependencies --no-prereleases --json";
        CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, new File(projectPath), 2);
        if (commandResult.code == CommandUtils.CommandResult.FAIL) return;
        try {
            final JSONObject versionJson = new JSONObject(commandResult.result);
            JSONArray packageInfoList = versionJson.optJSONArray("packages");
            if (packageInfoList == null) return;
            List<PackageInfo> packageVersionList = new ArrayList<>();
            for (int i = 0; i < packageInfoList.length(); i++) {
                JSONObject packageInfo = (JSONObject) packageInfoList.get(i);
                String packageName = packageInfo.optString("package");
                String currentVersion = parseVersion(packageInfo, "current");
                String latestVersion = parseVersion(packageInfo, "latest");
                if (StringUtils.isEmpty(packageName)
                        || StringUtils.isEmpty(currentVersion)
                        || StringUtils.isEmpty(latestVersion)
                        || StringUtils.equals(currentVersion, latestVersion)) {
                    continue;
                }
                packageVersionList.add(new PackageInfo(packageName, currentVersion, latestVersion));
            }
            //save package version info
            ApplicationManager.getApplication()
                    .invokeLater(() -> updatePackageInfo(packageVersionList));
        } catch (Exception e) {
            //ignore
        }
    }

    private String parseVersion(@NotNull JSONObject packageJson, String name) {
        JSONObject versionNode = packageJson.optJSONObject(name);
        if (versionNode == null) return null;
        return versionNode.optString("version");
    }

    private void updatePackageInfo(List<PackageInfo> packageInfoList) {
        LogUtils.info("FlutterPackageUpdater updatePackageInfo: " + packageInfoList);
        Map<String, PackageInfo> infoMap = StorageService.getInstance(project).getState().packageInfoMap;
        infoMap.clear();
        for (PackageInfo packageInfo : packageInfoList) {
            infoMap.put(packageInfo.name, packageInfo);
        }
    }
}
