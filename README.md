<!-- Plugin description -->
iFlutter is an IDEA Plugin for Developer of Flutter
<!-- Plugin description end -->

## iFlutter是一款辅助Flutter开发的 IDEA 插件

![platforms](https://img.shields.io/badge/platforms-macos%20%7C%20windows%20%7C%20linux-blue) ![tools](https://img.shields.io/badge/idea-intellij_IDEA%20%7C%20AndroidStudio-blue) ![licence](https://img.shields.io/badge/licence-MIT-blue) ![version](https://img.shields.io/badge/version-v2.1.2-blue)

**插件改为最低兼容211版本，如遇到无法安装，请升级AndroidStudio**

## 最新公告

- [MediaFilePreviewer](https://github.com/YangLang116/MediaFilePreviewer): 一款支持Lottie、SVGA预览的IDEA插件  
插件安装：插件已上传官方Plugins仓库，可搜索 `MediaFilePreviewer` 下载  
使用文档：[点击跳转](https://iflutter.toolu.cn/content/chapter-10/part-1.html)

- [GameCenter](https://github.com/YangLang116/GameCenter):支持IDEA中展示游戏菜单  
插件安装：插件已上传官方Plugins仓库，可搜索 `GameCenter` 下载  
使用文档：[点击跳转](https://iflutter.toolu.cn/content/chapter-11/part-1.html)

## 说明

- 插件安装：插件已上传官方Plugins仓库，可搜索 `iFlutter` 下载
- Github: [点击跳转](https://github.com/YangLang116/iFlutter)
- 详细文档: [点击跳转](https://iflutter.toolu.cn)
- 功能说明: 
    - 1、资源文件管理
    - 2、依赖树生成a
    - 3、Dart代码生成
    - 4、Http接口Mock
    - 5、包检查更新
    - 6、提取远程依赖
    - 7、Pub快捷搜索
    - 8、依赖快速定位
    - 9、支持国际化
    - 10、支持镜像仓库注入

## 版本更新
### v2.1.2
- R文件中的资源字段可配置是否携带包名前缀，[使用说明](https://iflutter.toolu.cn/content/chapter-1/part-1.html)

### v2.1.1
- 优化`fromJson`、`toJson`生成逻辑

### v2.1.0
- 生成的R文件中添加'PLUGIN_NAME'、'PLUGIN_VERSION'字段
- 修复fromJson生成失败问题
- 优化镜像仓库注入逻辑

### v2.0.7
- 注入镜像仓库到项目(包括引入的plugin)，解决qiang导致的Timeout问题，优化项目编译速度，[使用说明](https://iflutter.toolu.cn/content/chapter-12/part-1.html)

### v2.0.6
- 支持字体文件变体，[使用说明](https://iflutter.toolu.cn/content/chapter-1/part-3.html)
```yaml
flutter:
  fonts:
    - family: font
      fonts:
        - asset: assets/fonts/font.ttf
        - asset: assets/fonts/font@weight_500.ttf
          weight: 500
```

### v2.0.5
- fix反馈问题失败

### v2.0.4

- 支持新版本检查更新提醒
- 支持一键提交issue到Github
- Configuration界面添加Github信息

### v2.0.3

- 优化json2Dart，兼容驼峰命名key

### v2.0.2

- 优化项目插件版本检查逻辑

### v2.0.1

- 在功能菜单栏中添加使用文档入口

### v2.0.0

- 支持将资源文件拖拽到项目的资源目录上，进行注册资源
- 支持将资源文件直接复制到资源文件夹中，进行注册资源
- 支持同时对多个资源文件进行注册
- 优化若干交互细节

### v1.3.7

- 支持对新增图片进行大小监控 [wiki](https://iflutter.toolu.cn/content/chapter-1/part-8.html)

### v1.3.6

- 国际化添加自动翻译 [wiki](https://iflutter.toolu.cn/content/chapter-9/part-1.html)

### v1.3.5

- 支持国际化 [wiki](https://iflutter.toolu.cn/content/chapter-9/part-1.html)
- 修复i_font_res.dart的Git问题

## 其他

建议直接使用 [Intellij IDEA](https://www.jetbrains.com/idea/) 代替 `AndroidStudio` 开发Flutter项目：  
1、`iFlutter` 在 `Intellij IDEA` 也适用;  
2、`Intellij IDEA`
支持更多的[快捷功能](https://medium.com/flutter-community/flutter-ide-shortcuts-for-faster-development-2ef45c51085b);

## 赞赏(感谢支持)

<img src="https://iflutter.toolu.cn/configs/wx_pay.jpg" width="120"  alt="QQ"/>
