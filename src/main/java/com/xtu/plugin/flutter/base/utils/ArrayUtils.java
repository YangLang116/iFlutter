package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    @NotNull
    public static <T> List<T> sub(@NotNull List<T> aArray, @NotNull List<T> bArray) {
        List<T> result = new ArrayList<>();
        for (T a : aArray) {
            if (bArray.contains(a)) continue;
            result.add(a);
        }
        return result;
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
}
