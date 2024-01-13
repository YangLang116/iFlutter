package com.xtu.plugin.flutter.action.j2d.generate;

import com.xtu.plugin.flutter.utils.ClassUtils;

public class TypeEntity {

    public String key;
    public String displayName;
    public String type;
    public TypeEntity subType;

    public boolean isPrime;
    public boolean isObject;
    public boolean isList;

    public static TypeEntity prime(String key, String type) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.key = key;
        typeEntity.displayName = ClassUtils.toCamelCase(key);
        typeEntity.type = type;
        typeEntity.isPrime = true;
        typeEntity.isObject = false;
        typeEntity.isList = false;
        return typeEntity;
    }

    public static TypeEntity list(String key, TypeEntity subType) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.key = key;
        typeEntity.displayName = ClassUtils.toCamelCase(key);
        typeEntity.type = "List";
        typeEntity.subType = subType;
        typeEntity.isPrime = false;
        typeEntity.isObject = false;
        typeEntity.isList = true;
        return typeEntity;
    }

    public static TypeEntity object(String key, String type) {
        TypeEntity typeEntity = new TypeEntity();
        typeEntity.key = key;
        typeEntity.displayName = ClassUtils.toCamelCase(key);
        typeEntity.type = type;
        typeEntity.isPrime = false;
        typeEntity.isObject = true;
        typeEntity.isList = false;
        return typeEntity;
    }

}
