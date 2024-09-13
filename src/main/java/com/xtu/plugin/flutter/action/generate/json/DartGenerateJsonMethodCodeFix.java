package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.*;
import com.xtu.plugin.flutter.action.generate.json.entity.GenJSONMethodFieldDescriptor;
import com.xtu.plugin.flutter.base.utils.DartUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import template.PluginTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DartGenerateJsonMethodCodeFix extends BaseCreateMethodsFix<DartComponent> {

    private final boolean hasFromJson;
    private final boolean hasToJson;
    private final boolean supportNullSafety;

    public DartGenerateJsonMethodCodeFix(@NotNull DartClass dartClass,
                                         boolean supportNullSafety,
                                         boolean hasFromJson, boolean hasToJson) {
        super(dartClass);
        this.supportNullSafety = supportNullSafety;
        this.hasFromJson = hasFromJson;
        this.hasToJson = hasToJson;
    }

    @NotNull
    protected String getCommandName() {
        return "IFlutter.FromJsonAndToJson";
    }

    @NotNull
    protected String getNothingFoundMessage() {
        return "";
    }

    protected void processElements(@NotNull Project project, @NotNull Editor editor, @NotNull Set<DartComponent> elementsToProcess) {
        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = this.buildTemplate(project, templateManager, elementsToProcess);
        this.anchor = this.doAddMethodsForOne(editor, templateManager, template, this.anchor);
    }

    @Nullable
    protected Template buildTemplate(@NotNull Project project,
                                     @NotNull TemplateManager templateManager,
                                     @NotNull Set<DartComponent> elementsToProcess) {
        List<GenJSONMethodFieldDescriptor> dartFieldList = parseFieldList(elementsToProcess);
        Template template = templateManager.createTemplate(this.getClass().getName(), "Dart");
        template.setToReformat(true);
        if (!hasFromJson) addFromJSONSegment(project, template, dartFieldList);
        if (!hasToJson) addToJSONSegment(template, dartFieldList);
        return template;
    }

    private void addFromJSONSegment(@NotNull Project project,
                                    @NotNull Template template,
                                    @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        String className = myDartClass.getName();
        if (className == null) return;
        String fromJSONMethod = PluginTemplate.getGenFromJSONMethod(project, className, fieldList);
        template.addTextSegment(fromJSONMethod);
    }

    private void addToJSONSegment(@NotNull Template template,
                                  @NotNull List<GenJSONMethodFieldDescriptor> fieldList) {
        String toJSONMethod = PluginTemplate.getGenToJSONMethod(fieldList);
        template.addTextSegment(toJSONMethod);
    }

    @NotNull
    private List<GenJSONMethodFieldDescriptor> parseFieldList(@NotNull Set<DartComponent> varList) {
        List<GenJSONMethodFieldDescriptor> dartFieldList = new ArrayList<>();
        for (DartComponent var : varList) {
            GenJSONMethodFieldDescriptor field = createField(var);
            if (field == null) continue;
            GenJSONMethodFieldDescriptor typeArgument = createTypeArgument(var);
            if (typeArgument != null) {
                field.setSubType(typeArgument);
            }
            dartFieldList.add(field);
        }
        return dartFieldList;
    }

    @Nullable
    private GenJSONMethodFieldDescriptor createField(@NotNull DartComponent varAccessComponent) {
        DartComponentName componentName = PsiTreeUtil.getChildOfType(varAccessComponent, DartComponentName.class);
        if (componentName == null) return null;
        GenJSONMethodFieldDescriptor fieldEntity = new GenJSONMethodFieldDescriptor();
        fieldEntity.setName(componentName.getText());
        DartSimpleType dartSimpleType = PsiTreeUtil.findChildOfType(varAccessComponent, DartSimpleType.class);
        if (dartSimpleType == null) return null;
        DartReferenceExpression referenceExpression = PsiTreeUtil.getChildOfType(dartSimpleType, DartReferenceExpression.class);
        if (referenceExpression == null) return null;
        fieldEntity.setClassName(referenceExpression.getText());
        fieldEntity.setBuildIn(DartUtils.isBuiltInType(referenceExpression));
        fieldEntity.setNullable(!supportNullSafety || dartSimpleType.getText().endsWith("?"));
        fieldEntity.setDealNullable(dartSimpleType.getText().endsWith("?"));
        return fieldEntity;
    }

    @Nullable
    private GenJSONMethodFieldDescriptor createTypeArgument(@NotNull DartComponent varAccessComponent) {
        DartTypeArguments typeArguments = PsiTreeUtil.findChildOfType(varAccessComponent, DartTypeArguments.class);
        if (typeArguments == null) return null;
        DartReferenceExpression argumentReference = PsiTreeUtil.findChildOfType(typeArguments, DartReferenceExpression.class);
        if (argumentReference == null) return null;
        GenJSONMethodFieldDescriptor argumentEntity = new GenJSONMethodFieldDescriptor();
        argumentEntity.setClassName(argumentReference.getText());
        argumentEntity.setBuildIn(DartUtils.isBuiltInType(argumentReference));
        argumentEntity.setNullable(!supportNullSafety || typeArguments.getText().contains("?"));
        argumentEntity.setDealNullable(typeArguments.getText().contains("?"));
        return argumentEntity;
    }

    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
        return null;
    }
}