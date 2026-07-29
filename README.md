# jpush_flutter_android

[![pub package](https://img.shields.io/pub/v/jpush_flutter_android)](https://pub.dev/packages/jpush_flutter_android)
[![GitHub license](https://img.shields.io/github/license/LiWenHui96/jpush_flutter_android?label=协议&style=flat-square)](https://github.com/LiWenHui96/jpush_flutter_android/blob/master/LICENSE)

> 集成极光推送的 Android 厂商通道 SDK 的 Flutter 插件。

## 环境要求

需与 `jpush_flutter` [![pub package](https://img.shields.io/pub/v/jpush_flutter)](https://pub.dev/packages/jpush_flutter) 插件配合使用。

版本对应关系

| jpush_flutter | jpush_flutter_android |
|:-------------:|:---------------------:|
| 3.4.8+        | 1.0.2                 |
| 3.4.5+        | 1.0.1                 |
| 3.4.3+        | 0.0.10+               |
| 3.3.8+        | 0.0.9                 |
| 3.3.2+        | 0.0.7                 |
| 3.2.4-3.3.1   | 0.0.5-0.0.6           |
| 3.2.0-3.2.3   | 0.0.4                 |
| 3.1.9         | **不建议使用**          |
| 3.1.8         | 0.0.3                 |
| 3.0.9-3.1.7   | 0.0.1                 |

## 快速开始

### 添加依赖

将 `jpush_flutter_android` 添加至 `pubspec.yaml` 引用

```yaml
dependencies:
  jpush_flutter: ^latest_version
  jpush_flutter_android: ^latest_version

jpush_android:
  app_key: xxx
  channel: xxx
  huawei:
    enable: true # 默认为false
  xiaomi:
    enable: true # 默认为false
    app_key: xxx
    app_id: xxx
  meizu:
    enable: true # 默认为false
    app_key: xxx # MZ- 已默认配置，只需参数即可
    app_id: xxx # MZ- 已默认配置，只需参数即可
  vivo:
    enable: true # 默认为false
    app_key: xxx
    app_id: xxx
  oppo:
    enable: true # 默认为false
    app_key: xxx # OP- 已默认配置，只需参数即可
    app_id: xxx # OP- 已默认配置，只需参数即可
    app_secret: xxx # OP- 已默认配置，只需参数即可
  honor:
    enable: true # 默认为false
    app_id: xxx
```

### 相关配置

[官方文档 - 厂商通道参数申请指南](https://docs.jiguang.cn/jpush/client/Android/android_3rd_param)

[官方文档 - 厂商通道 SDK 集成指南](https://docs.jiguang.cn/jpush/client/Android/android_3rd_guide)

[官方文档 - 厂商通道回执配置指南](https://docs.jiguang.cn/jpush/client/Android/android_callback)

[官方文档 - 厂商限额及 QPS 限制](https://docs.jiguang.cn/jpush/client/Android/android_3rd_qps)

[官方文档 - 厂商消息分类使用指南](https://docs.jiguang.cn/jpush/client/Android/android_channel_id)

#### 配置参数

从 `1.0.0` 版本起，只需在 `pubspec.yaml` 配置即可。

在 `build.gradle` 中添加如下参数

```gradle
android {
  ......
  
  defaultConfig {
    applicationId = "xx.xx.xx"
    ......
    
    manifestPlaceholders = [
        JPUSH_PKGNAME     : applicationId,
        JPUSH_APPKEY      : "你的 Appkey", // JPush 上注册的包名对应的 Appkey.
        JPUSH_CHANNEL     : "developer-default", // 暂时填写默认值即可.
        XIAOMI_APPKEY     : "您的应用对应的小米的APPKEY",
        XIAOMI_APPID      : "您的应用对应的小米的APPID",
        MEIZU_APPKEY      : "MZ-您的应用对应的魅族的APPKEY",
        MEIZU_APPID       : "MZ-您的应用对应的魅族的APPID",
        VIVO_APPKEY       : "您的应用对应的VIVO的APPKEY",
        VIVO_APPID        : "您的应用对应的VIVO的APPID",
        OPPO_APPKEY       : "OP-您的应用对应的OPPO的APPKEY",
        OPPO_APPID        : "OP-您的应用对应的OPPO的APPID",
        OPPO_APPSECRET    : "OP-您的应用对应的OPPO的APPSECRET",
        HONOR_APPID       : "您的应用对应的Honor的APPID"
    ]
  }
}
```

#### 配置代码混淆

代码混淆已集成在插件内，无须额外配置。

#### 配置权限

所需的基础权限已经集成在配置的jar文件包内，无须额外配置。

### 配置华为推送

配置 agconnect-services.json 文件

参考 [厂商通道参数申请指南](https://docs.jiguang.cn/jpush/client/Android/android_3rd_param#%E5%8D%8E%E4%B8%BA%E5%8F%82%E6%95%B0%E8%8E%B7%E5%8F%96) 获取 agconnect-services.json 文件，并配置到 app 目录下。
