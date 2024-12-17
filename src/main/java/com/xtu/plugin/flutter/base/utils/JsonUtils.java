package com.xtu.plugin.flutter.base.utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.OrderJSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonUtils {

    public static String formatJson(@NotNull String jsonData) {
        if (jsonData.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonData);
            return jsonArray.toString(4);
        } else {
            JSONObject rawObj = new JSONObject(jsonData);
            OrderJSONObject orderObj = toOrderJsonObject(rawObj);
            return orderObj.toString(4);
        }
    }

    public static OrderJSONObject toOrderJsonObject(@NotNull JSONObject rawObj) {
        OrderJSONObject orderObj = new OrderJSONObject();
        for (String key : getOrderKeySet(rawObj)) {
            orderObj.put(key, rawObj.get(key));
        }
        return orderObj;
    }

    public static List<String> getOrderKeySet(@NotNull JSONObject jsonObj) {
        List<String> keySet = new ArrayList<>(jsonObj.keySet());
        Collections.sort(keySet);
        return keySet;
    }
}
