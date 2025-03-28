package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CollectionUtils {

    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> void addIfNotEmpty(@NotNull Collection<T> collection, @Nullable T obj) {
        if (obj != null) collection.add(obj);
    }

    public static void standardList(List<String> list) {
        if (isEmpty(list)) return;
        Set<String> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);
        Collections.sort(list);
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

    public static boolean endsWith(@Nullable String test, @NotNull List<String> ends) {
        if (test == null) return false;
        final String finalTest = test.toLowerCase();
        for (String end : ends) {
            if (finalTest.endsWith(end.toLowerCase())) return true;
        }
        return false;
    }

    public static boolean containsIgnoreCase(@NotNull List<String> list, @NotNull String test) {
        return list.stream().anyMatch(it -> it.equalsIgnoreCase(test));
    }
}
