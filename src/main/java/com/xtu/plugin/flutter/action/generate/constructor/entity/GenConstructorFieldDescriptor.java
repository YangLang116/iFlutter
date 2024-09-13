package com.xtu.plugin.flutter.action.generate.constructor.entity;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GenConstructorFieldDescriptor {

    private final boolean nullable;
    private final String name;

    public GenConstructorFieldDescriptor(boolean nullable, @NotNull String name) {
        this.nullable = nullable;
        this.name = name;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getName() {
        return name;
    }
}

