package com.xtu.plugin.flutter.action.j2d.handler.entity;

import com.xtu.plugin.flutter.base.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class J2DFieldDescriptor {

    public static final int TYPE_PRIME = 0;
    public static final int TYPE_OBJECT = 1;
    public static final int TYPE_LIST = 2;

    private int type;
    private String key;
    private String displayName;
    private String className;
    private J2DFieldDescriptor subType;

    public static J2DFieldDescriptor prime(@NotNull String key, @NotNull String className) {
        J2DFieldDescriptor fieldDescriptor = new J2DFieldDescriptor();
        fieldDescriptor.type = TYPE_PRIME;
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.className = className;
        return fieldDescriptor;
    }

    public static J2DFieldDescriptor object(@NotNull String key, @NotNull String className) {
        J2DFieldDescriptor fieldDescriptor = new J2DFieldDescriptor();
        fieldDescriptor.type = TYPE_OBJECT;
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.className = className;
        return fieldDescriptor;
    }

    public static J2DFieldDescriptor list(@NotNull String key, @Nullable J2DFieldDescriptor subType) {
        J2DFieldDescriptor fieldDescriptor = new J2DFieldDescriptor();
        fieldDescriptor.type = TYPE_LIST;
        fieldDescriptor.key = key;
        fieldDescriptor.displayName = ClassUtils.toCamelCase(key);
        fieldDescriptor.className = "List";
        fieldDescriptor.subType = subType;
        return fieldDescriptor;
    }

    public int getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getClassName() {
        return className;
    }

    public J2DFieldDescriptor getSubType() {
        return subType;
    }
}
