package com.xtu.plugin.flutter.action.generate.constructor;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartGenerateConstructorHandler extends BaseDartGenerateHandler {

    @Override
    @NotNull
    protected String getTitle() {
        return "IFlutter.Constructor";
    }

    @Override
    protected boolean doAllowEmptySelection() {
        return true;
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass,
                                     @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(
                this.computeClassMembersMap(dartClass, false).values(),
                (component) -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }

    @Override
    @NotNull
    protected DartGenerateConstructorCodeFix createFix(@NotNull DartClass dartClass) {
        return new DartGenerateConstructorCodeFix(dartClass);
    }
}
