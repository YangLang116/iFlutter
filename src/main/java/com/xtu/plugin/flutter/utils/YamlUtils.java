package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

public class YamlUtils {

    public static String getFileName() {
        return "pubspec.yaml";
    }

    private static String getYamlPath(Project project) {
        File pubSpecFile = new File(project.getBasePath(), getFileName());
        return pubSpecFile.getAbsolutePath();
    }

    public static void writeToYaml(Project project, Yaml yaml, Object data, List<String> assetList) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            Writer writer = null;
            try {
                File file = new File(getYamlPath(project));
                writer = new FileWriter(file);
                yaml.dump(data, writer);
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
                if (virtualFile == null) return;
                virtualFile.refresh(false, false, () -> {
                    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
                    if (statusBar != null) {
                        statusBar.setInfo(getFileName() + " Update Success");
                    }
                });
            } catch (Exception e) {
                LogUtils.error("YamlUtils writeToYaml: " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            } finally {
                CloseUtils.close(writer);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static void loadFromYaml(Project project, YamlReadListener listener) {
        ApplicationManager.getApplication().runReadAction(() -> {
            FileInputStream fileInputStream = null;
            try {
                DumperOptions dumperOptions = new DumperOptions();
                dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
                Yaml yaml = new Yaml(dumperOptions);
                String filePath = getYamlPath(project);
                fileInputStream = new FileInputStream(filePath);
                Map<String, Object> data = yaml.load(fileInputStream);
                if (!data.containsKey("flutter")) data.put("flutter", new HashMap<String, Object>());
                Map<String, Object> flutter = (Map<String, Object>) data.get("flutter");
                if (!flutter.containsKey("assets")) flutter.put("assets", new ArrayList<String>());
                List<String> assetList = (List<String>) flutter.get("assets");
                //对assetList去重，防止pubspec.yaml中同一资源配置了两次，并排序
                CollectionUtils.duplicateList(assetList);
                Collections.sort(assetList);
                listener.onRead(yaml, data, assetList);
            } catch (Exception e) {
                LogUtils.error("YamlUtils loadFromYaml: " + e.getMessage());
                ToastUtil.make(project, MessageType.ERROR, e.getMessage());
            } finally {
                CloseUtils.close(fileInputStream);
            }
        });
    }

    public interface YamlReadListener {

        void onRead(Yaml yaml, Object data, List<String> assetList);

    }
}
