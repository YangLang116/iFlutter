当合并分支代码时，如果出现 `_res.dart`、`i_font_res` 文件冲突，可重新生成：

![资源重新生成](../../configs/config_gen_res.png)

`iFlutter` 通过以下两个步骤，对资源进行重置:
- 将 `指定目录` 资源重新注册到 `pubsepec.yaml` 中；
- 重新生成 `_res.dart`、`i_font_res` 文件；
