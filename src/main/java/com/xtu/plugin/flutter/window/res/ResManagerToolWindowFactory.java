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
import com.xtu.plugin.flutter.utils.PluginUtils;
import com.xtu.plugin.flutter.window.res.ui.ResManagerRootPanel;
import com.xtu.plugin.flutter.window.res.ui.SortType;
import org.jetbrains.annotations.NotNull;

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
        ResManagerRootPanel rootPanel = new ResManagerRootPanel(project, SortType.SORT_AZ);
        Content content = factory.createContent(rootPanel, "", true);
        contentManager.addContent(content);
        toolWindow.setTitleActions(List.of(new SortAction(SortType.SORT_AZ, rootPanel)));
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
