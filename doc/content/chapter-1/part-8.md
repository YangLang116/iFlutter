为了防止不经意间添加大图片到项目中，导致应用包体积过分增涨，或者加载图片OOM等意外，`iFlutter` 会监听图片资源添加事件。当新引入到项目中的图片资源过大时，`iFlutter` 会通过弹窗的形式加以提醒。

![大图引入提醒](https://iflutter.toolu.cn/configs/check_pic.gif)

考虑到不同项目对资源把控的严格程度不一致，`iFlutter` 将相关参数以配置的形式提供出来，如下：

![大图引入配置](https://iflutter.toolu.cn/configs/check_pic_config.png)

约束条件 | 默认值
:--: | :--:
max size |  500K
max width | 720
max height | 1080
