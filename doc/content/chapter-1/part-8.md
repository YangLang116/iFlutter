为了避免添加大图片到项目中，导致应用包体积过分增涨，或者加载图片OOM等意外，`iFlutter`
会监听图片资源添加事件。当新引入到项目中的图片资源过大时，`iFlutter` 会通过弹窗的形式加以提醒。

![大图引入提醒](http://iflutter.toolu.cn/configs/check_pic.gif)

考虑到不同项目对资源把控的严格程度不一致，`iFlutter` 将相关参数以配置的形式提供出来，当然也可以关闭该功能，具体如下：

![大图引入配置](http://iflutter.toolu.cn/configs/check_pic_config.png)

|     约束条件      |     默认值     |
|:-------------:|:-----------:|
|   max size    |    500K     |
| max dimension | 1280 x 2400 |

---

除了检测图片大小以外，对于新增的图片，`iFlutter` 也会通过弹窗的形式，引导开发者是否需要对图片进行压缩。只有开发者配置了Tiny Key，并勾选`Show tiny image dialog ...`，功能才能被激活。

![压缩图片提醒](http://iflutter.toolu.cn/configs/auto_tiny.gif)