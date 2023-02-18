package com.xtu.plugin.flutter.action.j2d.generate;

import com.xtu.plugin.flutter.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class J2DGenerator {

    private final boolean enableFlutter2;
    private final List<ClassEntity> classList = new ArrayList<>();

    public J2DGenerator(boolean enableFlutter2) {
        this.enableFlutter2 = enableFlutter2;
    }

    public String generate(String className, JSONObject json) {
        String classContent = buildClass(className, json);
        classList.add(new ClassEntity(className, classContent));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < classList.size(); i++) {
            if (i != 0) result.append("\n\n");
            result.append(classList.get(i).content);
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private String buildClass(String className, JSONObject jsonObject) {
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("class ").append(className).append(" {");
        List<TypeEntity> typeEntityList = new ArrayList<>();
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            //添加字段
            TypeEntity typeEntity = makeType(key, jsonObject.get(key), name -> StringUtil.getClassName(name) + "Entity");
            if (typeEntity == null) continue;
            if (typeEntity.isList && typeEntity.subType != null) {
                String formatTemplate = enableFlutter2 ? "final List<%s>? %s;" : "final List<%s> %s;";
                classBuilder.append(String.format(Locale.ROOT, formatTemplate, typeEntity.subType.type, typeEntity.displayName));
            } else {
                String formatTemplate = enableFlutter2 ? "final %s? %s;" : "final %s %s;";
                classBuilder.append(String.format(Locale.ROOT, formatTemplate, typeEntity.type, typeEntity.displayName));
            }
            typeEntityList.add(typeEntity);
        }
        if (typeEntityList.size() > 0) {
            //添加构造函数
            StringBuilder constructorFieldsSb = new StringBuilder();
            for (TypeEntity typeEntity : typeEntityList) {
                constructorFieldsSb.append("this.").append(typeEntity.displayName).append(",");
            }
            classBuilder.append(String.format(
                    Locale.ROOT, "%s({%s});",
                    className, constructorFieldsSb));
            //添加fromJson方法
            StringBuilder fromJsonBodySb = new StringBuilder();
            fromJsonBodySb.append(className).append("(");
            for (TypeEntity typeEntity : typeEntityList) {
                if (typeEntity.isPrime) {
                    fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'],", typeEntity.displayName, typeEntity.key));
                } else if (typeEntity.isList) {
                    if (typeEntity.subType != null && typeEntity.subType.isObject) {
                        fromJsonBodySb.append(String.format(Locale.ROOT,
                                "%s: json['%s'] == null? " +
                                        "List<%s>.unmodifiable([]): " +
                                        "\nList<%s>.unmodifiable(\njson['%s'].map((x) => %s.fromJson(x))),",
                                typeEntity.displayName, typeEntity.key,
                                typeEntity.subType.type,
                                typeEntity.subType.type, typeEntity.key, typeEntity.subType.type));
                    } else {
                        fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'],", typeEntity.displayName, typeEntity.key));
                    }
                } else if (typeEntity.isObject) {
                    fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'] == null? null : %s.fromJson(json['%s']),",
                            typeEntity.displayName, typeEntity.key, typeEntity.type, typeEntity.key));
                }
            }
            fromJsonBodySb.append(");");
            classBuilder.append(String.format(
                    Locale.ROOT,
                    "factory %s.fromJson(Map<String, dynamic> json) {return %s}",
                    className, fromJsonBodySb));
            //添加toJson方法
            StringBuilder toJsonBodySb = new StringBuilder();
            for (TypeEntity typeEntity : typeEntityList) {
                toJsonBodySb.append("'").append(typeEntity.key).append("'").append(":");
                if (typeEntity.isList && typeEntity.subType != null && typeEntity.subType.isObject) {
                    String formatTemplate = enableFlutter2 ? "%s?.map((e) => e.toJson()).toList()" : "%s.map((e) => e.toJson()).toList()";
                    toJsonBodySb.append(String.format(Locale.ROOT, formatTemplate, typeEntity.displayName)).append(",");
                } else if (typeEntity.isObject) {
                    String formatTemplate = enableFlutter2 ? "%s?.toJson()" : "%s.toJson()";
                    toJsonBodySb.append(String.format(Locale.ROOT, formatTemplate, typeEntity.displayName)).append(",");
                } else {
                    toJsonBodySb.append(typeEntity.displayName).append(",");
                }
            }
            classBuilder.append(String.format(
                    Locale.ROOT,
                    "Map<String, dynamic> toJson() => { %s };",
                    toJsonBodySb
            ));
        }
        classBuilder.append(" }");
        return classBuilder.toString();
    }

    private TypeEntity makeType(String key, Object value, ClassNameFactory classNameFactory) {
        if (value instanceof String) {
            return TypeEntity.prime(key, "String");
        } else if (value instanceof Boolean) {
            return TypeEntity.prime(key, "bool");
        } else if (value instanceof Integer || value instanceof Double || value instanceof Float) {
            return TypeEntity.prime(key, "num");
        } else if (value instanceof JSONObject) {
            String className = getClassName(classNameFactory, key);
            String classContent = buildClass(className, (JSONObject) value);
            classList.add(new ClassEntity(className, classContent));
            return TypeEntity.object(key, className);
        } else if (value instanceof JSONArray) {
            TypeEntity argumentTypeEntity = null;
            if (((JSONArray) value).length() > 0) {
                Object item = ((JSONArray) value).get(0);
                argumentTypeEntity = makeType(key, item, name -> StringUtil.getClassName(name) + "ItemEntity");
            }
            return TypeEntity.list(key, argumentTypeEntity);
        }
        return null;
    }

    private String getClassName(ClassNameFactory factory, String name) {
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
        J2DGenerator generator = new J2DGenerator(true);
        String result = generator.generate("Test", new JSONObject("{\"name\":\"YangLang\",\"age\":24,\"male\":true,\"school\":{\"name\":\"xtu\",\"address\":\"测试地点\"},\"likes\":[1,2],\"friend\":[{\"name\":\"friend1\",\"age\":25},{\"name\":\"friend1\",\"age\":26}]}"));
        System.out.println(result);
    }
}
