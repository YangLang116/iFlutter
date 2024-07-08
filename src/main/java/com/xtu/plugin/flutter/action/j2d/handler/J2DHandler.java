package com.xtu.plugin.flutter.action.j2d.handler;

import com.xtu.plugin.flutter.action.j2d.handler.entity.ClassEntity;
import com.xtu.plugin.flutter.action.j2d.handler.entity.FieldDescriptor;
import com.xtu.plugin.flutter.utils.ClassUtils;
import com.xtu.plugin.flutter.utils.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class J2DHandler {

    private final boolean nullSafety;
    private final boolean isUnModifiableFromJson;

    public J2DHandler(boolean nullSafety, boolean isUnModifiableFromJson) {
        this.nullSafety = nullSafety;
        this.isUnModifiableFromJson = isUnModifiableFromJson;
    }

    public static String formatJson(@NotNull String jsonData) throws JSONException {
        if (jsonData.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonData);
            return jsonArray.toString(4);
        } else {
            JSONObject jsonObject = new JSONObject(jsonData);
            return jsonObject.toString(4);
        }
    }

    @NotNull
    public String genCode(@NotNull String className, @NotNull String jsonData) {
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
        StringBuilder classInfoBuilder = new StringBuilder();
        classInfoBuilder.append("class ").append(className).append(" {");

        List<FieldDescriptor> fieldList = parseFieldList(jsonObject, classList);
        if (!fieldList.isEmpty()) {
            classInfoBuilder.append(createFieldInfo(fieldList));
            classInfoBuilder.append(createConstructorInfo(className, fieldList));
            classInfoBuilder.append(createFromJsonInfo(className, fieldList));
            classInfoBuilder.append(createToJsonInfo(fieldList));
        }
        classInfoBuilder.append(" }");
        classList.add(new ClassEntity(className, classInfoBuilder.toString()));
    }


    @NotNull
    private String createFieldInfo(@NotNull List<FieldDescriptor> fieldList) {
        StringBuilder infoBuilder = new StringBuilder();
        for (FieldDescriptor field : fieldList) {
            if (field.isList && field.subType != null) {
                String formatTemplate = nullSafety ? "final List<%s>? %s;" : "final List<%s> %s;";
                infoBuilder.append(String.format(Locale.ROOT, formatTemplate, field.subType.type, field.displayName));
            } else {
                String formatTemplate = nullSafety ? "final %s? %s;" : "final %s %s;";
                infoBuilder.append(String.format(Locale.ROOT, formatTemplate, field.type, field.displayName));
            }
        }
        return infoBuilder.toString();
    }

    @NotNull
    private String createConstructorInfo(@NotNull String className, @NotNull List<FieldDescriptor> fieldList) {
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append(className).append("({");
        for (FieldDescriptor field : fieldList) {
            infoBuilder.append("this.").append(field.displayName).append(",");
        }
        infoBuilder.append("});");
        return infoBuilder.toString();
    }

    @NotNull
    private String createFromJsonInfo(@NotNull String className, @NotNull List<FieldDescriptor> fieldList) {
        StringBuilder infoBodyBuilder = new StringBuilder();
        infoBodyBuilder.append(className).append("(");
        for (FieldDescriptor field : fieldList) {
            if (field.isPrime) {
                infoBodyBuilder.append(String.format(Locale.ROOT,
                        "%s: json['%s'],",
                        field.displayName, field.key));
            } else if (field.isList) {
                if (field.subType != null && field.subType.isObject) {
                    String structWord = isUnModifiableFromJson ? "unmodifiable" : "from";
                    infoBodyBuilder.append(String.format(Locale.ROOT,
                            """
                                    %s: json['%s'] == null? \
                                             null : \
                                             List<%s>.%s(json['%s'].map((x) => %s.fromJson(x))),
                                    """,
                            field.displayName, field.key,
                            field.subType.type, structWord, field.key, field.subType.type));
                } else {
                    infoBodyBuilder.append(String.format(Locale.ROOT,
                            "%s: json['%s'],",
                            field.displayName, field.key));
                }
            } else if (field.isObject) {
                infoBodyBuilder.append(String.format(Locale.ROOT,
                        "%s: json['%s'] == null? null : %s.fromJson(json['%s']),",
                        field.displayName, field.key, field.type, field.key));
            }
        }
        infoBodyBuilder.append(");");
        return String.format(Locale.ROOT,
                "factory %s.fromJson(Map<String, dynamic> json) { return %s }",
                className, infoBodyBuilder);
    }

    @NotNull
    private String createToJsonInfo(@NotNull List<FieldDescriptor> fieldList) {
        StringBuilder infoBodyBuilder = new StringBuilder();
        for (FieldDescriptor field : fieldList) {
            infoBodyBuilder.append(String.format(Locale.ROOT, "'%s': ", field.displayName));
            if (field.isList && field.subType != null && field.subType.isObject) {
                String formatTemplate = nullSafety ? "%s?.map((e) => e.toJson()).toList()," : "%s.map((e) => e.toJson()).toList(),";
                infoBodyBuilder.append(String.format(Locale.ROOT, formatTemplate, field.displayName));
            } else if (field.isObject) {
                String formatTemplate = nullSafety ? "%s?.toJson()," : "%s.toJson(),";
                infoBodyBuilder.append(String.format(Locale.ROOT, formatTemplate, field.displayName));
            } else {
                infoBodyBuilder.append(field.displayName).append(",");
            }
        }
        return String.format(Locale.ROOT,
                "Map<String, dynamic> toJson() => { %s };",
                infoBodyBuilder
        );
    }


    @NotNull
    private List<FieldDescriptor> parseFieldList(@NotNull JSONObject jsonObject, @NotNull List<ClassEntity> classList) {
        List<FieldDescriptor> fieldList = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Set<String> fieldSet = jsonObject.keySet();
        for (String fieldName : fieldSet) {
            Object fieldValue = jsonObject.get(fieldName);
            FieldDescriptor field = parseField(fieldName, fieldValue, classList, name -> ClassUtils.getClassName(name) + "Entity");
            if (field == null) continue;
            fieldList.add(field);
        }
        return fieldList;
    }

    @Nullable
    private FieldDescriptor parseField(@NotNull String key, @NotNull Object value,
                                       @NotNull List<ClassEntity> classList,
                                       @NotNull ClassNameFactory classNameFactory) {
        if (value instanceof String) {
            return FieldDescriptor.prime(key, "String");
        } else if (value instanceof Boolean) {
            return FieldDescriptor.prime(key, "bool");
        } else if (value instanceof Integer || value instanceof Double || value instanceof Float) {
            return FieldDescriptor.prime(key, "num");
        } else if (value instanceof JSONObject) {
            String className = getClassName(key, classNameFactory, classList);
            createAndSaveClass(className, (JSONObject) value, classList);
            return FieldDescriptor.object(key, className);
        } else if (value instanceof JSONArray) {
            FieldDescriptor argumentFieldDescriptor = null;
            if (((JSONArray) value).length() > 0) {
                Object item = ((JSONArray) value).get(0);
                argumentFieldDescriptor = parseField(key, item, classList, name -> ClassUtils.getClassName(name) + "ItemEntity");
            }
            return FieldDescriptor.list(key, argumentFieldDescriptor);
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

    public static void main(String[] args) {
        J2DHandler handler = new J2DHandler(true, true);
        String result = handler.genCode("Test", "{\"name\":\"YangLang\",\"age\":24,\"male\":true,\"school\":{\"name\":\"xtu\",\"address\":\"测试地点\"},\"likes\":[1,2],\"friend\":[{\"name\":\"friend1\",\"age\":25},{\"name\":\"friend1\",\"age\":26}]}");
        System.out.println(result);
    }
}
