# 莫奈取色（Monet 动态配色）实现文档

> 面向使用者与二次开发者，说明「莫奈取色」的功能、代码结构、使用方式与兼容性边界。
> 相关提交：动态配色 P1–P4（Material3 侧）、v2.1.0（Miuix 侧 + 主题设置页）。

---

## 1. 功能说明

「莫奈取色」= 从一张图片（或一个预设色）里取出**一个种子色（seed）**，
再由这个种子色推导出**整套界面配色**，让应用的卡片、按钮、开关、底栏、预览图都随种子色变化。

### 1.1 两套主题，同一个种子

| 主题 | 落地方式 | 结果 |
|---|---|---|
| Material3 | `material-color-utilities`（MCU）由 seed 生成 `SchemeTonalSpot / Vibrant / Expressive / Content` 的**完整 Material 3 ColorScheme**（48 个 token 全覆盖，含 `surfaceContainer*` 层级） | 整套 Material 3 配色 |
| Miuix（HyperOS） | 复用上面那份 ColorScheme，再逐 token 映射进 Miuix 的 `Colors`（`dynamicMiuixColors`） | 整套 Miuix 配色，并与 `MaterialTheme.colorScheme` 同源 |

两套主题**共用同一份状态**（`dynamicSeed` / `dynamicStyle` / `dynamicContrast`），
因此切换主题风格时不会出现"两套配色"的割裂感；种子色本身也是同一份持久化数据。

> 为什么不直接用 Miuix 自带的 `colorsFromSeed`？它在本项目使用的 0.9.3 中是 **模块内部 API**（`internal`），
> 外部工程无法调用，因此改为"MCU 生成 → 映射进 Miuix token"，映射规则见 §2.3。

### 1.2 两个入口页面

| 页面 | 可见主题 | 内容 |
|---|---|---|
| **莫奈取色**（`MonetPage`） | Material3 | 预设色点、当前配色速览、选择图片、配色风格（Tonal Spot / Vibrant / Expressive / Content）、对比度（默认 / 高）、关闭动态配色 |
| **主题设置**（`ThemeSettingsPage`） | Miuix（HyperOS） | 手机预览示意图、明暗三选一（跟随系统 / 浅色 / 深色）、启用 Monet 颜色、强调色（默认 / 预设 / 从图片取色）、悬浮底栏、液态玻璃、模糊 |

Miuix 侧页面按参考截图的排版实现：居中标题 + 预览示意图 → 胶囊分段控件 → 卡片 1（莫奈）→ 卡片 2（底栏）。

**实时生效与预览机制**（`ThemeSettingsPage`）：

- 页面内所有开关**即时生效**：改动立刻写回 `AppState`，预览区域与当前界面（含底栏）同步更新，
  不需要返回主界面就能看到效果；
- 页面本身停留在原地：设置子页状态由**根布局**持有（`settingsPageState`，`SettingsPageSaver` 持久化），
  而不是留在设置页内部——底栏形态切换会让 `GlassShell` 走不同的布局分支（Scaffold ↔ 玻璃 Box+Scaffold），
  分支切换会改变该子树的组合 key，状态若留在分支内部就会被重置（表现为"一改液态玻璃/模糊就闪回设置主页"）；
- 预览示意图与真实界面**同源取色**（`themedSurfaceColors()`：HyperOS 下就是 Miuix 组件在用的那套颜色），
  因此预览所见即所得，并完整反映：明暗配色、种子色、底栏形态（悬浮 ↔ 贴边）、
  主题 shapes 圆角、底栏项文字排版；
- 过渡动画统一 `THEME_ANIM_MS = 240ms`：
  - **颜色**：`TempMailTheme` 把解析出的配色交给 `animateColorScheme` 逐 token 插值（Miuix 侧同理走
    `animateMiuixColors`），从"当前正在显示的颜色"开始渐变，**不存在闪帧**；明暗切换、莫奈种子色切换、
    卡片/底栏容器色全部走同一条动画，静置时不产生任何开销；
  - **底栏形态/玻璃进出**：`Crossfade`（普通 ↔ 玻璃 ↔ 贴边）+ `BottomBarTransition`（淡入淡出 + 轻微纵向位移）；
  - **预览示意图**：颜色 `animateColorAsState`、位移/形变 `animateDpAsState`，同一节奏。

### 1.3 底栏形态状态机

底栏形态是**三态枚举** `BarStyle`，无效组合在类型层面就不存在：

| 悬浮底栏 | 液态玻璃 | `BarStyle` | 形态 |
|---|---|---|---|
| 关 | —（不可用） | `Edge` | 贴边底栏（铺满整宽、贴屏幕底边、靠顶部分隔线分层） |
| 开 | 关 | `Float` | 普通悬浮底栏 |
| 开 | 开 | `LiquidGlass` | 悬浮液态玻璃底栏（API 33+ 为真玻璃） |

- 关闭悬浮 → 自动落到 `Edge`，**液态玻璃与模糊开关同时置灰不可用**（它们只对玻璃底栏有意义）；
- 开关状态持久化用稳定字符串 key（`float` / `liquid_glass` / `edge`），未知值回退 `Float`。

### 1.4 顺带引入的两项设置

- **明暗三态**：`ThemeMode.System / Light / Dark`。`System` 时实时跟随系统夜间模式，`Light / Dark` 为显式覆盖；
  旧版本的布尔 `isDarkMode` 会被一次性兼容读取（见 §4.3）。
- **模糊开关**：`glassBlurEnabled`。关闭后液态玻璃底栏自动退化为**不带模糊**的普通底栏
  （低版本、非 AGSL 设备本来就走这条路径），其余交互不变；该开关只在"悬浮开 + 液态玻璃开"时可用。

---

## 2. 代码结构

```
app/src/main/java/com/tempmail/app/
├── ui/theme/
│   ├── Theme.kt                      # 主题装配：seed → ColorScheme → MaterialTheme + MiuixTheme
│   ├── Color.kt                      # 手工配色常量 + ThemeStyle / ThemeMode 枚举
│   ├── MiuixComponents.kt            # Miuix 桥接层：MiuixThemeIfNeeded / dynamicMiuixColors / Themed*
│   └── dynamic/
│       ├── DynamicColorSchemes.kt    # seed → 完整 Material 3 双套配色（纯函数）
│       ├── SeedExtractor.kt          # 图片 → seed（降采样 + Palette）
│       └── MonetPresets.kt           # 内置预设色 + DefaultMonetSeed（"默认"强调色）
└── MainActivity.kt                   # AppState / 持久化 / 两个设置页 / 预览示意图 / 强调色下拉
```

### 2.1 `dynamic/DynamicColorSchemes.kt` — 取色的数学部分

- `const val NoDynamicSeed = -1`：**未启用**动态配色（用 Int 而不是可空类型，避免 prefs / JSON 里的歧义）。
- `enum class DynamicStyle(key, label)`：`TonalSpot` / `Vibrant` / `Expressive` / `Content`，对应 MCU 的 Scheme 变体。
- `fun buildDynamicColorSchemes(seedArgb, style, contrastLevel): DynamicColorPair`：
  **纯函数**（无 IO、无平台依赖），同一个 seed 永远得到同一结果 —— 所以只需要持久化 seed，不必存图片或缓存配色。
- `@Composable fun rememberDynamicColorSchemes(...)`：只在 seed / 风格 / 对比度变化时重算一次。

### 2.2 `dynamic/SeedExtractor.kt` — 图片取色

`suspend fun extractSeedFromUri(context, uri): Int?`：

1. `BitmapFactory` 先 `inJustDecodeBounds` 读尺寸，再按 2 的幂降采样到最长边 200px（只看颜色分布，避免大图 OOM）；
2. `androidx.palette`（`maximumColorCount = 16`）取代表色，优先级
   `Vibrant → DarkVibrant → Muted → LightVibrant → 出现次数最多的 swatch`；
3. 取色失败（如纯灰图）返回 `null`，调用方提示"取色失败，请换一张图片"；
4. 全程在 `Dispatchers.IO`，解码后的 Bitmap 用完立即 `recycle()`。

### 2.3 `ui/theme/MiuixComponents.kt` — 两套主题的桥接

- `hyperMiuixColors(dark)`：项目**手工定义**的 HyperOS 配色（未启用莫奈时使用，与启用前逐像素一致）。
- `dynamicMiuixColors(scheme, dark)`：动态配色下的 Miuix 配色。要点是**卡片与页面都要带种子色色调**：
  浅色下页面取 `surfaceContainerLow`（最浅的一档）、卡片取 `surfaceContainer`；
  深色下页面取 `surfaceContainerLowest`、卡片取 `surfaceContainer`。
  （不能把卡片映射到 `surfaceContainerLowest`——那是纯白，开启莫奈后会出现"页面变色、卡片仍是固定白色"的冲突。）
- `MiuixThemeIfNeeded(themeStyle, darkTheme, dynamicScheme, content)`：
  HyperOS 时用上面二者之一包一层 `MiuixTheme`；Material3 时直接透传（Miuix 类不会被加载）。
- `Themed*` 组件桥接：`ThemedSegmentedTabs`（胶囊分段，选中项 200ms 变色过渡）、
  `ThemedListRow`（图标 + 标题 + 副标题 + 右侧控件；HyperOS 用 Miuix `BasicComponent`）、
  `ThemedDropdownValue`（灰色当前值 + 上下双箭头）等。
- 其它桥接入口：`themedSurfaceColors()`（当前主题真正生效的"页面底/卡片/主色"等 token，HyperOS 取 Miuix 色板，
  预览示意图与底栏共用）、`themedBarContainerColor()`（底栏容器色，开启莫奈后带种子色色调）、
  `cornerRadiusOf(shape)`（把 shapes token 换算成 dp，供 Miuix Card 与预览示意图使用）。

### 2.4 `MainActivity.kt` — 状态、持久化与页面

- `AppState` 新增字段：`themeMode`（三态）、`glassBlurEnabled`；
  既有字段：`dynamicSeed` / `dynamicStyle` / `dynamicContrast`。
- 实时值：`val darkTheme = state.themeMode.isDark(isSystemInDarkTheme())`，
  整棵 UI 树只认这个 `darkTheme`（状态栏图标、`TempMailTheme`、玻璃底栏都取它）。
- 持久化：`SharedPreferences("app")` 键 `themeMode` / `glassBlurEnabled` / `dynamicSeed` / `dynamicStyle` / `dynamicContrast`；
  配置变更与进程重建由 `rememberSaveable(AppStateSaver)`（JSON）兜底。
- **强调色下拉**（`AccentDropdown`）：`Popup`（`Alignment.TopEnd`）锚定在强调色行下方的零高度锚点上，
  内容为「默认（应用主色蓝）/ 预设色带（可横向滚动）/ 从图片取色」，带 180ms 淡入 + 缩放过渡。
- 底栏：`BarStyle`（`Float` / `LiquidGlass` / `Edge`）三选一，
  `MiuixFloatingBottomBar`（悬浮）/ `LiquidGlassBottomBar`（伪玻璃，低版本与"模糊关闭"时）/ `MiuixEdgeBottomBar`（贴边）三种形态，
  切换时用 `Crossfade` 过渡；Material3 主题仍走原来的 `NavigationBar`，完全不受影响。

---

## 3. 使用方法

### 3.1 Material3 主题

设置 → **莫奈取色**：

1. 点预设色点，或点「选择图片」从相册取色（无需任何权限，走系统 Photo Picker）；
2. 需要更鲜明的效果可切换「配色风格」，需要更强对比可选「对比度：高」；
3. 点「关闭动态配色」恢复默认配色。

### 3.2 Miuix（HyperOS）主题

设置 → **主题设置**：

1. 顶部三选一切换「跟随系统 / 浅色 / 深色」；
2. 「启用 Monet 颜色」开关打开后，强调色默认取应用主色蓝；
3. 点「强调色」下拉可换预设色或从图片取色（此时显示"自定义"）；
4. 卡片 2 控制底栏形态：悬浮底栏 → 液态玻璃 → 模糊（后两者只在悬浮开启后可用，见 §1.3）；
5. 以上所有改动**立即生效**：预览示意图与当前界面（含底栏）同步变化，页面停留在原处，可直接继续调整；
6. 返回（左上箭头 / 系统返回）只是离开本页，改动已经在生效中。

### 3.3 参数一览

| 参数 | 取值 | 说明 |
|---|---|---|
| `dynamicSeed` | `-1` 或 ARGB | `-1` = 关闭；否则为种子色 |
| `dynamicStyle` | `tonal_spot` / `vibrant` / `expressive` / `content` | MCU 配色风格，默认 `tonal_spot` |
| `dynamicContrast` | `0f` / `0.5f` | 对比度（默认 / 高） |
| `themeMode` | `system` / `light` / `dark` | 明暗三态，默认 `light` |
| `barStyle` | `float` / `liquid_glass` / `edge` | 底栏形态（悬浮 / 悬浮+液态玻璃 / 贴边），默认 `float` |
| `glassBlurEnabled` | `true` / `false` | 玻璃模糊开关，默认 `true`（仅液态玻璃形态下可用） |

---

## 4. 兼容性

### 4.1 系统版本

- **取色本身不依赖任何新 API**：`androidx.palette` + MCU 都是纯 Java/Kotlin 计算，`minSdk 24` 起全版本可用。
- 仅**液态玻璃底栏**依赖 API 33+（AGSL/RuntimeShader）与 `miuix-blur`；低版本走既有的非玻璃底栏，
  与"模糊开关关闭"是同一条回退路径。
- 图片选择用 `ActivityResultContracts.PickVisualMedia`：Android 13+ 走系统 Photo Picker，低版本自动回退文档选择器，均无需存储权限。

### 4.2 与 Material3 的隔离

- 未启用动态配色时，两条分支的配色常量、组件选择、底栏形态都与定制前一致（Material3 逐像素不变）。
- Miuix 相关类只在 `ThemeStyle.HyperOS` 下被实例化；Material3 主题不会加载 `miuix-blur` 的类。

### 4.3 存储兼容

- 新键 `themeMode` 优先；若不存在，则读取旧布尔键 `isDarkMode`（`true → dark`，`false → light`），
  因此从旧版本升级后明暗行为不变。
- `dynamicSeed` / `dynamicStyle` / `dynamicContrast` 键名沿用 P1–P4 的约定，无需迁移。

### 4.4 实测验证（模拟器 Android 15 / API 35，1080×2400）

| 场景 | 结果 |
|---|---|
| Miuix 主题 + 关闭莫奈 | 与定制前一致（灰底 + 白卡片） |
| Miuix 主题 + 开启莫奈（默认 / 预设橙 / 预设玫红，浅色与深色） | 页面底色、卡片、开关、底栏、预览图全部随种子色变化，卡片不再是固定白色 |
| 强调色下拉 | 菜单锚定在行下方，预设色带完整可见，选中项显示对勾 |
| 深色 / 浅色 / 跟随系统 | 三态均生效；系统夜间模式切换时界面实时跟随 |
| 冷启动 | `themeMode` / `dynamicSeed` / `barStyle` / `glassBlurEnabled` 均正确恢复 |
| 底栏三态 | 悬浮关 → 贴边底栏（液态玻璃/模糊置灰）；悬浮开+玻璃关 → 悬浮底栏；悬浮开+玻璃开 → 悬浮玻璃底栏 |
| 实时生效与预览 | 改任意开关后：预览区域与当前界面（含底栏）立即同步变化；页面停留在主题设置页；持久化文件即时写入 |
| 预览完整性 | 底栏形态（悬浮/贴边）、明暗配色、种子色、主题圆角、底栏文字排版均在预览中实时反映 |
| 过渡动画 | 系统动画缩放设为 8× 后逐帧采样：配色切换时页面底与卡片呈现**各自不同的中间色**（`#6D6D6D` / `#5D5D5D`），说明是逐 token 插值而非整屏遮罩；玻璃底栏进出可见半透明/位移的中间帧 |
| 动画性能 | 一次 240ms 颜色渐变 ≈ 14 帧（60fps 标准），静置无开销；模拟器（swiftshader 软渲染）实测 90th=85ms、对照组（整页重组无动画）46ms、纯 Tab 切换 30ms，差异来自软渲染下的逐帧重组+重绘，真机（GPU 渲染）远低于此。系统"移除动画"（animator_duration_scale=0）时 Compose 会自动跳过动画 |
| 模糊关闭 + 液态玻璃 | 底栏退化为非模糊路径，无崩溃 |
| 连续切换 Tab | 液态玻璃底栏 18 次（含 6 次小幅快速滑动）+ 贴边底栏 12 次，底栏高亮与页面内容始终一致 |
| Material3 主题回归 | 设置项、配色、NavigationBar 底栏与定制前一致；莫奈取色页、深色模式页（三选一）正常 |

---

## 5. 二次开发提示

- **新增预设色**：往 `MonetPresets` 里加 ARGB 即可（下拉色带与 Material3 预设点会自动带上）。
- **改"默认"强调色**：改 `DefaultMonetSeed`（当前 = 应用主色蓝 `#0072E3`）。
- **新增取色风格**：在 `DynamicStyle` 加枚举并补 `buildScheme` 的 `when` 分支（MCU 的 Scheme 变体）；
  Miuix 侧无需改动（它复用同一份 ColorScheme）。
- **调整 Miuix 层级观感**：只改 `dynamicMiuixColors` 的 token 映射，不要动 `hyperMiuixColors`（保证关闭莫奈时像素不变）。
- **新增底栏形态**：往 `BarStyle` 加枚举 + 在 `fallbackBar` 的 `Crossfade` 里补分支；
  若新形态会改变内容避让，同时更新 `glassActive` 与各 Tab 的 `bottomOverlap`。
- **不要**在 `ui/glass/**` 里引入动态配色逻辑：那一层只负责玻璃渲染，配色统一由主题层提供。