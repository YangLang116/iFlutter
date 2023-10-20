当项目使用`flutter_localizations`支持国际化后，不同语种的字符资源需要手动添加到不同的`*.arb`文件中，过程枯燥而毫无技术含量。针对这一问题，`iFlutter`支持快速添加、移除国际化资源，效果如下：

- 添加资源 (快捷键: Option/Alt + A)

![添加资源](https://iflutter.toolu.cn/configs/intl_add.gif)

- 移除资源 (快捷键: Option/Alt + R)

![移除资源](https://iflutter.toolu.cn/configs/intl_remove.gif)


如果当前IDEA安装了`flutter_intl`插件，为了提供统一的开发习惯，`iFlutter`会在`flutter_intl`功能入口处，注入资源管理入口，效果如下：

![功能注入](https://iflutter.toolu.cn/configs/intl_inject.png)

---

`iFlutter-1.3.6` 版本以后支持自动翻译功能，在添加资源界面的**任意**`locale`输入框中输入内容，切换光标以后，`iFlutter`会尝试通过百度翻译进行其他`locale`内容的填充。

`iFlutter` 作为IDEA插件会被多个团队使用，而百度翻译请求存在QPS限制，为了保证各个团队的翻译功能不受限，`iFlutter` 将百度翻译需要的 `API KEY` 和 `API SECRET` 以配置的方式提供出来，如下图:

![翻译配置](https://iflutter.toolu.cn/configs/intl_config.png)

其中的 `API KEY` 和 `API SECRET` 需要在[百度翻译平台](https://api.fanyi.baidu.com/product/11)注册获取。
