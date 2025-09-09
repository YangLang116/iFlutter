package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageReader;
import java.io.Closeable;
import java.io.IOException;

public class CloseUtils {

    public static void close(@Nullable Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                //ignore
            }
        }
    }

    public static void close(@Nullable ImageReader reader) {
        if (reader != null) reader.dispose();
    }
}
