package com.xtu.plugin.flutter.action.anchor;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.xtu.plugin.flutter.base.action.YamlDependencyAction;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

public class RepositoryAnchorAction extends YamlDependencyAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Pair<String, YAMLKeyValue> packageInfo = getPackageInfo(e);
        if (packageInfo == null) return;
        String repository = getHosted(packageInfo.getSecond()) + "/packages/" + packageInfo.getFirst();
        BrowserUtil.open(repository);
    }

    private String getHosted(@NotNull YAMLKeyValue keyValue) {
        YAMLValue valuePsi = keyValue.getValue();
        if (valuePsi instanceof YAMLMapping mapping) {
            YAMLKeyValue hosted = mapping.getKeyValueByKey("hosted");
            if (hosted != null) return hosted.getValueText().trim();
        }
        return "https://pub.dev";
    }
}
