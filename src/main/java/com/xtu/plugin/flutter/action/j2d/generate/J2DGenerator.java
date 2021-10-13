package com.xtu.plugin.flutter.action.j2d.generate;

import com.xtu.plugin.flutter.utils.StringUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class J2DGenerator {

    private final List<String> classList = new ArrayList<>();

    public String generate(String className, JSONObject json) {
        classList.add(buildClass(className, json));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < classList.size(); i++) {
            if (i != 0) result.append("\n\n");
            result.append(classList.get(i));
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    private String buildClass(String className, JSONObject jsonObject) {
        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("class ").append(className).append(" {");
        List<TypeEntity> typeEntityList = new ArrayList<>();
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) { //添加字段
            TypeEntity typeEntity = makeType(key, jsonObject.get(key), name -> StringUtil.upFirstChar(name) + "Entity");
            if (typeEntity == null) continue;
            if (typeEntity.isList && typeEntity.subType != null) {
                classBuilder.append(String.format(Locale.ROOT, "final List<%s> %s;", typeEntity.subType.type, typeEntity.name));
            } else {
                classBuilder.append(String.format(Locale.ROOT, "final %s %s;", typeEntity.type, typeEntity.name));
            }
            typeEntityList.add(typeEntity);
        }
        if (typeEntityList.size() > 0) {
            //添加构造函数
            StringBuilder constructorFieldsSb = new StringBuilder();
            for (TypeEntity typeEntity : typeEntityList) {
                constructorFieldsSb.append("this.").append(typeEntity.name).append(",");
            }
            classBuilder.append(String.format(
                    Locale.ROOT, "%s({%s});",
                    className, constructorFieldsSb));
            //添加fromJson方法
            StringBuilder fromJsonBodySb = new StringBuilder();
            fromJsonBodySb.append(className).append("(");
            for (TypeEntity typeEntity : typeEntityList) {
                if (typeEntity.isPrime) {
                    fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'],", typeEntity.name, typeEntity.name));
                } else if (typeEntity.isList) {
                    if (typeEntity.subType != null && typeEntity.subType.isObject) {
                        fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'] == null? []: List<%s>.unmodifiable(json['%s'].map((x) => %s.fromJson(x))),",
                                typeEntity.name, typeEntity.name, typeEntity.subType.type, typeEntity.name, typeEntity.subType.type));
                    } else {
                        fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'],", typeEntity.name, typeEntity.name));
                    }
                } else if (typeEntity.isObject) {
                    fromJsonBodySb.append(String.format(Locale.ROOT, "%s: json['%s'] == null? null : %s.fromJson(json['%s']),",
                            typeEntity.name, typeEntity.name, typeEntity.type, typeEntity.name));
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
                toJsonBodySb.append("'").append(typeEntity.name).append("':");
                if (typeEntity.isPrime) {
                    toJsonBodySb.append(typeEntity.name);
                } else if (typeEntity.isList) {
                    if (typeEntity.subType != null && typeEntity.subType.isObject) {
                        toJsonBodySb.append("List<dynamic>.from(").append(typeEntity.name).append(".map((x) => x.toJson()))");
                    } else {
                        toJsonBodySb.append(typeEntity.name);
                    }
                } else if (typeEntity.isObject) {
                    toJsonBodySb.append(typeEntity.name).append(".toJson()");
                }
                toJsonBodySb.append(",");
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
            String className = classNameFactory.create(key);
            classList.add(buildClass(className, (JSONObject) value));
            return TypeEntity.object(key, className);
        } else if (value instanceof JSONArray) {
            TypeEntity argumentTypeEntity = null;
            if (((JSONArray) value).length() > 0) {
                Object item = ((JSONArray) value).get(0);
                argumentTypeEntity = makeType(key, item, name -> StringUtil.upFirstChar(name) + "ItemEntity");
            }
            return TypeEntity.list(key, "List", argumentTypeEntity);
        }
        return null;
    }

    interface ClassNameFactory {
        String create(String key);
    }


    public static void main(String[] args) {
        J2DGenerator generator = new J2DGenerator();
        String result = generator.generate("Test", new JSONObject("{\"name\":\"YangLang\",\"age\":24,\"male\":true,\"school\":{\"name\":\"xtu\",\"address\":\"测试地点\"},\"likes\":[1,2],\"friend\":[{\"name\":\"friend1\",\"age\":25},{\"name\":\"friend1\",\"age\":26}]}"));
        System.out.println(result);
    }
}
