package com.xtu.plugin.flutter.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import javax.swing.JComponent;

/**
 * Created by YangLang on 2017/11/25.
 */
public class ToastUtils {

    private static void make(JComponent jComponent, MessageType type, String text) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(text, type, null)
                .setFadeoutTime(7500)
                .createBalloon()
                .show(RelativePoint.getCenterOf(jComponent), Balloon.Position.above);
    }


    public static void make(Project project, MessageType type, String text) {
        Runnable showRunnable = () -> {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            make(statusBar.getComponent(), type, text);
        };
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            showRunnable.run();
        } else {
            application.invokeLater(showRunnable, ModalityState.any());
        }
    }
}