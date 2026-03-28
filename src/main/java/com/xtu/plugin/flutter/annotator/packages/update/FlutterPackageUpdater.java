package com.xtu.plugin.flutter.annotator.packages.update;

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
import com.intellij.util.concurrency.AppExecutorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FlutterPackageUpdater implements Disposable {

    private final Project project;
    private ScheduledFuture<?> scheduledTask;
    private volatile boolean isDetach = false;

    public FlutterPackageUpdater(@NotNull Project project) {
        this.project = project;
    }

    public static FlutterPackageUpdater getService(@NotNull Project project) {
        return project.getService(FlutterPackageUpdater.class);
    }

    public void attach() {
        LogUtils.info("FlutterPackageUpdater attach");
        this.isDetach = false;
        //定时拉取最新版本，间隔2小时
        if (!PluginUtils.isFlutterProject(project)) return;
        if (scheduledTask != null && !scheduledTask.isDone()) return;
        scheduledTask = AppExecutorUtil.getAppScheduledExecutorService()
                .scheduleWithFixedDelay(this::pullLatestVersion, 0, 2, TimeUnit.HOURS);
    }

    private void detach() {
        LogUtils.info("FlutterPackageUpdater detach");
        this.isDetach = true;
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
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
            LogUtils.error("FlutterPackageUpdater pullLatestVersion", e);
        }
    }

    private String parseVersion(@NotNull JSONObject packageJson, String name) {
        JSONObject versionNode = packageJson.optJSONObject(name);
        if (versionNode == null) return null;
        return versionNode.optString("version");
    }

    private void updatePackageInfo(List<PackageInfo> packageInfoList) {
        if (this.isDetach) return;
        ConcurrentHashMap<String, PackageInfo> newMap = new ConcurrentHashMap<>();
        for (PackageInfo packageInfo : packageInfoList) {
            newMap.put(packageInfo.name, packageInfo);
        }
        ProjectStorageService.getStorage(project).packageInfoMap = newMap;
    }


    @Override
    public void dispose() {
        detach();
    }
}
