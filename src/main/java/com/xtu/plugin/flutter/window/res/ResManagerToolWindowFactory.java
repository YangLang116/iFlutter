package com.xtu.plugin.flutter.window.res;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import org.jetbrains.annotations.NotNull;

public class ResManagerToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return !PluginUtils.isNotFlutterProject(project);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory factory = contentManager.getFactory();
        Content content = factory.createContent(new ResManagerRootPanel(project), "", true);
        contentManager.addContent(content);
    }
}
