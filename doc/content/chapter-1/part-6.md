随着Flutter项目的不断迭代，项目中所使用的资源也会越来越多。大部分的情况下，项目所使用的资源文件都是直接平展在目录下，类似:

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

资源归类后，`iFlutter` 会重新注册资源到 `pubspec.yaml` 中，并修改受影响的 `*.dart` 文件。

- 并非所有目录都支持资源归类，默认 `assets` 、`images` 目录，开发者可自行调整，具体如下:

![Res Category 配置](https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/config_gen_r.png)

具体效果如下：

![Res Category 动效](https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/cate_res.gif)
