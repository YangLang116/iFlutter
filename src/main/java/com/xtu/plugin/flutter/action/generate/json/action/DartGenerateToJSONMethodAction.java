package com.xtu.plugin.flutter.action.generate.json.action;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.NotNull;

public class DartGenerateToJSONMethodAction extends DartGenerateJSONMethodAction {

    @Override
    protected boolean needGenFromJson(@NotNull DartClass dartClass) {
        return false;
    }
}
