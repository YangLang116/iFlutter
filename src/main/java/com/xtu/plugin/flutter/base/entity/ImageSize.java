package com.xtu.plugin.flutter.base.entity;

import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ImageSize {

    public final int width;
    public final int height;

    public ImageSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @NotNull
    public Dimension toDimension() {
        return new Dimension(width, height);
    }
}
