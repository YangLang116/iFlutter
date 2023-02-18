package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts.Command;
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
    private final boolean enableFlutter2;

    public CreateFromJsonAndToJsonCodeFix(@NotNull DartClass dartClass, boolean enableFlutter2, boolean hasFromJson, boolean hasToJson) {
        super(dartClass);
        this.enableFlutter2 = enableFlutter2;
        this.hasFromJson = hasFromJson;
        this.hasToJson = hasToJson;
    }

    @NotNull
    @Command
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
//        DartVarAccessDeclaration
//            DartType
//              DartSimpleType
//                  DartReferenceExpression
//                        DartId
//                  DartTypeArguments
//                       DartTypeList
//                          DartType
//          DartComponentName
        Template template = templateManager.createTemplate(this.getClass().getName(), "Dart");
        template.setToReformat(true);
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
        if (!hasFromJson) { //生成fromJson方法
            String className = myDartClass.getName();
            template.addTextSegment(String.format(
                    Locale.US,
                    "factory %s.fromJson(Map<String, dynamic> json) {return %s(",
                    className, className));
            for (DartFieldEntity fieldEntity : dartFieldList) {
                if (fieldEntity.isList()) {
                    DartFieldEntity argument = fieldEntity.argument;
                    if (argument != null && !argument.isBuiltInType) {
                        template.addTextSegment(String.format(Locale.US,
                                "%s: json['%s'] == null ? " +
                                        "List<%s>.unmodifiable([]) : " +
                                        "\nList<%s>.unmodifiable(\njson['%s'].map((x) => %s.fromJson(x))),",
                                fieldEntity.name, fieldEntity.name,
                                argument.type,
                                argument.type, fieldEntity.name, argument.type));
                    } else {
                        template.addTextSegment(String.format(Locale.US, "%s: json['%s'],", fieldEntity.name, fieldEntity.name));
                    }
                } else if (!fieldEntity.isBuiltInType) {
                    template.addTextSegment(String.format(Locale.US, "%s: %s.fromJson(json['%s']),",
                            fieldEntity.name, fieldEntity.type, fieldEntity.name));
                } else {
                    template.addTextSegment(String.format(Locale.US, "%s: json['%s'],", fieldEntity.name, fieldEntity.name));
                }
            }
            template.addTextSegment(");}\n");
        }
        if (!hasToJson) {
            template.addTextSegment("Map<String, dynamic> toJson() => {");
            for (DartFieldEntity fieldEntity : dartFieldList) {
                if (fieldEntity.isList()) {
                    DartFieldEntity argument = fieldEntity.argument;
                    if (argument != null && !argument.isBuiltInType) {
                        template.addTextSegment(String.format(Locale.US,
                                "'%s': %s%s.map((e) => e.toJson()).toList(),",
                                fieldEntity.name, fieldEntity.name, enableFlutter2 ? "?" : ""));
                    } else {
                        template.addTextSegment(String.format(Locale.US, "'%s': %s,", fieldEntity.name, fieldEntity.name));
                    }
                } else if (!fieldEntity.isBuiltInType) {
                    template.addTextSegment(String.format(Locale.US, "'%s': %s%s.toJson(),",
                            fieldEntity.name, fieldEntity.name, enableFlutter2 ? "?" : ""));
                } else {
                    template.addTextSegment(String.format(Locale.US, "'%s': %s,", fieldEntity.name, fieldEntity.name));
                }
            }
            template.addTextSegment("};\n");
        }
        return template;
    }

    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
        return null;
    }
}