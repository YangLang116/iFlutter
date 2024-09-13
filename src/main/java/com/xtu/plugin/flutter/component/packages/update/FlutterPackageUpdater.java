package com.xtu.plugin.flutter.component.packages.update;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.utils.CommandUtils;
import com.xtu.plugin.flutter.base.utils.LogUtils;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import com.xtu.plugin.flutter.store.project.ProjectStorageService;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlutterPackageUpdater implements Disposable {

    private final Project project;
    private final ScheduledExecutorService latestVersionChecker;
    private volatile boolean isDetach = false;

    public FlutterPackageUpdater(@NotNull Project project) {
        this.project = project;
        this.latestVersionChecker = Executors.newScheduledThreadPool(1);
    }

    public static FlutterPackageUpdater getService(@NotNull Project project) {
        return project.getService(FlutterPackageUpdater.class);
    }

    public void attach() {
        LogUtils.info("FlutterPackageUpdater attach");
        this.isDetach = false;
        //定时拉取最新版本，间隔5分钟
        if (!PluginUtils.isFlutterProject(project)) return;
        this.latestVersionChecker.scheduleWithFixedDelay(this::pullLatestVersion, 0, 5, TimeUnit.MINUTES);
    }

    private void detach() {
        LogUtils.info("FlutterPackageUpdater detach");
        this.isDetach = true;
        if (!PluginUtils.isFlutterProject(project)) return;
        try {
            this.latestVersionChecker.shutdown();
        } catch (Exception e) {
            LogUtils.error("FlutterPackageUpdater detach", e);
        }
    }

    private void pullLatestVersion() {
        try {
            if (this.isDetach) return;
            CommandUtils.CommandResult commandResult = CommandUtils.executeSync(
                    project,
                    "pub outdated --dependency-overrides --dev-dependencies --no-prereleases --json", 5);
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
        if (this.isDetach) return;
        Map<String, PackageInfo> infoMap = ProjectStorageService.getStorage(project).packageInfoMap;
        infoMap.clear();
        for (PackageInfo packageInfo : packageInfoList) {
            infoMap.put(packageInfo.name, packageInfo);
        }
    }


    @Override
    public void dispose() {
        detach();
    }
}
