package com.xtu.plugin.flutter.action.generate.json.action;

public class DartGenerateAllJSONMethodAction extends DartGenerateJSONMethodAction {

    @Override
    protected boolean doEnable(boolean createFromJson, boolean createToJson) {
        return createFromJson && createToJson;
    }
}
