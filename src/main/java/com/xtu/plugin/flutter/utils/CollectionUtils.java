package com.xtu.plugin.flutter.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
