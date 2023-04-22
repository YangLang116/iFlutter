package com.xtu.plugin.flutter.upgrader;

import com.google.gson.Gson;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.xtu.plugin.flutter.utils.VersionUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class UpgradeManager {

    private static final String sGroupId = "com.xtu.plugin.flutter.upgrade";
    private static final String sUrl = "https://iflutter.toolu.cn/iflutter-version.json";

    private UpgradeManager() {
    }

    private static final UpgradeManager sInstance = new UpgradeManager();

    public static UpgradeManager getInstance() {
        return sInstance;
    }

    public void checkPluginVersion(@NotNull Project project) {
        final String currentVersion = VersionUtils.getPluginVersion();
        final String url = sUrl + "?version=" + currentVersion + "&os=" + SystemInfo.getOsNameAndVersion();
        NetworkManager.getInstance().get(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //ignore
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) return;
                ResponseBody body = response.body();
                if (body == null) return;
                String responseContent = body.string();
                if (TextUtils.isEmpty(responseContent)) return;
                Gson gson = new Gson();
                VersionInfo versionInfo = gson.fromJson(responseContent, VersionInfo.class);
                if (versionInfo != null && !TextUtils.isEmpty(versionInfo.version)) {
                    String serverVersion = versionInfo.version;
                    if (VersionUtils.compareVersion(currentVersion, serverVersion) > 0) { //有新版更新
                        pushNotification(project,
                                versionInfo.title, versionInfo.subtitle, versionInfo.content,
                                versionInfo.detailBtnText, versionInfo.detailUrl);
                    }
                }
            }
        });
    }

    private void pushNotification(@NotNull Project project,
                                  String title, String subtitle, String content,
                                  String detailBtnText, String detailUrl) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(sGroupId)
                .createNotification(title, subtitle, content, NotificationType.INFORMATION,
                        new NotificationListener.UrlOpeningListener(true))
                .setImportant(true)
                .addAction(createUpgradeAction(project))  //引导升级
                .addAction(createDetailAction(detailBtnText, detailUrl)) //查看升级信息
                .notify(project);
    }

    private AnAction createUpgradeAction(@NotNull Project project) {
        return new NotificationAction("Upgrade") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
                propertiesComponent.setValue(PluginManagerConfigurable.SELECTION_TAB_KEY, 1, 0);
                ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginManagerConfigurable.class);
            }
        };
    }

    private AnAction createDetailAction(String detailBtnText, String detailUrl) {
        return new NotificationAction(detailBtnText) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                BrowserUtil.open(detailUrl);
            }
        };
    }
}
