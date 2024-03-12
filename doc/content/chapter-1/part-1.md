Flutter开发过程中，不可避免地需要使用到本地资源。比如：使用 `bg_login.png` 文件作为背景图，开发一个登录界面，步骤如下：  
1. 在项目根目录创建 `images` 文件夹，并把 `bg_login.png` 添加到 `images`;
2. 注册资源到 `pubspec.yaml`：
```
···
flutter:
  uses-material-design: true
  assets:
    - images/bg_login.png    #注册资源    
···  
```
3. 使用资源：
```
Image.asset('images/bg_login');
```

使用资源前都需要在 `pubspec.yaml` 文件中注册，并且通过 `硬编码` 引用，凸显出两个问题：
1. 存在繁琐的机械劳动
2. 出错率高

iFlutter解决方案: 当在 `指定的目录` 中添加、删除、重命名文件时，`iFlutter` 插件都会感知，并自动修改 `pubspec.yaml` 文件，同时生成配套的 `_res.dart文件(类似Android中的R文件)`，通过 `R.xx` 的方式就能使用资源，效果如下:  

![资源联动动效](http://iflutter.toolu.cn/configs/dynamic_res.gif)

- 其中 `指定的目录` ，并不是 `iFlutter` 所固定要求的，开发者可自行配置，默认 `images`、 `assets`，具体配置如下：  

![资源联动配置](http://iflutter.toolu.cn/configs/config_gen_r.png)

- 生成的 `_res.dart` 文件的规则又是什么呢？ 如果 `指定的目录` 是 `images`，那么就会生成 `lib/res/images_res.dart` 文件，对应的类名 `ImagesRes`。按此类推，如果目录名是 `Assets`，生成的文件和类名分别是 `lib/res/assets_res.dart` 和 `AssetsRes`。值得一提的是，如果开发者手动修改 `pubspec.yaml` 文件中资源的配置，`iFlutter` 也会感知，并重新生成 `_res.dart` 文件。

- 从 `节省包体积`、`照顾强迫症研发` 出发，可配置某些资源不会在 `_res.dart` 文件中生成字段，比如 `.json` 文件，默认都生成，开发者可自行配置：

![资源联动配置](http://iflutter.toolu.cn/configs/config_ignore_field.png)

- 如果AndroidStudio安装了 `iFlutter` 插件，资源自动注册功能默认打开，如果想禁用此功能，可配置关闭，配置如下:

![资源联动配置](http://iflutter.toolu.cn/configs/config_enable_psi.png)

---
对于`Flutter Plugin`类型的项目，`iFlutter`会自动将资源注册到 `pubspec.yaml` 中，并生成对应的 `_res.dart` 文件。若主项目引入该 `Flutter Plugin` 以后，如果想使用其中的图片资源，代码如下：

```
Image.asset(SubModuleRes.LOGIN, package: SubModuleRes.PLUGIN_NAME)
```

为了简化代码的书写，对于 `Flutter Plugin` 类型的项目，可以打开配置，来调整资源R文件的字段生成规则：

![资源生成前缀](http://iflutter.toolu.cn/configs/res_with_package.png)

此时，主项目使用 `Flutter Plugin` 中的资源，可省略 `package` 参数，具体代码如下：

```
Image.asset(SubModuleRes.LOGIN)
```
