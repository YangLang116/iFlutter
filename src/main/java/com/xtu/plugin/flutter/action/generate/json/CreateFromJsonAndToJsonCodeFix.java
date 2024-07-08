package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.*;
import com.xtu.plugin.flutter.action.generate.json.entity.DartFieldEntity;
import com.xtu.plugin.flutter.utils.DartUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CreateFromJsonAndToJsonCodeFix extends BaseCreateMethodsFix<DartComponent> {

    private final boolean hasFromJson;
    private final boolean hasToJson;
    private final boolean nullSafety;
    private final boolean isUnModifiableFromJson;

    public CreateFromJsonAndToJsonCodeFix(@NotNull DartClass dartClass,
                                          boolean nullSafety, boolean isUnModifiableFromJson,
                                          boolean hasFromJson, boolean hasToJson) {
        super(dartClass);
        this.nullSafety = nullSafety;
        this.isUnModifiableFromJson = isUnModifiableFromJson;
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
        this.anchor = this.doAddMethodsForOne(editor, templateManager, this.buildFunctionsText(templateManager, elementsToProcess), this.anchor);
    }

    protected Template buildFunctionsText(@NotNull TemplateManager templateManager, @NotNull Set<DartComponent> elementsToProcess) {
        Template template = templateManager.createTemplate(this.getClass().getName(), "Dart");
        template.setToReformat(true);
        List<DartFieldEntity> dartFieldList = parseFieldList(elementsToProcess);
        if (!hasFromJson) genFromJson(template, dartFieldList);
        if (!hasToJson) genToJson(template, dartFieldList);
        return template;
    }

    private void genFromJson(@NotNull Template template, @NotNull List<DartFieldEntity> fieldList) {
        String className = myDartClass.getName();
        template.addTextSegment(String.format(Locale.ROOT,
                "factory %s.fromJson(Map<String, dynamic> json) {return %s(",
                className, className)
        );
        for (DartFieldEntity field : fieldList) {
            if (field.isList()) {
                DartFieldEntity argument = field.argument;
                if (argument != null && !argument.isBuiltInType) {
                    String structWord = isUnModifiableFromJson ? "unmodifiable" : "from";
                    template.addTextSegment(String.format(Locale.ROOT,
                            """
                                     %s: json['%s'] == null ? \
                                     null : \
                                     List<%s>.%s(json['%s'].map((x) => %s.fromJson(x))),
                                    """,
                            field.name, field.name,
                            argument.type, structWord, field.name, argument.type));
                } else {
                    template.addTextSegment(String.format(Locale.ROOT,
                            "%s: json['%s'],",
                            field.name, field.name));
                }
            } else if (!field.isBuiltInType) {
                template.addTextSegment(String.format(Locale.ROOT,
                        "%s: %s.fromJson(json['%s']),",
                        field.name, field.type, field.name));
            } else {
                template.addTextSegment(String.format(Locale.ROOT,
                        "%s: json['%s'],",
                        field.name, field.name));
            }
        }
        template.addTextSegment(");}\n");
    }

    private void genToJson(@NotNull Template template, @NotNull List<DartFieldEntity> fieldList) {
        template.addTextSegment("Map<String, dynamic> toJson() => {");
        for (DartFieldEntity field : fieldList) {
            if (field.isList()) {
                DartFieldEntity argument = field.argument;
                if (argument != null && !argument.isBuiltInType) {
                    template.addTextSegment(String.format(Locale.ROOT,
                            "'%s': %s%s.map((e) => e.toJson()).toList(),",
                            field.name, field.name, nullSafety ? "?" : ""));
                } else {
                    template.addTextSegment(String.format(Locale.ROOT,
                            "'%s': %s,",
                            field.name, field.name));
                }
            } else if (!field.isBuiltInType) {
                template.addTextSegment(String.format(Locale.ROOT,
                        "'%s': %s%s.toJson(),",
                        field.name, field.name, nullSafety ? "?" : ""));
            } else {
                template.addTextSegment(String.format(Locale.ROOT,
                        "'%s': %s,",
                        field.name, field.name));
            }
        }
        template.addTextSegment("};\n");
    }

    @NotNull
    private List<DartFieldEntity> parseFieldList(@NotNull Set<DartComponent> elementsToProcess) {
        List<DartFieldEntity> dartFieldList = new ArrayList<>();
        for (DartComponent varAccessComponent : elementsToProcess) {
            DartComponentName componentName = PsiTreeUtil.getChildOfType(varAccessComponent, DartComponentName.class);
            if (componentName == null) continue;
            DartFieldEntity fieldEntity = new DartFieldEntity();
            fieldEntity.name = componentName.getText();
            DartSimpleType dartSimpleType = PsiTreeUtil.findChildOfType(varAccessComponent, DartSimpleType.class);
            if (dartSimpleType == null) continue;
            DartReferenceExpression referenceExpression = PsiTreeUtil.getChildOfType(dartSimpleType, DartReferenceExpression.class);
            if (referenceExpression == null) continue;
            fieldEntity.type = referenceExpression.getText();
            fieldEntity.isBuiltInType = DartUtils.isBuiltInType(referenceExpression);
            DartTypeArguments typeArguments = PsiTreeUtil.getChildOfType(dartSimpleType, DartTypeArguments.class);
            if (typeArguments != null) {
                DartReferenceExpression argumentReference = PsiTreeUtil.findChildOfType(typeArguments, DartReferenceExpression.class);
                if (argumentReference != null) {
                    DartFieldEntity argumentEntity = new DartFieldEntity();
                    argumentEntity.type = argumentReference.getText();
                    argumentEntity.isBuiltInType = DartUtils.isBuiltInType(argumentReference);
                    fieldEntity.argument = argumentEntity;
                }
            }
            dartFieldList.add(fieldEntity);
        }
        return dartFieldList;
    }

    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
        return null;
    }
}