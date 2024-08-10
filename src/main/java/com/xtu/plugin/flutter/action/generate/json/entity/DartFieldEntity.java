package com.xtu.plugin.flutter.action.generate.json.entity;

import com.xtu.plugin.flutter.base.utils.StringUtils;

public class DartFieldEntity {

    public String name;
    public String type;
    public boolean isBuiltInType;
    public boolean isNullable;

    public DartFieldEntity argument;


    public boolean isList() {
        return StringUtils.equals(type, "List");
    }

    @Override
    public String toString() {
        return "DartFieldEntity{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isBuiltInType=" + isBuiltInType +
                ", isNullable=" + isNullable +
                ", argument=" + argument +
                '}';
    }
}
