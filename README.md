# TempMail · 临时邮箱

> 一个 Android 临时邮箱应用（Jetpack Compose）：一键生成临时邮箱、收信、复制验证码，
> 15 种语言、深浅色与动态配色，支持应用内自动更新。

[![License](https://img.shields.io/badge/License-GPL--3.0--or--later-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Android-API%2024%2B-3DDC84.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose-BOM%202026.05.01-4285F4.svg)](https://developer.android.com/jetpack/compose)

---

## Features

- **临时邮箱**：一键生成地址、刷新收件、复制地址、历史邮箱复用
- **邮件详情**：WebView 渲染 HTML 正文，链接 / 按钮 / 验证码快捷复制
- **界面风格**：Material 3 与 Miuix（HyperOS）两套主题风格可切换；明暗支持「跟随系统 / 浅色 / 深色」三态
- **动态配色**：从图片或预设色取出一个种子色，生成整套配色（两套主题风格各自适配）
- **底栏形态**：贴边 / 悬浮 / 悬浮液态玻璃，可在设置中切换；玻璃形态在 **API 33+ 且支持 AGSL** 的设备上启用，其余设备自动回退
- **多语言**：15 种语言（中 / 英 / 日 / 韩 / 法 / 德 / 西 / 葡 / 俄 / 意 / 阿 / 印地 / 越 / 泰 / 印尼）
- **自动更新**：启动时检查 GitHub Release，下载 APK 并做 SHA-256 校验后安装（校验失败拒绝安装）
- **其它**：首次启动的免责声明页、收件页顶部随机诗词、深色启动无白闪

---

## Requirements

| 项 | 版本 |
|---|---|
| JDK（运行 Gradle） | **JDK 24**（JDK 25 会让 Kotlin 2.4 编译器的版本解析崩溃） |
| Kotlin / Compose 编译器插件 | 2.4.20 |
| Compose BOM | 2026.05.01（ui / foundation 1.11.2、material3 1.4.0） |
| AGP / Gradle | 8.13.0 / 8.14.4 |
| compileSdk / targetSdk / minSdk | 37 / 34 / **24** |
| 玻璃效果运行要求 | **API 33+ 且 RuntimeShader(AGSL) 可用** |

> `miuix-blur` 声明 minSdk 33，本项目保留 minSdk 24 并用
> `<uses-sdk tools:overrideLibrary="top.yukonga.miuix.kmp.blur" />` 放行，运行时按 API 分级降级。
> Miuix 0.9.3 要求 compileSdk ≥ 37，AGP 8.13 官方只测到 36.1，故 `gradle.properties` 需要
> `android.suppressUnsupportedCompileSdk=37`。

---

## Build

```powershell
# Windows PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-24"
.\gradlew :app:assembleDebug      # 调试包
.\gradlew :app:assembleRelease    # 发布包
```

Release 签名通过仓库根目录的 `keystore.properties` 提供（已在 `.gitignore` 中，不入库）：

```properties
storeFile=../keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```

缺少该文件时只构建调试包，Release 任务会被跳过。

---

## 版本历史

| 版本 | 内容 |
|---|---|
| v1.0 ~ v1.9 | 临时邮箱主体功能演进：收件 / 历史 / 多语言 / WebView 正文 / 验证码复制 / 自动更新与 SHA-256 校验 / WebView 泄漏修复 / 自适应图标 等 |
| **v2.0** | 工具链升级（Kotlin 2.4.20 / Compose 1.11.2 / material3 1.4.0 / compileSdk 37）；新增 Miuix（HyperOS）主题风格与液态玻璃底栏；15 语言文案同步 |
| **v2.1** | 动态配色（从图片 / 预设色取种子色生成整套配色，两套主题风格均支持）；新增「主题设置」页（实时预览、明暗三态、强调色、底栏形态）；底栏扩展为贴边 / 悬浮 / 悬浮液态玻璃三态；配色与底栏切换加入平滑过渡 |

---

## License

本项目以 **GNU General Public License v3.0 or later（GPL-3.0-or-later）** 发布，全文见 [LICENSE](LICENSE)；
Copyright (C) 2026 wzhdgithub。

你可以自由使用、修改、分发本项目，但衍生作品必须同样以 GPL-3.0-or-later 开源，并保留原作者版权声明。
第三方代码与依赖的版权 / 许可声明见 [NOTICE.md](NOTICE.md)。

---

## 相关链接

- GitHub 仓库：https://github.com/wzhdgithub/tempmail
- Releases：https://github.com/wzhdgithub/tempmail/releases
- 官网：https://wzhtmail.pwapi.cn/