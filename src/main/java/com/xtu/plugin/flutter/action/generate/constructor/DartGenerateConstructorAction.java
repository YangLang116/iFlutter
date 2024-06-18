package com.xtu.plugin.flutter.action.generate.constructor;

import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;

import org.jetbrains.annotations.NotNull;

public class DartGenerateConstructorAction extends BaseDartGenerateAction {

    @Override
    @NotNull
    protected BaseDartGenerateHandler getGenerateHandler() {
        return new DartGenerateConstructorHandler();
    }
}
