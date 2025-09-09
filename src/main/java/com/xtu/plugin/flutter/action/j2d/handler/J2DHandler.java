package com.xtu.plugin.flutter.action.j2d.handler;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.j2d.handler.entity.ClassEntity;
import com.xtu.plugin.flutter.action.j2d.handler.entity.J2DFieldDescriptor;
import com.xtu.plugin.flutter.base.utils.ClassUtils;
import com.xtu.plugin.flutter.base.utils.JsonUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.OrderJSONObject;
import template.PluginTemplate;

import java.util.ArrayList;
import java.util.List;

public class J2DHandler {

    private final Project project;

    public J2DHandler(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public String genCode(@NotNull String className, @NotNull String jsonData, boolean keepComment) throws JSONException {
        List<ClassEntity> classList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject(jsonData);
        createAndSaveClass(className, jsonObject, keepComment, classList);

        StringBuilder fileContentBuilder = new StringBuilder();
        for (int i = classList.size() - 1; i >= 0; i--) {
            fileContentBuilder.append(classList.get(i).content);
            if (i != 0) fileContentBuilder.append("\n\n");
        }
        return fileContentBuilder.toString();
    }

    private void createAndSaveClass(@NotNull String className, @NotNull JSONObject jsonObj, boolean keepComment, @NotNull List<ClassEntity> classList) {
        List<J2DFieldDescriptor> fieldList = parseFieldList(jsonObj, keepComment, classList);
        String comment = getComment(jsonObj, keepComment);
        String content = PluginTemplate.getJ2DContent(project, className, comment, fieldList);
        classList.add(new ClassEntity(className, content));
    }

    @NotNull
    private List<J2DFieldDescriptor> parseFieldList(@NotNull JSONObject rawObj, boolean keepComment, @NotNull List<ClassEntity> classList) {
        OrderJSONObject orderObj = JsonUtils.toOrderJsonObject(rawObj);
        List<J2DFieldDescriptor> fieldList = new ArrayList<>();
        for (String fieldName : orderObj.keySet()) {
            Object fieldValue = orderObj.get(fieldName);
            J2DFieldDescriptor field = parseField(fieldName, fieldValue, keepComment, classList, name -> ClassUtils.getClassName(name) + "Entity");
            if (field == null) continue;
            fieldList.add(field);
        }
        return fieldList;
    }

    @Nullable
    private J2DFieldDescriptor parseField(@NotNull String key, @NotNull Object value, boolean keepComment, @NotNull List<ClassEntity> classList, @NotNull ClassNameFactory classNameFactory) {
        switch (value) {
            case String ignored -> {
                return J2DFieldDescriptor.prime(key, "String");
            }
            case Boolean ignored -> {
                return J2DFieldDescriptor.prime(key, "bool");
            }
            case Number ignored -> {
                return J2DFieldDescriptor.prime(key, "num");
            }
            case JSONObject jsonObject -> {
                String className = getClassName(key, classNameFactory, classList);
                createAndSaveClass(className, jsonObject, keepComment, classList);
                return J2DFieldDescriptor.object(key, className);
            }
            case JSONArray objects -> {
                J2DFieldDescriptor argumentFieldDescriptor = null;
                if (!objects.isEmpty()) {
                    Object item = ((JSONArray) value).get(0);
                    argumentFieldDescriptor = parseField(key, item, keepComment, classList, name -> ClassUtils.getClassName(name) + "ItemEntity");
                }
                return J2DFieldDescriptor.list(key, argumentFieldDescriptor);
            }
            default -> {
                return null;
            }
        }
    }

    private String getClassName(@NotNull String name, @NotNull ClassNameFactory factory, @NotNull List<ClassEntity> classList) {
        int index = 1;
        String candidate = factory.create(name);
        while (true) {
            boolean hasSameName = false;
            for (ClassEntity classEntity : classList) {
                if (StringUtils.equals(classEntity.name, candidate)) {
                    hasSameName = true;
                    break;
                }
            }
            if (!hasSameName) break;
            candidate = factory.create(name + (index++));
        }
        return candidate;
    }

    @Nullable
    private String getComment(@NotNull JSONObject rawJson, boolean keepComment) {
        if (!keepComment) {
            return null;
        }
        OrderJSONObject orderJson = new OrderJSONObject();
        for (String key : JsonUtils.getOrderKeySet(rawJson)) {
            Object value = rawJson.get(key);
            if (value instanceof JSONObject) {
                continue;
            }
            if (value instanceof JSONArray && !((JSONArray) value).isEmpty() && ((JSONArray) value).get(0) instanceof JSONObject) {
                continue;
            }
            orderJson.put(key, value);
        }
        String[] lines = orderJson.toString(4).split("\n");
        StringBuilder commentSb = new StringBuilder();
        for (String line : lines) {
            commentSb.append("// ").append(line).append("\n");
        }
        return commentSb.toString();
    }
}
