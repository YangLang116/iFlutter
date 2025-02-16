package com.xtu.plugin.flutter.action.generate.json.entity;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GenJSONMethodFieldDescriptor {

    private String name;
    private String className;
    private boolean buildIn;
    private boolean nullable;
    private GenJSONMethodFieldDescriptor subType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(@NotNull String className) {
        this.className = className;
    }

    public boolean isBuildIn() {
        return buildIn;
    }

    public void setBuildIn(boolean buildIn) {
        this.buildIn = buildIn;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public GenJSONMethodFieldDescriptor getSubType() {
        return subType;
    }

    public void setSubType(@NotNull GenJSONMethodFieldDescriptor subType) {
        this.subType = subType;
    }
}
