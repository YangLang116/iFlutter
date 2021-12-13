package com.xtu.plugin.flutter.action.generate.json.entity;

public class DartFieldEntity {

    public String name;
    public String type;
    public String argumentType;

    @Override
    public String toString() {
        return "DartClassEntity{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", argumentType='" + argumentType + '\'' +
                '}';
    }
}
