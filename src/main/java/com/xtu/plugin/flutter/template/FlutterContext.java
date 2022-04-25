package com.xtu.plugin.flutter.template;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class FlutterContext extends TemplateContextType {

    protected FlutterContext() {
        super("iFLUTTER", "iFlutter");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        PsiFile file = templateActionContext.getFile();
        return file.getName().endsWith(".dart");
    }
}
