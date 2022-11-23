package com.xtu.plugin.flutter.action.generate.json.entity;

import org.apache.commons.lang.StringUtils;

public class DartFieldEntity {

    public String name;
    public String type;
    public boolean isBuiltInType;

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
                ", argument=" + argument +
                '}';
    }
}
