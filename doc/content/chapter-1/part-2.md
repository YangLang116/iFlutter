根据上一章节的说明，`iFlutter` 会自动注册资源到 `pubspec.yaml` 文件中，格式如下：
```
flutter:
  assets:
    - images/about_a.png
    - images/about_b.png
    ...

```
随着项目越来越大，以这种方式注册资源，`pubspec.yaml` 文件内容也会越来越长。为了兼容此情况，`iFlutter` 提供以目录的形式进行资源的注册，配置如下：

![资源注册类型](https://iflutter.toolu.cn/configs/fold_register.png)

调整资源注册方式，`iFlutter` 会提示重启IDEA，在下一次对资源文件的增、删、改后(或者`重置资源`)，`pubspec.yaml` 文件注册方式会自动调整，格式如下：

```
flutter:
  assets:
    - images/
    ...

```

**目录注册** 的方式，会导致 dimension(2.0x、3.0x...)失效，可能是Flutter对资源处理方式不同，并非`iFlutter` 导致！！！，即：

```
有如下文件夹结构:

- images
   - 3.0x
    - about.png
   - about.png

以 'images/about.png' 的方式，在任何分辨率的机型下，都无法使用到3.0x下的素材。

```
