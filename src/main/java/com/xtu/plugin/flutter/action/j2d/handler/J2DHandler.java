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
import template.PluginTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class J2DHandler {

    private final Project project;

    public J2DHandler(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    public String genCode(@NotNull String className, @NotNull String jsonData, boolean keepComment) throws JSONException {
        List<ClassEntity> classList = new ArrayList<>();
        JSONObject orderObj = JsonUtils.createOrderObj(jsonData);
        createAndSaveClass(className, orderObj, keepComment, classList);

        StringBuilder fileContentBuilder = new StringBuilder();
        for (int i = classList.size() - 1; i >= 0; i--) {
            fileContentBuilder.append(classList.get(i).content);
            if (i != 0) fileContentBuilder.append("\n\n");
        }
        return fileContentBuilder.toString();
    }

    private void createAndSaveClass(@NotNull String className,
                                    @NotNull JSONObject jsonObject,
                                    boolean keepComment,
                                    @NotNull List<ClassEntity> classList) {
        List<J2DFieldDescriptor> fieldList = parseFieldList(jsonObject, classList, keepComment);
        String comment = getComment(jsonObject, keepComment);
        String content = PluginTemplate.getJ2DContent(project, className, comment, fieldList);
        classList.add(new ClassEntity(className, content));
    }

    @NotNull
    private List<J2DFieldDescriptor> parseFieldList(@NotNull JSONObject jsonObject,
                                                    @NotNull List<ClassEntity> classList,
                                                    boolean keepComment) {
        List<J2DFieldDescriptor> fieldList = new ArrayList<>();
        Set<String> fieldSet = jsonObject.keySet();
        for (String fieldName : fieldSet) {
            Object fieldValue = jsonObject.get(fieldName);
            J2DFieldDescriptor field = parseField(fieldName, fieldValue, keepComment, classList, name -> ClassUtils.getClassName(name) + "Entity");
            if (field == null) continue;
            fieldList.add(field);
        }
        return fieldList;
    }

    @Nullable
    private J2DFieldDescriptor parseField(@NotNull String key, @NotNull Object value,
                                          boolean keepComment,
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
            createAndSaveClass(className, (JSONObject) value, keepComment, classList);
            return J2DFieldDescriptor.object(key, className);
        } else if (value instanceof JSONArray) {
            J2DFieldDescriptor argumentFieldDescriptor = null;
            if (!((JSONArray) value).isEmpty()) {
                Object item = ((JSONArray) value).get(0);
                argumentFieldDescriptor = parseField(key, item, keepComment, classList, name -> ClassUtils.getClassName(name) + "ItemEntity");
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

    @Nullable
    private String getComment(@NotNull JSONObject obj, boolean keepComment) {
        if (!keepComment) {
            return null;
        }
        JSONObject tempObj = new JSONObject();
        for (String key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                continue;
            }
            if (value instanceof JSONArray && !((JSONArray) value).isEmpty() && ((JSONArray) value).get(0) instanceof JSONObject) {
                continue;
            }
            tempObj.put(key, value);
        }
        String[] lines = tempObj.toString(4).split("\n");
        StringBuilder commentSb = new StringBuilder();
        for (String line : lines) {
            commentSb.append("// ").append(line).append("\n");
        }
        return commentSb.toString();
    }
}
