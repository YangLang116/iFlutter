package com.xtu.plugin.flutter.reporter;

import com.google.gson.Gson;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.NlsActions;
import com.intellij.util.Consumer;
import com.xtu.plugin.flutter.upgrader.NetworkManager;
import com.xtu.plugin.flutter.utils.CloseUtils;
import com.xtu.plugin.flutter.utils.LogUtils;
import com.xtu.plugin.flutter.utils.ToastUtil;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FErrorReporter extends ErrorReportSubmitter {

    private static final String sUrl = "https://api.github.com/repos/YangLang116/iFlutter/issues";
    private static final String sActionText = "Report to Author";

    private final Gson gson = new Gson();

    @Override
    @NotNull
    @NlsActions.ActionText
    public String getReportActionText() {
        return sActionText;
    }

    @Override
    public boolean submit(@NotNull IdeaLoggingEvent[] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        final DataManager mgr = DataManager.getInstance();
        final DataContext context = mgr.getDataContext(parentComponent);
        final Project project = CommonDataKeys.PROJECT.getData(context);
        new Task.Backgroundable(project, "Sending Error Report") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                StringBuilder errorStackInfoBuilder = new StringBuilder();
                for (IdeaLoggingEvent event : events) {
                    errorStackInfoBuilder.append(event.getThrowableText()).append("\n");
                }
                boolean isSuccess = postErrorMsg(additionalInfo, errorStackInfoBuilder.toString());
                ApplicationManager.getApplication().invokeLater(() -> {
                    ToastUtil.make(project, MessageType.INFO, "thank you for submitting your report!");
                    SubmittedReportInfo.SubmissionStatus status =
                            isSuccess ? SubmittedReportInfo.SubmissionStatus.NEW_ISSUE
                                    : SubmittedReportInfo.SubmissionStatus.FAILED;
                    consumer.consume(new SubmittedReportInfo(status));
                });
            }
        }.queue();
        return true;
    }

    private boolean postErrorMsg(String additionalInfo, String errorInfo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/vnd.github+json");
        headers.put("Authorization", "token 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");
        String title = "iFlutter issue(By Plugin)";
        String info = "";
        if (!StringUtils.isEmpty(additionalInfo)) {
            info += ("Description: \n" + additionalInfo + "\n\n");
        }
        info += ("StackTrace: \n" + errorInfo);
        FErrorInfo fErrorInfo = new FErrorInfo(title, info);
        String requestBody = gson.toJson(fErrorInfo);
        NetworkManager networkManager = NetworkManager.getInstance();
        Response response = null;
        try {
            response = networkManager.postAsync(sUrl, headers, requestBody);
            return response.code() == 201;
        } catch (Exception e) {
            LogUtils.error("FErrorReporter postErrorMsg: " + e.getMessage());
            return false;
        } finally {
            if (response != null) CloseUtils.close(response);
        }
    }
}
