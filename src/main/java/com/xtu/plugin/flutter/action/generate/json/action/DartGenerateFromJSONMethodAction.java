package com.xtu.plugin.flutter.action.generate.json.action;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;

public class DartGenerateFromJSONMethodAction extends DartGenerateJSONMethodAction {

    @Override
    protected boolean needGenToJson(@NotNull DartClass dartClass) {
        return false;
    }
}
