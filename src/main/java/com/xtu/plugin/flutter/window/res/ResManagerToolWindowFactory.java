package com.xtu.plugin.flutter.window.res;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.utils.TinyUtils;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import com.xtu.plugin.flutter.window.res.ui.SortType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResManagerToolWindowFactory implements ToolWindowFactory, DumbAware {


    @Override
    public boolean isApplicable(@NotNull Project project) {
        return !PluginUtils.isNotFlutterProject(project);
    }

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        ContentFactory factory = contentManager.getFactory();
        boolean showSearchBar = false;
        SortType defaultSortType = SortType.SORT_AZ;
        ResManagerRootPanel rootPanel = new ResManagerRootPanel(project, showSearchBar, defaultSortType);
        Content content = factory.createContent(rootPanel, "", true);
        contentManager.addContent(content);
        toolWindow.setTitleActions(Arrays.asList(
                new SearchAction(rootPanel),
                new CompressAction(rootPanel),
                new SortAction(defaultSortType, rootPanel)));
    }

    private static class CompressAction extends AnAction {

        private final ResManagerRootPanel rootPanel;

        CompressAction(@NotNull ResManagerRootPanel rootPanel) {
            super(AllIcons.Actions.MenuCut);
            this.rootPanel = rootPanel;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            if (project == null) return;
            List<File> resList = this.rootPanel.getResList();
            if (CollectionUtils.isEmpty(resList)) return;
            List<File> resultList = new ArrayList<>();
            for (File file : resList) {
                if (!TinyUtils.isSupport(file)) continue;
                resultList.add(file);
            }
            TinyUtils.compressImage(project, resultList, this.rootPanel::reloadResList);
        }
    }

    private static class SearchAction extends AnAction {

        private final ResManagerRootPanel rootPanel;

        SearchAction(@NotNull ResManagerRootPanel rootPanel) {
            super(AllIcons.Actions.Search);
            this.rootPanel = rootPanel;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            this.rootPanel.toggleTopBar();
        }
    }

    private static class SortAction extends AnAction {

        private SortType sortType;
        private final ResManagerRootPanel rootPanel;

        SortAction(@NotNull SortType sortType, @NotNull ResManagerRootPanel rootPanel) {
            super(sortType == SortType.SORT_AZ ? AllIcons.ObjectBrowser.Sorted : AllIcons.ObjectBrowser.SortByType);
            this.sortType = sortType;
            this.rootPanel = rootPanel;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            if (this.sortType == SortType.SORT_AZ) {
                this.rootPanel.sort(this.sortType = SortType.SORT_SIZE);
                presentation.setIcon(AllIcons.ObjectBrowser.SortByType);
            } else if (this.sortType == SortType.SORT_SIZE) {
                this.rootPanel.sort(this.sortType = SortType.SORT_AZ);
                presentation.setIcon(AllIcons.ObjectBrowser.Sorted);
            }
        }
    }
}
