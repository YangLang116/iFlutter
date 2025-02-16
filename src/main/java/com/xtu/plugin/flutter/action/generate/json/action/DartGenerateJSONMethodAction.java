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

public abstract class DartGenerateJSONMethodAction extends BaseDartGenerateAction {

    private boolean createFromJson;
    private boolean createToJson;

    protected boolean doEnable(@Nullable DartClass dartClass) {
        this.createFromJson = needGenFromJson(dartClass);
        this.createToJson = needGenToJson(dartClass);
        return doEnable(createFromJson, createToJson);
    }

    protected abstract boolean doEnable(boolean createFromJson, boolean createToJson);

    protected boolean needGenFromJson(@Nullable DartClass dartClass) {
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

    protected boolean needGenToJson(@Nullable DartClass dartClass) {
        if (dartClass == null) return false;
        return !doesClassContainMethod(dartClass, "toJson");
    }

    @Override
    protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
        return new DartGenerateJSONMethodHandler(createFromJson, createToJson);
    }
}
