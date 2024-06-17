package com.xtu.plugin.flutter.action.generate.constructor;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.xtu.plugin.flutter.utils.CollectionUtils;
import com.xtu.plugin.flutter.utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartGenerateConstructorCodeFix extends BaseCreateMethodsFix<DartComponent> {

    public DartGenerateConstructorCodeFix(@NotNull DartClass dartClass) {
        super(dartClass);
    }

    @Override
    @NotNull
    protected String getNothingFoundMessage() {
        return "";
    }

    @Override
    @NotNull
    protected String getCommandName() {
        return "IFlutter.Constructor";
    }

    @Override
    @Nullable
    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent dartComponent) {
        return null;
    }

    @Override
    protected void processElements(@NotNull Project project,
                                   @NotNull Editor editor,
                                   @NotNull Set<DartComponent> elementsToProcess) {
        TemplateManager templateManager = TemplateManager.getInstance(project);
        this.anchor = this.doAddMethodsForOne(editor, templateManager,
                this.buildFunctionsText(templateManager, elementsToProcess), this.anchor);
    }

    protected Template buildFunctionsText(@NotNull TemplateManager templateManager,
                                          @NotNull Set<DartComponent> elementsToProcess) {
        Template template = templateManager.createTemplate(this.getClass().getName(), "Dart");
        template.setToReformat(true);
        String className = myDartClass.getName();
        template.addTextSegment(String.format("%s(", className));
        if (!CollectionUtils.isEmpty(elementsToProcess)) {
            template.addTextSegment("{");
            for (DartComponent varAccessComponent : elementsToProcess) {
                DartComponentName componentName = PsiTreeUtil.getChildOfType(varAccessComponent, DartComponentName.class);
                if (componentName == null) continue;
                String name = componentName.getText();
                if (StringUtils.isEmpty(name)) continue;
                template.addTextSegment(String.format("this.%s,", name));
            }
            template.addTextSegment("}");
        }
        template.addTextSegment(");");
        return template;
    }

}
