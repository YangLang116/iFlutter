package com.xtu.plugin.flutter.action.generate.json.action;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartFactoryConstructorDeclaration;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.xtu.plugin.flutter.action.generate.json.DartGenerateJSONMethodHandler;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartGenerateJSONMethodAction extends BaseDartGenerateAction {

    private boolean createFromJson;
    private boolean createToJson;

    protected boolean doEnable(@Nullable DartClass dartClass) {
        if (dartClass == null) return false;
        this.createFromJson = needGenFromJson(dartClass);
        this.createToJson = needGenToJson(dartClass);
        return this.createFromJson || this.createToJson;
    }

    protected boolean needGenFromJson(@NotNull DartClass dartClass) {
        DartFactoryConstructorDeclaration[] constructorDeclarationList = PsiTreeUtil.getChildrenOfType(DartResolveUtil.getBody(dartClass), DartFactoryConstructorDeclaration.class);
        if (constructorDeclarationList != null) {
            for (DartFactoryConstructorDeclaration constructor : constructorDeclarationList) {
                if (StringUtils.equals(constructor.getName(), "fromJson")) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean needGenToJson(@NotNull DartClass dartClass) {
        return !doesClassContainMethod(dartClass, "toJson");
    }

    @Override
    protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
        return new DartGenerateJSONMethodHandler(createFromJson, createToJson);
    }
}
