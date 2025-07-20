package com.xtu.plugin.flutter.reporter;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ex.ApplicationInfoEx;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.util.Consumer;
import com.xtu.plugin.flutter.base.utils.VersionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class FErrorReporter extends ErrorReportSubmitter {

    private static final String sActionText = "Copy & Report";
    private static final String sReportUrl = "https://github.com/YangLang116/iFlutter/issues";

    @Override
    @NotNull
    @NlsActions.ActionText
    public String getReportActionText() {
        return sActionText;
    }

    @Override
    public boolean submit(IdeaLoggingEvent @NotNull [] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<? super SubmittedReportInfo> consumer) {
        final String errorStack = collectErrorStack(events);
        SubmittedReportInfo.SubmissionStatus status = postErrorMsg(additionalInfo, errorStack);
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

    private SubmittedReportInfo.SubmissionStatus postErrorMsg(@Nullable String additionalInfo, @NotNull String errorInfo) {
        ApplicationInfoEx appInfo = ApplicationInfoEx.getInstanceEx();
        StringBuilder contentSb = new StringBuilder()
                .append("version: ").append(VersionUtils.getPluginVersion()).append("\n")
                .append("os: ").append(SystemInfo.getOsNameAndVersion()).append("\n")
                .append("ide: ").append(appInfo.getFullApplicationName()).append("\n")
                .append("build: ").append(appInfo.getBuild().asString()).append("\n");
        if (additionalInfo != null) {
            contentSb.append("additionalInfo: ").append(additionalInfo).append("\n");
        }
        contentSb.append("errorStack:\n").append(errorInfo);
        CopyPasteManager.copyTextToClipboard(contentSb.toString());
        BrowserUtil.open(sReportUrl);
        return SubmittedReportInfo.SubmissionStatus.NEW_ISSUE;
    }
}
