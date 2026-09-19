# TempMail

一个轻量级 Android 临时邮箱应用，基于 PearAPI 实现邮箱生成与邮件接收。

**🌐 官网：https://wzhtmail.pwapi.cn/**（下载最新 APK、查看发版说明）

## 开源协议

[MIT License](LICENSE)

你可以自由使用、修改、分发本软件，但需保留原作者版权声明。

---

## 开发思路

### 设计目标

- **极简体验**：无需注册、无需登录、打开即用。一键生成临时邮箱，一键刷新查看邮件
- **隐私优先**：不收集任何个人信息，所有数据仅存在于当前会话中。历史记录仅本地存储
- **轻量高效**：APK 仅约 3MB，无冗余依赖，不请求多余权限
- **离线可用**：核心 UI 完全本地渲染，仅网络请求部分依赖 PearAPI

### 架构决策

采用**单文件架构**（single-file architecture），原因如下：

- 应用功能域清晰且有限（邮箱生成 → 邮件接收 → 历史管理 → 设置），无需复杂的多模块拆分
- 避免 ViewModel + StateFlow 等重型架构带来的模板代码
- `AppState` 单一数据源通过 `mutableStateOf` 驱动 Compose 重组，状态管理直观
- 网络请求使用协程（`Dispatchers.IO`），不引入 Flow/架构组件，保持轻量

所有 UI、网络、解析、更新逻辑均集中于 `MainActivity.kt`（约 1930 行），配合 `ui/theme/` 下的主题配置。

### 工作流程

```
用户点击"生成" → GET https://api.pearapi.ai/api/email/?type=get
                 ↓
          获取邮箱地址 → 展示在界面
                 ↓
用户点击"刷新" → GET https://api.pearapi.ai/api/email/?type=receive&email={邮箱}
                 ↓
          解析 receivedata JSON → 渲染邮件卡片列表
                 ↓
用户退出或生成新邮箱 → 原邮箱自动移入历史记录
```

---

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Kotlin | 1.9.22 |
| UI | Jetpack Compose + Material 3 | BOM 2024.01.00 |
| 编译器 | Kotlin Compiler Extension | 1.5.8 |
| 网络 | OkHttp | 4.12.0 |
| JSON | org.json (Android 内置) | — |
| 构建 | Android Gradle Plugin | 8.13.0 |
| 最低 SDK | Android 7.0 (API 24) | — |
| 目标 SDK | Android 14 (API 34) | — |
| 编译 SDK | Android 14 (API 34) | — |
| 签名 | 自定义 keystore.jks | — |

### 选择理由

| 选择 | 理由 |
|------|------|
| **无 ViewModel / StateFlow** | 应用状态简单，`mutableStateOf` 足以驱动重组，避免模板代码 |
| **无 kotlinx.serialization** | 仅解析固定格式 JSON，`org.json.JSONObject` 更轻量且无需额外编译插件 |
| **无 Hilt / Koin** | 仅一个 OkHttpClient 实例，手动 `remember` 一把即可 |
| **无 Navigation Compose** | 三个 tab 用 AnimatedContent 切换，设置子页用 enmu + AnimatedContent，比 Navigation 更可控 |
| **无 Room** | 历史记录仅存内存，非持久化（用户预期就是一次性使用） |
| **协程（无 Flow）** | 仅用 `Dispatchers.IO` + `launch`，不引入 Flow/架构组件，保持轻量 |

### Gradle 构建要点

```kotlin
// AGP 8.13 需要显式开启 buildConfig
buildFeatures { compose = true; buildConfig = true }

// JDK 24 必须设置 $env:JAVA_HOME，否则 Kotlin 1.9.22 的版本号解析器会崩溃
// 系统自带的 JDK 25 不兼容 Kotlin 1.9.22

// 发布 APK 需要签名 + ProGuard 混淆
release { isMinifyEnabled = true; isShrinkResources = true }

// 中国 ROM 会拦截 testOnly APK，需要 adbOptions { installOptions("-t") }
```

---

## 功能说明

### 首次启动

- **免责声明**：首次启动展示服务免责声明，同意后可进入（含一个性别选择小彩蛋）
- **诗句横幅**：收件箱顶部随机展示一句古诗词（poetry.palemoky.com，仅启动时请求一次）

### 收件箱（Inbox）

- **生成邮箱**：点击"生成新邮箱"按钮，调用 PearAPI 分配一个临时邮箱地址
- **复制邮箱**：点击复制按钮将邮箱地址写入剪贴板
- **刷新邮件**：点击"刷新"查询该邮箱的收件状态，获取新邮件
- **邮件列表**：按时间倒序展示邮件卡片，显示发件人、主题、时间、正文
- **原始数据**：当 JSON 解析异常时，自动降级展示原始 `receivedata`

### 历史记录（History）

- **当前邮箱**：列表顶部始终显示正在使用的邮箱，标记为"使用中"
- **历史邮箱**：每次生成新邮箱时，旧邮箱自动保存到历史列表，标记为"已过期"
- 历史记录仅在当前会话有效，退出应用即清空

### 设置（Settings）

| 功能 | 说明 |
|------|------|
| 语言 | 支持 15 种语言：中文、English、日本語、한국어、Français、Deutsch、Español、Português、Русский、Italiano、العربية、हिन्दी、Tiếng Việt、ไทย、Bahasa Indonesia |
| 深色模式 | 开关切换亮色/深色主题，实时生效 |
| 主题风格 | `Material3`（默认）与 `Miuix` 两套主题一键切换，Material3 表现与定制前完全一致 |
| 底栏风格 | 仅 Miuix 主题下提供：`悬浮`（默认，轻量悬浮底栏）与 `Liquid Glass`（液态玻璃底栏，玻璃质感 + 滑动指示器 + 按压回弹） |
| 检查更新 | 手动触发 GitHub Releases 检查 |
| 自动检查 | 启动时自动检查更新，可通过开关关闭 |
| 关于 | 应用简介 |
| 作者信息 | 作者主页、项目仓库、Blog、Email、微信 |

### 自动更新

1. 启动时（或手动点击"检查更新"） → GET `https://api.github.com/repos/wzhdgithub/tempmail/releases/latest`
2. 比较 `tag_name` 与当前 `BuildConfig.VERSION_NAME`
3. 有新版本 → 弹出 AlertDialog 显示发版说明
4. 点击"在线更新" → `OkHttp` 下载 APK 到 `getExternalFilesDir/updates`
5. 下载过程中显示 `LinearProgressIndicator` + 百分比
6. 下载完成 → 校验 APK 的 SHA-256（取自 Release asset 的 `digest` 字段，或发版说明中的 64 位十六进制哈希），校验失败则删除安装包并提示失败
7. 校验通过 → `FileProvider` + `ACTION_VIEW` 拉起安装界面

```kotlin
fun downloadInstall(url: String) {
    // OkHttp 流式下载 → 写入文件 → FileProvider 共享 → 安装 Intent
}

fun checkUpdate(isManual: Boolean) {
    // GitHub Releases API → JSON 解析 → 版本比较 → AlertDialog
}
```

### 邮件解析

PearAPI 返回的 `receivedata` 格式：

```json
[
    {
        "from": "sender@example.com",
        "subject": "邮件主题",
        "time": "2024-01-01 12:00:00",
        "body": { "text": "邮件正文内容", "html": "<a href='...'>点击确认</a>" }
    }
]
```

`parseEmails()` 函数将其映射为 `List<EmailItem>`，解析失败时显示原始 JSON。

### 邮件正文渲染（WebView）

- 邮件详情用 WebView 渲染 HTML，支持 Google / Microsoft / GitHub / Qoder 等验证邮件的按钮与超链接
- 点击任意链接（`http` / `https` / `mailto`）→ 通过 `Intent.ACTION_VIEW` 跳转系统浏览器
- 三重拦截保障链接可点：`shouldOverrideUrlLoading` 双重载（新版 + 兼容旧版）、`onCreateWindow` 处理 `target="_blank"`、注入 JS 捕获阶段拦截（防邮件自带 JS / 浮层干扰）
- 安全：`javaScriptEnabled = true` 仅用于邮件渲染，链接一律外部浏览器打开，WebView 不长期驻留

### 验证码快捷复制

- 邮件正文中检测验证码（支持中文/英文提示词，4-8 位数字字母），弹窗内提供"复制验证码"按钮，一键复制到剪贴板

### 邮件去重合并

- 多次刷新按 `from|subject|time` 去重合并，新邮件追加到列表尾部，不丢旧邮件

### 历史记录复用

- 已过期的历史邮箱可点击"使用"重新启用，无需再等生成新邮箱

### 动态版本号

- 版本号只维护在 `build.gradle.kts` 的 `versionName`，设置页显示与更新检查均读取 `BuildConfig.VERSION_NAME`，无需多处修改

---

## PearAPI

免费、无需鉴权的临时邮箱 API。

| 端点 | 参数 | 返回 |
|------|------|------|
| `GET /api/email/?type=get` | — | `{"code":"200","email":"xxx@domain.com"}` |
| `GET /api/email/?type=receive&email={email}` | 邮箱地址 | `{"code":"200","count":"3","receivedata":"[...]"}` |

- 使用 HTTPS，在 `network_security_config.xml` 中已配置
- 响应中的 `code` 为 `"200"` 表示成功
- `receivedata` 是 JSON 字符串（非数组），需要二次解析

---

## 版本历史(截至目前)

| 版本 | 内容 |
|------|------|
| v1.0 | 基础功能：生成邮箱 + 接收邮件 + 历史记录 |
| v1.1 | 底部导航、设置页（语言/深色模式/关于）、页面过渡动画、自动更新机制、GitHub 发布 |
| v1.2 | 更新对话框 + 进度下载、自动检查开关持久化 |
| v1.3 | ProGuard 混淆优化、FileProvider 安装适配 |
| v1.4 | 扩展至 15 种语言、沉浸式状态栏 |
| v1.5 | 邮件解析兼容多格式、正文 HTML/纯文本降级展示 |
| v1.6 | WebView 渲染邮件正文、验证链接/按钮点击跳转浏览器、验证码一键复制 |
| v1.7 | 历史邮箱一键复用、设置持久化、版本号统一读取 BuildConfig |
| v1.8 | 配置更改状态持久化（rememberSaveable）、时间戳排序、Dialog 文案全面国际化、SHA-256 更新校验、签名凭据外置、Release 日志剥离 |
| v1.9 | WebView 泄漏修复、状态栏图标跟随应用主题、更新限流误报修复、验证码正则修正、远程图片默认关闭（防追踪）、下载可取消、自适应图标 |

---

## 构建指南

### 环境要求

- **JDK 24**（`$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"`）：用于运行 Gradle/Kotlin 编译守护进程（JDK 25 与 Kotlin 1.9.22 的版本号解析器不兼容）；编译产物目标为 Java 17
- Android SDK 34
- Gradle 8.13 (wrapper 自动下载，Windows 用 `gradlew.bat`，Linux/macOS 用 `./gradlew`)

### 构建 Debug APK

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
./gradlew :app:assembleDebug
```

### 构建 Release APK（已签名）

```bash
$env:JAVA_HOME = "C:\Program Files\Java\jdk-24"
./gradlew :app:assembleRelease
```

APK 输出位置：`app/build/outputs/apk/release/app-release.apk`

### 注意

- 签名凭据存放于根目录 `keystore.properties`（已加入 `.gitignore`，不纳入版本控制），字段为 `storeFile` / `storePassword` / `keyAlias` / `keyPassword`；也可通过环境变量 `KEYSTORE_PASSWORD` / `KEY_PASSWORD` 提供。缺少凭据时 debug 回退到默认签名、release 不签名
- 发布新版时，APK 的 SHA-256 会自动随 GitHub Release asset 的 `digest` 字段下发；如使用其他发布渠道，请在发版说明中附上 64 位十六进制哈希
- Release 构建启用 ProGuard（`proguard-rules.pro`），并通过 `-assumenosideeffects` 移除全部 `android.util.Log` 调用
- `buildConfig = true` 必须开启，否则 `BuildConfig.VERSION_NAME` 不可用

---

## 项目结构

```
TempMailApp/
├── build.gradle.kts              # 项目级构建配置 (AGP 8.13 + Kotlin 1.9.22)
├── settings.gradle.kts           # 项目设置
├── gradle.properties             # Gradle 属性
├── app/
│   ├── build.gradle.kts          # 模块构建配置
│   ├── keystore.jks              # 签名文件
│   ├── proguard-rules.pro        # ProGuard 规则
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/tempmail/app/
│       │   ├── MainActivity.kt   # 全部逻辑（约 1586 行）
│       │   └── ui/theme/
│       │       ├── Color.kt      # 颜色定义
│       │       ├── Theme.kt      # Material3 主题
│       │       └── Type.kt       # 排版配置
│       └── res/
│           ├── drawable/         # 启动图标 (Vector Drawable)
│           ├── xml/
│           │   ├── network_security_config.xml
│           │   └── file_paths.xml
│           └── values/
│               └── themes.xml    # 基础主题
```

---

## 相关链接

- **官网**：https://wzhtmail.pwapi.cn/
- **GitHub 仓库**：https://github.com/wzhdgithub/tempmail
- **GitHub Releases**：https://github.com/wzhdgithub/tempmail/releases
- **作者主页**：https://github.com/wzhdgithub
- **Blog**：https://wzhblog6.pwapi.cn/

---

## 致谢

- [PearAPI](https://api.pearapi.ai) — 提供免费临时邮箱 API
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — 声明式 UI 框架
- [OkHttp](https://square.github.io/okhttp/) — HTTP 客户端
- [Material 3](https://m3.material.io/) — 设计系统
