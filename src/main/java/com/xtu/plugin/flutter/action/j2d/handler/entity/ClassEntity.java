package com.xtu.plugin.flutter.action.j2d.handler.entity;

import org.jetbrains.annotations.NotNull;

public class ClassEntity {

    public String name;
    public String content;

    public ClassEntity(@NotNull String name, @NotNull String content) {
        this.name = name;
        this.content = content;
    }
}
