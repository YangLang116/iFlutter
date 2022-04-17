package com.xtu.plugin.flutter.action.intl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.xtu.plugin.flutter.action.intl.exception.IntlException;
import com.xtu.plugin.flutter.action.intl.utils.IntlUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class IntlResManager {

    public static void addLocaleAssetList(@NotNull Project project,
                                          @NotNull String key,
                                          @NotNull Map<String, String> localeMap) {
        for (Map.Entry<String, String> entry : localeMap.entrySet()) {
            String locale = entry.getKey();
            String content = entry.getValue();
            try {
                IntlUtils.addLocaleValue(project, locale, key, content);
            } catch (IntlException e) {
                String fileName = IntlUtils.getFileName(locale);
                ToastUtil.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
            } catch (Throwable e) {
                String fileName = IntlUtils.getFileName(locale);
                ToastUtil.make(project, MessageType.ERROR, fileName + ": 处理异常");
            }
        }
    }

    public static void removeLocaleAssetList(@NotNull Project project,
                                             @NotNull String key,
                                             @NotNull List<String> localeList) {
        for (String locale : localeList) {
            try {
                IntlUtils.removeLocaleValue(project, locale, key);
            } catch (IntlException e) {
                String fileName = IntlUtils.getFileName(locale);
                ToastUtil.make(project, MessageType.ERROR, fileName + ": " + e.getMessage());
            } catch (Throwable e) {
                String fileName = IntlUtils.getFileName(locale);
                ToastUtil.make(project, MessageType.ERROR, fileName + ": 处理异常");
            }
        }
    }
}
