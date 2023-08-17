package com.xtu.plugin.flutter.window.res;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.window.res.action.CompressAction;
import com.xtu.plugin.flutter.window.res.action.LocateAction;
import com.xtu.plugin.flutter.window.res.action.SearchAction;
import com.xtu.plugin.flutter.window.res.action.SortAction;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import com.xtu.plugin.flutter.window.res.ui.SortType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ResManagerToolWindowFactory implements ToolWindowFactory, DumbAware {


    @Override
    public boolean isApplicable(@NotNull Project project) {
        return PluginUtils.isFlutterProject(project);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory factory = contentManager.getFactory();
        boolean showSearchBar = false;
        SortType defaultSortType = SortType.SORT_TIME;
        ResManagerRootPanel rootPanel = new ResManagerRootPanel(project, showSearchBar, defaultSortType);
        Content content = factory.createContent(rootPanel, "", true);
        contentManager.addContent(content);
        toolWindow.setTitleActions(createActionList(defaultSortType, rootPanel));
    }

    @NotNull
    private static List<AnAction> createActionList(@NotNull SortType defaultSortType,
                                                   @NotNull ResManagerRootPanel rootPanel) {
        return Arrays.asList(
                new LocateAction(rootPanel),
                new SearchAction(rootPanel),
                new CompressAction(rootPanel),
                new SortAction(defaultSortType, rootPanel));
    }
}
