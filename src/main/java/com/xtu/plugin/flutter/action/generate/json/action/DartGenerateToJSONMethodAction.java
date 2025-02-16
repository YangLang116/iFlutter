package com.xtu.plugin.flutter.action.generate.json.action;

import com.jetbrains.lang.dart.psi.DartClass;
import org.jetbrains.annotations.Nullable;

public class DartGenerateToJSONMethodAction extends DartGenerateJSONMethodAction {


    @Override
    protected boolean doEnable(boolean createFromJson, boolean createToJson) {
        return createToJson;
    }

    @Override
    protected boolean needGenFromJson(@Nullable DartClass dartClass) {
        return false;
    }
}
