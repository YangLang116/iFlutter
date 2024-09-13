package com.xtu.plugin.flutter.action.j2d.handler;

import com.intellij.openapi.project.Project;
import com.xtu.plugin.flutter.action.j2d.handler.entity.ClassEntity;
import com.xtu.plugin.flutter.action.j2d.handler.entity.J2DFieldDescriptor;
import com.xtu.plugin.flutter.base.utils.ClassUtils;
import com.xtu.plugin.flutter.base.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import template.PluginTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class J2DHandler {

    private final Project project;

    public J2DHandler(@NotNull Project project) {
        this.project = project;
    }

    public String formatJson(@NotNull String jsonData) throws JSONException {
        if (jsonData.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonData);
            return jsonArray.toString(4);
        } else {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.toString(4);
        }
    }

    @NotNull
    public String genCode(@NotNull String className, @NotNull String jsonData) throws JSONException {
        List<ClassEntity> classList = new ArrayList<>();
        createAndSaveClass(className, new JSONObject(jsonData), classList);

        StringBuilder fileContentBuilder = new StringBuilder();
        for (int i = classList.size() - 1; i >= 0; i--) {
            fileContentBuilder.append(classList.get(i).content);
            if (i != 0) fileContentBuilder.append("\n\n");
        }
        return fileContentBuilder.toString();
    }

    private void createAndSaveClass(@NotNull String className,
                                    @NotNull JSONObject jsonObject,
                                    @NotNull List<ClassEntity> classList) {
        List<J2DFieldDescriptor> fieldList = parseFieldList(jsonObject, classList);
        String content = PluginTemplate.getJ2DContent(project, className, fieldList);
        classList.add(new ClassEntity(className, content));
    }


    @NotNull
    private List<J2DFieldDescriptor> parseFieldList(@NotNull JSONObject jsonObject, @NotNull List<ClassEntity> classList) {
        List<J2DFieldDescriptor> fieldList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = jsonObject.keySet();
        for (String fieldName : fieldSet) {
            Object fieldValue = jsonObject.get(fieldName);
            J2DFieldDescriptor field = parseField(fieldName, fieldValue, classList, name -> ClassUtils.getClassName(name) + "Entity");
            if (field == null) continue;
            fieldList.add(field);
        }
        return fieldList;
    }

    @Nullable
    private J2DFieldDescriptor parseField(@NotNull String key, @NotNull Object value,
                                          @NotNull List<ClassEntity> classList,
                                          @NotNull ClassNameFactory classNameFactory) {
        if (value instanceof String) {
            return J2DFieldDescriptor.prime(key, "String");
        } else if (value instanceof Boolean) {
            return J2DFieldDescriptor.prime(key, "bool");
        } else if (value instanceof Integer || value instanceof Double || value instanceof Float) {
            return J2DFieldDescriptor.prime(key, "num");
        } else if (value instanceof JSONObject) {
            String className = getClassName(key, classNameFactory, classList);
            createAndSaveClass(className, (JSONObject) value, classList);
            return J2DFieldDescriptor.object(key, className);
        } else if (value instanceof JSONArray) {
            J2DFieldDescriptor argumentFieldDescriptor = null;
            if (((JSONArray) value).length() > 0) {
                Object item = ((JSONArray) value).get(0);
                argumentFieldDescriptor = parseField(key, item, classList, name -> ClassUtils.getClassName(name) + "ItemEntity");
            }
            return J2DFieldDescriptor.list(key, argumentFieldDescriptor);
        }
        return null;
    }

    private String getClassName(@NotNull String name,
                                @NotNull ClassNameFactory factory,
                                @NotNull List<ClassEntity> classList) {
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
}
