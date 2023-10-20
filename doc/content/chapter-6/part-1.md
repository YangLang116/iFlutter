## 项目说明

因为国内网络环境原因，在编译flutter项目过程中，经常会出现类似如下问题：
```
FAILURE: Build failed with an exception.
[        ] * What went wrong:
[        ] A problem occurred configuring project ':audioplayers'.
[        ] > Could not resolve all artifacts for configuration ':audioplayers:classpath'.
[        ]    > Could not resolve org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32.
[        ]      Required by:
[        ]          project :audioplayers
[        ]       > Could not resolve org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32.
[        ]          > Could not get resource
'https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/1.4.32/kotlin-gradle-plugin-1.4.32.pom'.
[        ]             > Could not HEAD
'https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/1.4.32/kotlin-gradle-plugin-1.4.32.pom'.
[        ]                > Connect to repo.maven.apache.org:443 [repo.maven.apache.org/151.101.24.215] failed: Read timed out
```

为了能下载到对应的编译产物，通常需要在项目中添加镜像仓库，如阿里云镜像.

```
buildscript {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/central' }
        maven { url 'https://maven.aliyun.com/repository/jcenter' }
        google()
        mavenCentral()
    }

    dependencies {
        classpath 'com.android.tools.build:gradle:4.1.0'
    }
}
```

**但是**flutter项目会或多或少引入`Flutter Plugin`，那么就需要在每个`Plugin`的`build.gradle`中加入镜像仓库，工作量繁琐。好在`Gradle 7.0` 推出了新的依赖管理方式，能避免大量的CV操作，但是对于`Gradle7.0`之前的项目还是无能为力。而`iFlutter`提供了通用的处理方案，直接接管这一CV操作，自动给项目所有插件注入镜像仓库。

## 注入说明
- 根项目的`build.gradle`注入
- 所有`Android Flutter Plugin`的`build.gradle`注入
- Flutter编译脚本注入 ($flutterRoot/packages/flutter_tools/gradle/`flutter.gradle`)

## 使用入口

![插件使用](https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/mirror_repo_3.png)

**注意**: 引入新的`Android Flutter Plugin`，在执行`flutter pub get` 以后，需要重新注入，以保证新的插件会注入镜像仓库。

## 配置说明

`iFlutter` 镜像仓库手动配置，支持新增镜像仓库以及右键删除，具体配置如下：

![插件配置入口](https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/mirror_repo_1.png)

![插件配置](https://cdn.jsdelivr.net/gh/YangLang116/iFlutter/doc/configs/mirror_repo_2.png)

插件内置镜像仓库地址：
- https://maven.aliyun.com/repository/gradle-plugin
- https://maven.aliyun.com/repository/public
- https://maven.aliyun.com/repository/google
- https://maven.aliyun.com/repository/central
- https://maven.aliyun.com/repository/jcenter
