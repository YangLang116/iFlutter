<!-- Plugin description -->
iFlutter is an IDEA Plugin for Developer of Flutter
<!-- Plugin description end -->

## iFlutter是一款辅助Flutter开发的 IDEA 插件

![platforms](https://img.shields.io/badge/platforms-macos%20%7C%20windows%20%7C%20linux-blue)
![tools](https://img.shields.io/badge/idea-intellij_IDEA%20%7C%20AndroidStudio-blue)
![licence](https://img.shields.io/badge/licence-MIT-blue)
![downloads](https://img.shields.io/jetbrains/plugin/d/18457)
![version](https://img.shields.io/jetbrains/plugin/v/18457)

**插件改为最低兼容211版本，如遇到无法安装，请升级AndroidStudio**

## 最新公告

- [MediaFilePreviewer](https://github.com/YangLang116/MediaFilePreviewer): 一款支持Lottie、SVGA预览的IDEA插件  
  插件安装：插件已上传官方Plugins仓库，可搜索 `MediaFilePreviewer` 下载  
  使用文档：[点击跳转](https://iflutter.toolu.cn/content/chapter-7/part-1.html)

- [GameCenter](https://github.com/YangLang116/GameCenter):支持IDEA中展示游戏菜单  
  插件安装：插件已上传官方Plugins仓库，可搜索 `GameCenter` 下载  
  使用文档：[点击跳转](https://iflutter.toolu.cn/content/chapter-8/part-1.html)

- [Pub](https://pub.dev/publishers/iflutter.toolu.cn/packages): 更多Flutter Plugin插件 ~

## 插件安装

- 插件安装：插件已上传官方Plugins仓库，可搜索 `iFlutter` 下载
- Github: [点击跳转](https://github.com/YangLang116/iFlutter)
- 详细文档: [点击跳转](https://iflutter.toolu.cn)

## 功能说明

- 1、资源文件管理
    - [注册资源](https://iflutter.toolu.cn/content/chapter-1/part-1.html)
    - [注册资源(目录)](https://iflutter.toolu.cn/content/chapter-1/part-2.html)
    - [注册字体](https://iflutter.toolu.cn/content/chapter-1/part-3.html)
    - [资源重置](https://iflutter.toolu.cn/content/chapter-1/part-4.html)
    - [检查重复资源](https://iflutter.toolu.cn/content/chapter-1/part-5.html)
    - [归纳资源](https://iflutter.toolu.cn/content/chapter-1/part-6.html)
    - [无用资源检查](https://iflutter.toolu.cn/content/chapter-1/part-7.html)
    - [图片资源监控](https://iflutter.toolu.cn/content/chapter-1/part-8.html)
    - [图片资源管理](https://iflutter.toolu.cn/content/chapter-1/part-9.html)
- 2、代码生成
    - [Json 转 Dart Entity](https://iflutter.toolu.cn/content/chapter-2/part-1.html)
    - [fromJson、toJson](https://iflutter.toolu.cn/content/chapter-2/part-2.html)
    - [Live Template](https://iflutter.toolu.cn/content/chapter-2/part-3.html)
- 3、插件包管理
    - [Pub快捷搜索](https://iflutter.toolu.cn/content/chapter-3/part-1.html)
    - [依赖快速定位](https://iflutter.toolu.cn/content/chapter-3/part-2.html)
    - [提取远程依赖](https://iflutter.toolu.cn/content/chapter-3/part-3.html)
    - [包检查更新](https://iflutter.toolu.cn/content/chapter-3/part-4.html)
    - [依赖树生成](https://iflutter.toolu.cn/content/chapter-3/part-5.html)
- [4、Http接口Mock](https://iflutter.toolu.cn/content/chapter-4/part-1.html)
- [5、国际化支持](https://iflutter.toolu.cn/content/chapter-5/part-1.html)
- [6、镜像仓库注入](https://iflutter.toolu.cn/content/chapter-6/part-1.html)
- [7、清除注释](https://iflutter.toolu.cn/content/chapter-11/part-1.html)
- [8、意见与反馈](https://iflutter.toolu.cn/content/chapter-10/part-1.html)

## 问题反馈

  <img src="https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/iflutter_wechat.png" width="120"  alt="wx chat"/>

## 版本更新记录

### v3.0.3

- 优化`资源管理`窗口图片预览效果，新增删除菜单
- 修复`HttpMock`偶现启动失败问题
- 处理Bug

### v3.0.2

- 调整Dart代码生成机制
- 修改插件错误处理策略[(LogUtils)](https://github.com/YangLang116/iFlutter/blob/main/src/main/java/com/xtu/plugin/flutter/utils/LogUtils.java)，将错误打印改为上报，所有上报的数据[(AdviceManager)](https://github.com/YangLang116/iFlutter/blob/main/src/main/java/com/xtu/plugin/flutter/advice/AdviceManager.java)不涉及任何项目隐私，请放心使用

### v3.0.1

- 支持快速清除`Dart`、`YAML`文件中所有注释 [详细内容](https://iflutter.toolu.cn/content/chapter-11/part-1.html)

### v3.0.0

- 提升插件性能
- 优化资源释放

### v2.2.7

- 修复`资源管理`窗口启动异常

## 其他

建议直接使用 [Intellij IDEA](https://www.jetbrains.com/idea/) 代替 `AndroidStudio` 开发Flutter项目：  
1、`iFlutter` 在 `Intellij IDEA` 也适用;  
2、`Intellij IDEA`
支持更多的[快捷功能](https://medium.com/flutter-community/flutter-ide-shortcuts-for-faster-development-2ef45c51085b);
