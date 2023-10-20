1、往 `指定的目录` 添加文件，如果该文件后缀为：`ttf`, `font`, `fon`, `otf`, `eot`, `woff`, `ttc`，则 `iFlutter` 会视该文件为字体文件，便自动注册自定义字体到 `pubspec.yaml` 中，同时创建或修改 `lib/res/i_font_res.dart` 以生成引用字段。

- <font color="#ff0000">**注意**</font>: 如果项目已经有自定义字体配置，**首次使用 `iFlutter` 需要确保**，`family:xxx` 和字体文件名(不包括后缀)一致，例如：

```
flutter:
  ...
  fonts:
    - family: a
      fonts:
        - asset: relative/path/b.ttf
```
则需要修改 `relative/path/b.ttf` 文件名为 `a.ttf`

2、对于字体变体类型的注册，请参考以下字体文件的命名规则，如：

```
flutter:
  fonts:
    - family: font
      fonts:
        - asset: assets/fonts/font.ttf
        - asset: assets/fonts/font@weight_500.ttf
          weight: 500
```

- <font color="#ff0000">**规则**</font>: **字体族名称**@**key1_value1_key2_value2...**[ttf|font...]
