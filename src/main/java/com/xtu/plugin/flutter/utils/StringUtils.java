package com.xtu.plugin.flutter.utils;


import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class StringUtils {

    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String join(Collection<?> collection, String separator) {
        return collection == null ? null : join(collection.iterator(), separator);
    }

    public static String join(Iterator<?> iterator, String separator) {
        if (iterator == null) {
            return null;
        } else if (!iterator.hasNext()) {
            return "";
        } else {
            Object first = iterator.next();
            if (!iterator.hasNext()) {
                return Objects.toString(first);
            } else {
                StringBuilder buf = new StringBuilder(256);
                if (first != null) {
                    buf.append(first);
                }

                while (iterator.hasNext()) {
                    if (separator != null) {
                        buf.append(separator);
                    }

                    Object obj = iterator.next();
                    if (obj != null) {
                        buf.append(obj);
                    }
                }

                return buf.toString();
            }
        }
    }
}
