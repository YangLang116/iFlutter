package com.xtu.plugin.flutter.action.generate.json.action;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.Nullable;

public class DartGenerateFromJSONMethodAction extends DartGenerateJSONMethodAction {


    @Override
    protected boolean doEnable(boolean createFromJson, boolean createToJson) {
        return createFromJson;
    }

    @Override
    protected boolean needGenToJson(@Nullable DartClass dartClass) {
        return false;
    }
}
