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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlutterPackageUpdater {

    private final Project project;
    private final ScheduledExecutorService latestVersionChecker;

    public FlutterPackageUpdater(@NotNull Project project) {
        this.project = project;
        this.latestVersionChecker = Executors.newScheduledThreadPool(1);
    }

    public void attach() {
        LogUtils.info("FlutterPackageUpdater attach");
        //定时拉取最新版本，间隔5分钟
        this.latestVersionChecker.scheduleWithFixedDelay(this::pullLatestVersion, 0, 5, TimeUnit.MINUTES);
    }

    public void detach() {
        LogUtils.info("FlutterPackageUpdater detach");
        try {
            this.latestVersionChecker.shutdownNow();
        } catch (Exception e) {
            LogUtils.error("FlutterPackageUpdater detach: " + e.getMessage());
        }
    }

    private void pullLatestVersion() {
        try {
            if (this.latestVersionChecker.isShutdown()) return;
            LogUtils.info("FlutterPackageUpdater pullLatestVersion");
            String projectPath = PluginUtils.getProjectPath(project);
            if (StringUtils.isEmpty(projectPath)) return;
            String flutterPath = DartUtils.getFlutterPath(project);
            if (StringUtils.isEmpty(flutterPath)) return;
            String executorName = SystemInfo.isWindows ? "flutter.bat" : "flutter";
            File executorFile = new File(flutterPath, "bin" + File.separator + executorName);
            String command = executorFile.getAbsolutePath() + " pub outdated --dependency-overrides --dev-dependencies --no-prereleases --json";
            CommandUtils.CommandResult commandResult = CommandUtils.executeSync(command, new File(projectPath), 5);
            LogUtils.info("FlutterPackageUpdater pullLatestVersion: " + commandResult.result);
            if (commandResult.code == CommandUtils.CommandResult.FAIL) return;

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
                        || StringUtils.isEmpty(latestVersion)) {
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
        if (this.latestVersionChecker.isShutdown()) return;
        LogUtils.info("FlutterPackageUpdater updatePackageInfo: " + packageInfoList);
        Map<String, PackageInfo> infoMap = StorageService.getInstance(project).getState().packageInfoMap;
        infoMap.clear();
        for (PackageInfo packageInfo : packageInfoList) {
            infoMap.put(packageInfo.name, packageInfo);
        }
    }
}
