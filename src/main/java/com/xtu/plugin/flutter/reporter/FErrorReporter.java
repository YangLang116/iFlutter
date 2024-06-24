package com.xtu.plugin.flutter.reporter;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.util.Consumer;
import com.xtu.plugin.flutter.advice.AdviceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class FErrorReporter extends ErrorReportSubmitter {

    private static final String sActionText = "Report to Author";

    @Override
    @NotNull
    @NlsActions.ActionText
    public String getReportActionText() {
        return sActionText;
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events,
                          @Nullable String additionalInfo,
                          @NotNull Component parentComponent,
                          @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        final DataManager mgr = DataManager.getInstance();
        final DataContext context = mgr.getDataContext(parentComponent);
        final Project project = CommonDataKeys.PROJECT.getData(context);
        assert project != null;
        final String errorStack = collectErrorStack(events);
        SubmittedReportInfo.SubmissionStatus status = postErrorMsg(project, additionalInfo, errorStack);
        consumer.consume(new SubmittedReportInfo(status));
        return true;
    }

    private String collectErrorStack(@NotNull IdeaLoggingEvent[] events) {
        StringBuilder errorStackInfoBuilder = new StringBuilder();
        for (IdeaLoggingEvent event : events) {
            errorStackInfoBuilder.append(event.getThrowableText()).append("\n");
        }
        return errorStackInfoBuilder.toString();
    }

    private SubmittedReportInfo.SubmissionStatus postErrorMsg(
            @NotNull Project project,
            @Nullable String additionalInfo,
            @NotNull String errorInfo) {
        StringBuilder contentSb = new StringBuilder();
        if (additionalInfo != null) {
            contentSb.append("additionalInfo:\n").append(additionalInfo).append("\n\n");
        }
        contentSb.append("errorStack:\n").append(errorInfo);
        AdviceManager.getInstance().submitAdvice(project, "report issue", contentSb.toString());
        return SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;
    }
}
