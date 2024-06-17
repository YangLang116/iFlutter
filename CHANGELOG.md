# Changelog

## [未完待续]

###

## [4.1.1] - 2024-06-17
- `Base`: 调整部分UI布局
- `Base`: 优化R资源生成机制
- `Base`: 修复windows平台乱码问题

## [4.0.4] - 2024-05-03
- `Code`: 兼容223以后版本(ActionUpdateThread) 

## [4.0.3] - 2024-04-15
- `Code`: 生成的R资源类构造函数私有化
- `Dart Code`: 优化`LiveTemplate`配置

## [4.0.2] - 2024-04-2
- `Flutter Resource`: 优化图片压缩引导弹窗逻辑
- `Repo Mirror`: 调整镜像配置UI && 拆分镜像注册方式(项目注入、插件注入、Flutter Gradle脚本注入)

## [4.0.1] - 2024-03-12

- `Base`: Bug修复 & 更新文档地址

## [4.0.0] - 2024-01-16

- `Flutter Resource`: 添加资源图片时，新增压缩引导弹窗
- `Code`: 修复以目录的方式注册资源，R文件不更新Bug
- `Base`: 解决切换分支时，图片大小检测弹窗频繁弹出Bug

## [3.0.6] - 2024-01-13

- `Base`: 代码优化

## [3.0.5] - 2023-12-24

- `Base`: 资源管理优化

## [3.0.4] - 2023-11-18

- `Dart Code`: 添加更多`LiveTemplate`片段

## [3.0.3] - 2023-10-09

- `Flutter Resource`: 上下文菜单新增删除功能
- `Flutter Resource`: 支持图片缺省展示
- `HttpMock`: 修复启动失败问题
- `Base`: 其他微小改动

## [3.0.2] - 2023-09-20

- `Dart Code`: 调整代码生成机制
- `Base`:
  修改插件错误处理策略[(LogUtils)](https://github.com/YangLang116/iFlutter/blob/main/src/main/java/com/xtu/plugin/flutter/utils/LogUtils.java)
  ，将错误打印改为上报，所有上报的数据[(AdviceManager)](https://github.com/YangLang116/iFlutter/blob/main/src/main/java/com/xtu/plugin/flutter/advice/AdviceManager.java)
  不涉及任何项目隐私，请放心使用

## [3.0.1] - 2023-09-08

- `Base`: 支持快速清除`Dart`、`YAML`文件中所有注释
- `Base`: Bug修复

## [3.0.0] - 2023-08-17

- `Base`: 优化插件性能 & Bug修复

## [2.2.7] - 2023-08-09

- `Flutter Resource`: 修复Bug
- `Suggestion & Feedback`: 调整错误上报方式

## [2.2.6] - 2023-08-07

- `Flutter Resource`: 更新窗口图标

## [2.2.5] - 2023-07-25

- `Flutter Resource`: Bug修复

## [2.2.4] - 2023-06-20

- `Suggestion & Feedback`: 新增错误与反馈渠道

## [2.2.3] - 2023-04-24

- `Flutter Resource`: 上下文菜单新增图片压缩功能

## [2.2.2] - 2023-04-23

- `Flutter Resource`: 上下文菜单新增图片路径拷贝功能
- `Base`: 一些其他小改动

## [2.2.1] - 2023-04-15

- `Flutter Resource`: 上下文菜单新增搜索功能

## [2.2.0] - 2023-04-13

- `Flutter Resource`: 新增图片资源管理窗口

## [2.1.4] - 2023-02-21

- `Upgrade`: 优化插件引导升级逻辑
- `Code`: 支持生成类构造器代码

## [2.1.3] - 2023-02-18

- `Code`: 优化 `fromJson`、`toJson` 代码生成逻辑
- `Covert Dependency To Local`: 兼容 Flutter 3.0