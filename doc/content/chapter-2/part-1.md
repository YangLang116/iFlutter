在Flutter项目的 `lib` 目录及其 `子目录` 下，`iFlutter` 支持 `Json` 转 `Dart Entity` 功能，在其他目录下该功能不可用，使用效果如下：

![J2D动效](http://iflutter.toolu.cn/configs/gen_entity.gif)

- 默认生成的 `Dart Entity` 是支持 `空安全` 的，如果项目还没适配到 `Flutter2.x` 版本，通过修改 `iFlutter` 配置即可，配置如下：

![J2D配置](http://iflutter.toolu.cn/configs/config_flutter_2.png)

- 填写类名时，采用**驼峰命名法**。
