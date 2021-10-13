package com.xtu.plugin.flutter.action.j2d.generate;

public class TypeEntity {

    public String name;
    public String type;
    public TypeEntity subType;

    public boolean isPrime;
    public boolean isObject;
    public boolean isList;

    public static TypeEntity prime(String name, String type) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.name = name;
        typeEntity.type = type;
        typeEntity.isPrime = true;
        typeEntity.isObject = false;
        typeEntity.isList = false;
        return typeEntity;
    }

    public static TypeEntity list(String name, String type, TypeEntity subType) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.name = name;
        typeEntity.type = type;
        typeEntity.subType = subType;
        typeEntity.isPrime = false;
        typeEntity.isObject = false;
        typeEntity.isList = true;
        return typeEntity;
    }

    public static TypeEntity object(String name, String type) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.name = name;
        typeEntity.type = type;
        typeEntity.isPrime = false;
        typeEntity.isObject = true;
        typeEntity.isList = false;
        return typeEntity;
    }

}
