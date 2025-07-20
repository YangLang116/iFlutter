# 注册字体

## 概述

`iFlutter` 提供了智能的字体资源管理功能，能够自动识别字体文件并完成注册配置。

## 🔤 自动识别字体文件

当向指定目录添加字体文件时，`iFlutter` 会自动识别以下格式的字体文件：

| 支持的字体格式 |
|-------------|
| `.ttf` |
| `.font` |
| `.fon` |
| `.otf` |
| `.eot` |
| `.woff` |
| `.ttc` |

## ⚙️ 自动化处理

识别到字体文件后，`iFlutter` 会自动执行以下操作：

1. **自动注册**：将字体配置添加到 `pubspec.yaml` 中
2. **生成代码**：创建或更新 `lib/res/i_font_res.dart` 文件，生成引用字段

## ⚠️ 重要注意事项

### 字体文件命名规则

如果项目已有自定义字体配置，**首次使用 `iFlutter` 时需要确保 `family` 名称与字体文件名（不包括后缀）一致**。

**示例：**

```yaml
flutter:
  fonts:
    - family: a
      fonts:
        - asset: relative/path/b.ttf  # ❌ 错误：文件名与 family 不一致
```

**需要修改为：**

```yaml
flutter:
  fonts:
    - family: a
      fonts:
        - asset: relative/path/a.ttf  # ✅ 正确：文件名与 family 一致
```

## 🎨 字体变体支持

### 命名规则

对于字体变体（如不同粗细），请按照以下规则命名字体文件：

**格式：** `字体族名称@key1_value1_key2_value2...[.ttf|.font...]`

### 配置示例

```yaml
flutter:
  fonts:
    - family: font
      fonts:
        - asset: assets/fonts/font.ttf                    # 常规字体
        - asset: assets/fonts/font@weight_500.ttf         # 中等粗细
          weight: 500
        - asset: assets/fonts/font@weight_700.ttf         # 粗体
          weight: 700
        - asset: assets/fonts/font@style_italic.ttf       # 斜体
          style: italic
```

### 常用变体参数

| 参数 | 说明 | 示例值 |
|------|------|--------|
| `weight` | 字体粗细 | `100`, `300`, `400`, `500`, `700`, `900` |
| `style` | 字体样式 | `normal`, `italic` |

> 💡 **提示**：通过规范的命名方式，`iFlutter` 可以自动识别并正确配置字体变体。
