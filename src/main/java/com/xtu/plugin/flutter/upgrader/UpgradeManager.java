package com.xtu.plugin.flutter.upgrader;

import com.google.gson.Gson;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.PluginManagerConfigurable;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.base.net.NetworkManager;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.VersionUtils;

import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class UpgradeManager {

    private static final String sGroupId = "com.xtu.plugin.flutter.upgrade";
    @SuppressWarnings("HttpUrlsUsage")
    private static final String sUrl = "http://iflutter.toolu.cn/iflutter-version.json";

    public static void checkPluginVersion(@NotNull Project project) {
        LogUtils.info("UpgradeManager checkPluginVersion");
        NetworkManager.getInstance().get(sUrl, new Callback() {
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
                    final String currentVersion = VersionUtils.getPluginVersion();
                    if (VersionUtils.compareVersion(currentVersion, serverVersion) > 0) { //有新版更新
                        pushNotification(project,
                                versionInfo.title, versionInfo.subtitle, versionInfo.content,
                                versionInfo.detailBtnText, versionInfo.detailUrl);
                    }
                }
            }
        });
    }

    private static void pushNotification(@NotNull Project project,
                                         String title, String subtitle, String content,
                                         String detailBtnText, String detailUrl) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup(sGroupId)
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(title)
                .setSubtitle(subtitle)
                .setListener(new NotificationListener.UrlOpeningListener(true))
                .setImportant(true)
                .addAction(createUpgradeAction(project))  //引导升级
                .addAction(createDetailAction(detailBtnText, detailUrl)) //查看升级信息
                .notify(project);
    }

    private static AnAction createUpgradeAction(@NotNull Project project) {
        return new NotificationAction("Upgrade") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
                propertiesComponent.setValue(PluginManagerConfigurable.SELECTION_TAB_KEY, 1, 0);
                ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginManagerConfigurable.class);
            }
        };
    }

    private static AnAction createDetailAction(String detailBtnText, String detailUrl) {
        return new NotificationAction(detailBtnText) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                BrowserUtil.open(detailUrl);
            }
        };
    }
}
