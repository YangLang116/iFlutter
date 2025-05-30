package com.xtu.plugin.flutter.window.res;

import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.messages.MessageBus;
import com.xtu.plugin.flutter.base.utils.PluginUtils;
import com.xtu.plugin.flutter.window.res.action.CompressAction;
import com.xtu.plugin.flutter.window.res.action.LocateAction;
import com.xtu.plugin.flutter.window.res.action.SearchAction;
import com.xtu.plugin.flutter.window.res.action.SortAction;
import com.xtu.plugin.flutter.window.res.core.IResRootPanel;
import com.xtu.plugin.flutter.window.res.core.ResToolWindowRefresher;
import com.xtu.plugin.flutter.window.res.sort.SortType;
import com.xtu.plugin.flutter.window.res.ui.ResRootPanel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ResToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String WindowID = "Flutter Resource";

    @Override
    public boolean isApplicable(@NotNull Project project) {
        return PluginUtils.isFlutterProject(project);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ResRootPanel rootPanel = initResWindow(project, toolWindow);
        initThemeChangedListener(rootPanel);
        initResWindowRefresher(project, rootPanel);
    }

    @NotNull
    private ResRootPanel initResWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory factory = contentManager.getFactory();
        SortType defaultSortType = SortType.SORT_TIME;
        ResRootPanel rootPanel = new ResRootPanel(project, false, defaultSortType);
        Content content = factory.createContent(rootPanel, "", true);
        contentManager.addContent(content);
        toolWindow.setAutoHide(false);
        toolWindow.setTitleActions(createActionList(defaultSortType, rootPanel));
        return rootPanel;
    }

    private void initThemeChangedListener(@NotNull IResRootPanel rootPanel) {
        MessageBus messageBus = ApplicationManager.getApplication().getMessageBus();
        messageBus.connect().subscribe(LafManagerListener.TOPIC, (LafManagerListener) lafManager -> rootPanel.lookAndFeelChanged(lafManager));
    }

    private void initResWindowRefresher(@NotNull Project project, @NotNull IResRootPanel rootPanel) {
        ResToolWindowRefresher refresher = new ResToolWindowRefresher(project, rootPanel);
        refresher.init();
    }

    @NotNull
    private static List<AnAction> createActionList(@NotNull SortType defaultSortType,
                                                   @NotNull IResRootPanel rootPanel) {
        return Arrays.asList(
                new LocateAction(rootPanel),
                new CompressAction(rootPanel),
                new SortAction(defaultSortType, rootPanel),
                new SearchAction(rootPanel));
    }
}
