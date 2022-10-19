package com.xtu.plugin.flutter.action.generate.json;

import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartFactoryConstructorDeclaration;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartGenerateFromJsonAndToJsonAction extends BaseDartGenerateAction {

    private boolean hasFromJson;
    private boolean hasToJson;

    protected boolean doEnable(@Nullable DartClass dartClass) {
        if (dartClass == null) return false;
        return !doesClassContainFromJsonAndToJson(dartClass);
    }

    public boolean doesClassContainFromJsonAndToJson(@NotNull DartClass dartClass) {
        DartFactoryConstructorDeclaration[] constructorDeclarationList = PsiTreeUtil.getChildrenOfType(
                DartResolveUtil.getBody(dartClass),
                DartFactoryConstructorDeclaration.class);
        if (constructorDeclarationList != null) {
            for (DartFactoryConstructorDeclaration constructor : constructorDeclarationList) {
                if (StringUtils.equals(constructor.getName(), "fromJson")) {
                    hasFromJson = true;
                    break;
                }
            }
        }
        hasToJson = doesClassContainMethod(dartClass, "toJson");
        return hasFromJson && hasToJson;
    }

    @Override
    protected @NotNull BaseDartGenerateHandler getGenerateHandler() {
        return new DartGenerateFromJsonAndToJsonCodeHandler(hasFromJson, hasToJson);
    }
}
