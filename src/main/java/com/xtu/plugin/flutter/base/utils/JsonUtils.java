package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class JsonUtils {

    public static String formatJson(@NotNull String jsonData) {
        if (jsonData.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonData);
            return jsonArray.toString(4);
        } else {
            JSONObject orderObj = createOrderObj(jsonData);
            return orderObj.toString(4);
        }
    }

    public static JSONObject createOrderObj(@NotNull String jsonData) {
        JSONObject rawObj = new JSONObject(jsonData);
        List<String> keys = new ArrayList<String>(rawObj.keySet());
        Collections.sort(keys);
        Map<String, Object> pairs = new LinkedHashMap<>();
        for (String key : keys) {
            pairs.put(key, rawObj.get(key));
        }
        return new JSONObject(pairs);
    }
}
