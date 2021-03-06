package com.xtu.plugin.flutter.utils;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CollectionUtils {

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> void duplicateList(List<T> list) {
        if (isEmpty(list)) return;
        Set<T> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);
    }

    public static String join(@NotNull List<String> list, @NotNull String joinStr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i != 0) sb.append(joinStr);
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    public static List<String> split(@NotNull String str, String split) {
        String[] result = str.split(split);
        return new ArrayList<>(Arrays.asList(result));
    }
}
