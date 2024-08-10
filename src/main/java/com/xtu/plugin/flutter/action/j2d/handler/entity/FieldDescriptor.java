package com.xtu.plugin.flutter.action.j2d.handler.entity;

import com.xtu.plugin.flutter.base.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldDescriptor {

    public String key;
    public String displayName;
    public String type;
    public FieldDescriptor subType;

    public boolean isPrime;
    public boolean isObject;
    public boolean isList;

    public static FieldDescriptor prime(@NotNull String key, @NotNull String type) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.type = type;
        fieldDescriptor.isPrime = true;
        fieldDescriptor.isObject = false;
        fieldDescriptor.isList = false;
        return fieldDescriptor;
    }

    public static FieldDescriptor list(@NotNull String key, @Nullable FieldDescriptor subType) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.type = "List";
        fieldDescriptor.subType = subType;
        fieldDescriptor.isPrime = false;
        fieldDescriptor.isObject = false;
        fieldDescriptor.isList = true;
        return fieldDescriptor;
    }

    public static FieldDescriptor object(@NotNull String key, @NotNull String type) {
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.type = type;
        fieldDescriptor.isPrime = false;
        fieldDescriptor.isObject = true;
        fieldDescriptor.isList = false;
        return fieldDescriptor;
    }
}
