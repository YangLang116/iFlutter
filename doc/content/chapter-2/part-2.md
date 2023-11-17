通过上一节 `Json 转 Dart Entity` 工具可以创建一个新的实体类，对于项目中已有的类，那我们又该如何生成 `toJson` 和 `fromJson` 方法呢？先看使用效果：

![Gen动效](https://iflutter.toolu.cn/configs/generate_to_from_json.gif)

- 官方插件 `Dart` 已经提供了生成 `Constructor`、`Named Constructor` 和 `toString`方法，而 `iFlutter` 的 `fromJson` 和 `toJson` 正好加强了官方对类的 `fix(补全)` 吧。
