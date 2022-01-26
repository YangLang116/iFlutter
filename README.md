<!-- Plugin description -->
iFlutter is an IDEA Plugin for Developer of Flutter
<!-- Plugin description end -->

## [IDEA插件下载地址](https://github.com/YangLang116/iFlutter/tree/main/plugin-version)
> 下载最新版本的zip文件，千万不要解压，直接拖拽到 AndroidStudio 中即可安装。


## iFlutter生态
- 欢迎PR、issues、advice，一起共建Flutter生态
- 减少无效工作，腾出时间更好的摸鱼！

## 功能说明
- 资源实时注册
- R资源重置
- 检查是否存在重复资源
- `Json` 转 `Dart Entity`
- 一键生成 `fromJson`、`toJson` 方法
- 提供 *HTTP MOCK*
- 支持对资源目录文件一键归类
- 内置常用的 `Live Template`

---
## 资源实时注册
### 背景：
在Flutter开发过程中，我们可能需要为App添加素材文件，比如我们用Flutter开发了一个登录界面，
在界面中需要有一张登录背景图，按照Flutter开发流程，我们需要进行如下步骤:

- 在项目根目录创建 `images` 文件夹，并把 `bg_login.png` 添加到文件夹中
- 修改 `pubspec.yaml` 文件，用于资源的注册
```
···
flutter:
  uses-material-design: true
  assets:
    - images/bg_login.png    #注册资源    
···  
```
- 使用资源
```
Image.asset('images/bg_login');
```

### 痛点:
- **存在繁琐的机械劳动** : 向 `pubspec.yaml` 文件中配置资源
- **整个工作流成本高** : 资源的配置和使用都是以字符串的形式存在，任何一个环节配置错误，功能都无法正常使用

### iFlutter解决方案:
当在 `指定的目录` 中添加、删除、重命名文件时，`iFlutter` 插件都会感知，并自动修改 `pubspec.yaml` 文件，同时生成配套的 `_res.dart文件(类似Android中的R文件)`，通过 `R.xx` 的方式就能使用资源，效果如下:  

![资源联动动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/dynamic_res.gif)

### 补充说明
- 上一节说的 `指定的目录`，并不是 `iFlutter` 所固定要求的，开发者可自行配置，默认 `images`、 `assets`，如下图：  

![资源联动配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_gen_r.png)


- 生成的 `_res.dart` 文件的规则又是什么呢？ 如果 `指定的目录` 是 `images`，那么就会生成 `lib/res/images_res.dart` 文件，对应的类名 `ImagesRes`，按此类推，如果目录名是 `Assets`，生成的文件和类名分别是 `lib/res/assets_res.dart` 和 `AssetsRes`。

- 值得一提的是，如果开发者手动修改 `pubspec.yaml` 文件中资源的配置，保存以后，`iFlutter` 也会感知，并重新生成 `_res.dart` 文件。

- 从 `节省包体积`、`照顾强迫症研发` 出发，可配置某些资源不会在 `_res.dart` 文件中生成字段，比如 `.ttf`、`.json` 文件，默认都生成，开发者可自行配置：

![资源联动配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_ignore_field.png)

- **需要注意**，项目中添加资源时，文件名中不要存在 `-`，比如 `bg-login.png`，否则 `pubsepec.yaml` 文件生成会受到影响，可使用 `bg_login.png` 代替。

- **重点！！！**，如果AndroidStudio安装了 `iFlutter` 插件，此功能默认打开，如果想禁用此功能，可配置关闭，配置如下:

![资源联动配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_enable_psi.png)

---

## R资源重置

### 背景
当项目开发接近尾期，不同分支代码需要 `Merge Code` 。但由于 `_res.dart` 文件是自动生成的，可能会出现`Merge Conflict` 问题 (通常情况下，利用AndroidStudio冲突解决魔法棒工具能很好处理)。 `iFlutter` 为了资源更好的同步，提供了重新生成资源文件的功能入口：

![资源重新生成](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_gen_res.png)

### 补充说明
`iFlutter` 重新生成资源拆分为如下两个操作:
- 将 `指定目录` 资源重新注册到 `pubsepec.yaml` 中
- 重新生成 `_res.dart` 文件

---

## 检查是否存在重复资源

在项目版本需求开发周期内，同一个设计资源可能被不同的开发小伙伴重复引入，导致包体积增涨。`iFlutter` 支持检查项目中是否存在重复资源，效果如下：

![重复资源检查动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/res_duplicate.gif)


---

## `Json` 转 `Dart Entity`

### 背景
在Flutter开发过程中，客户端通过HTTP请求服务端数据。一般情况下，服务端都是以 `json` 字符串的形式下发，Flutter因为不支持运行时反射，所以不存在类似`Gson`、`YYModel` 的库。对于 `json` 字符串的解析，如果数据不复杂，手动编写 `序列化` 和 `反序列` 的代码倒不麻烦，而复杂的数据手动编写显然不现实，除了使用 `json_serializable`, 还可以借助工具自动生成对应的 `Dart Entity`，而 `iFlutter` 就具备这样的能力。

### 说明
在项目的 `lib` 目录及其 `子目录` 下，`iFlutter` 支持 `Json` 转 `Dart Entity` 功能，在其他目录下该功能不可用，使用效果如下：

![J2D动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/gen_entity.gif)

### 补充说明
- 默认生成的 `Dart Entity` 是支持 `空安全` 的，如果项目还没适配到 `Flutter2.x` 版本，通过修改 `iFlutter` 配置即可，配置如下：

![J2D配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_flutter_2.png)

- **着重说明**：填写类名时，采用驼峰命名法。

---
## 一键生成 `fromJson` 、`toJson` 方法

### 背景
对于新的实体类，可以通过上一节 `Json 转 Dart Entity` 工具自动生成。针对项目中已有的类，那我们又该什么生成 `toJson` 和 `fromJson` 方法呢？先看使用效果：

![Gen动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/generate_to_from_json.gif)

### 补充说明
- 官方插件 `Dart` 已经提供了生成 `Constructor`、`Named Constructor` 和 `toString`方法，而 `iFlutter` 的 `fromJson` 和 `toJson` 正好加强了官方对类的 `fix(补全)` 吧。

---

## HTTP MOCK

### 背景
在开发新的需求时，如果功能设计到网络请求时，我们都会事先跟服务端小伙伴约定好HTTP协议，然后再分头开发。如果 `客户端` 开发完功能后，服务端小伙伴还没时间联调，那么`客户端`的小伙伴，就需要自己考虑接口Mock了，而 `iFlutter` 的 `HTTP MOCK` 也由此而生。

### 功能使用
![HTTP MOCK 动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/http-mock.gif)

### 说明
- `iFlutter` 开启的 `HTTP Mock`与项目绑定，换一句话说，对于同一个HTTP URL PATH `/v1/test` 请求响应数据可以不同，MOCK 数据以项目为维度进行隔离。

---

## 资源一键归类

### 背景
随着Flutter项目的不断迭代，项目中所使用的资源也会越来越多。大部分的情况下，项目所使用的资源文件都是直接平展的放在资源目录下，类似:
```
images
  - login_wx.png
  - login_qq.png
  - login_phone.png
  - mine_setting.png
  - mine_defailt_portrait.png
  - launcher.png
  ...
```

## 说明

为了方便项目模块化，`iFlutter` 支持对目录下的文件进行归类，以 `_` 为规则进行分类，分类结果如下:
```
images
  - login
    - login_wx.png
    - login_qq.png
    - login_phone.png
  - mine
    - mine_setting.png
    - mine_defailt_portrait.png
  - launcher.png
  ...
```

资源归类后，`iFlutter` 会自动调整 `pubspec.yaml` 中对资源的注册，以及所有使用该资源的 `*.dart`文件。

**重点说明**: 并非所有目录都支持资源归类，默认 `images` 目录，开发者可自行调整，具体如下:

![Res Category 配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_opt_category.png)

## 使用效果
![Res Category 动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/cate_res.gif)

---
## 内置常用的 `Live Template`

## 使用效果
![Live Template 动效](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/live_code.gif)

## 使用说明
快捷键 | 代码片段
:--: | :--:
importM | `import 'package:flutter/material.dart`;
importC | `import 'package:flutter/cupertino.dart`;
f_Column | `Column(...)`
f_Container | `Container(...)`
f_GestureDetector | `GestureDetector(...)`
f_Row | `Row(...)`
f_Stack |  `Stack(...)`
f_Text | `Text(...)`

## 具体代码细节
![Live Template 配置](https://raw.githubusercontent.com/YangLang116/iFlutter/main/configs/config_live_template.png)
