# Changelog

## [Coming]
### Changed

## [2.2.0] - 2023-04-13
### Changed
- support image resource management

## [2.1.4] - 2023-02-21
### Changed
-  optimize upgrade guide
-  support for generating constructors

## [2.1.3] - 2023-02-18
### Changed
- optimize `fromJson`、`toJson`
- fix `covert dependency to local` fail with flutter3.0

## [2.1.2] - 2022-11-24
### Changed
- support register res for Flutter Plugin

## [2.1.1] - 2022-11-23
### Changed
- optimize `fromJson`、`toJson`

## [2.1.0] - 2022-11-20
### Changed
- add 'PLUGIN_NAME'、'PLUGIN_VERSION' to R file
- fix `fromJson` fail
- optimize mirror repo code

## [2.0.7] - 2022-11-09
### Add
- inject the mirror repository into the project (including the imported plugin), to solve the timeout problem caused by qiang, and to optimize the project build speed.

## [2.0.6] - 2022-10-30
### Changed
- support font variant, [more detail](https://iflutter.toolu.cn/content/chapter-1/part-3.html)
```yaml
flutter:
  fonts:
    - family: font
      fonts:
        - asset: assets/fonts/font.ttf
        - asset: assets/fonts/font@weight_500.ttf
          weight: 500
```

## [2.0.5] - 2022-10-19
### Changed
- fix issue feedback fail

## [2.0.4] - 2022-08-15
### Changed
- add description in configuration
- support check for updates
- support submit a question

## [2.0.3] - 2022-05-26
### Changed
- optimize json2dart

## [2.0.2] - 2022-05-07
### Changed
- optimize version checker

## [2.0.1] - 2022-04-29
### Changed
- open document quickly

## [2.0.0] - 2022-04-29
### Changed
- register resources with multi file
- register resources by dragging
- register resources with finder

## [1.3.7] - 2022-04-23
### Changed
- check image size

## [1.3.6] - 2022-04-21
### Changed
- support translate for intl

## [1.3.5] - 2022-04-17
### Changed
- fix Git Question for i_font_res.dart
- add intl support

## [1.3.4] - 2022-04-09
### Changed
- analyze useless asset

## [1.3.3] - 2022-03-23
### Changed
- optimize json2dart

## [1.3.2] - 2022-03-22
### Changed
- optimize version checker

## [1.3.1] - 2022-03-17
### Changed
- fix replace res question

## [1.3.0] - 2022-03-12
### Changed
- register resource with folder

## [1.2.3] - 2022-03-08
### Changed
- support dependency anchor

## [1.2.2] - 2022-03-04
### Changed
- optimize action group
- add pub search

## [1.2.1] - 2022-02-28
### Changed
- optimize psi
- support font asset register

## [1.2.0] - 2022-02-27
### Changed
- compatible dimension

## [1.1.7] - 2022-02-16
### Changed
- convert dependency to local

## [1.1.6] - 2022-02-16
### Changed
- add PluginIcon
- fix Cache File Conflict

## [1.1.5] - 2022-02-14
### Changed
- check latest package version

## [1.1.4] - 2022-02-11
### Changed
- fix file conflict

## [1.1.3] - 2022-02-10
### Changed
- generate dependencies tree

## [1.1.2] - 2022-02-08
### Changed
- compatible windows

## [1.1.1] - 2022-01-26
### Changed
- support check duplicate resource

## [1.1.0] - 2022-01-25
### Changed
- 支持检查项目是否存在重复文件

## [1.0.9] - 2022-01-24
### Changed
- 修复资源一键归类后，目录不刷新问题

## [1.0.8] - 2022-01-24
### Changed
- 支持对Project可选择性开启R文件生成

## [1.0.7] - 2022-01-12
### Changed
- Http Mock 项目隔离
- GenerateR 优化
- yaml文件支持注释

## [1.0.6] - 2021-12-11
### Changed
- 支持Http本地Mock

## [1.0.5] - 2021-12-09
### Changed
- 支持Flutter2.0
- 在项目首选项中添加插件配置项
- 新增手动生成R文件Action

## [1.0.4] - 2021-10-13
### Changed
- 添加LiveTemplate
- 支持资源R文件的生成
- 支持对images目录进行整理
- 支持json转Dart