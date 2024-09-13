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
import com.jetbrains.lang.dart.psi.DartSimpleType;
import com.xtu.plugin.flutter.action.generate.constructor.entity.GenConstructorFieldDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import template.PluginTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DartGenerateConstructorCodeFix extends BaseCreateMethodsFix<DartComponent> {

    private final boolean supportNullSafety;

    public DartGenerateConstructorCodeFix(boolean supportNullSafety, @NotNull DartClass dartClass) {
        super(dartClass);
        this.supportNullSafety = supportNullSafety;
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
        Template template = this.buildTemplate(templateManager, elementsToProcess);
        this.anchor = this.doAddMethodsForOne(editor, templateManager, template, this.anchor);
    }

    @Nullable
    protected Template buildTemplate(@NotNull TemplateManager templateManager, @NotNull Set<DartComponent> varList) {
        String className = myDartClass.getName();
        if (className == null) return null;
        List<GenConstructorFieldDescriptor> fieldList = new ArrayList<>();
        for (DartComponent var : varList) {
            DartComponentName componentName = PsiTreeUtil.getChildOfType(var, DartComponentName.class);
            if (componentName == null) continue;
            String variantName = componentName.getText();
            DartSimpleType dartSimpleType = PsiTreeUtil.findChildOfType(var, DartSimpleType.class);
            if (dartSimpleType == null) continue;
            boolean nullable = !supportNullSafety || dartSimpleType.getText().endsWith("?");
            fieldList.add(new GenConstructorFieldDescriptor(nullable, variantName));
        }
        String content = PluginTemplate.getGenConstructor(className, fieldList);
        Template template = templateManager.createTemplate(this.getClass().getName(), "Dart");
        template.setToReformat(true);
        template.addTextSegment(content);
        return template;
    }

}
