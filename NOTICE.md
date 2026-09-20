# NOTICE / 第三方声明

本项目（TempMailApp，含其 Liquid Glass 底栏实现）以 **GPL-3.0-or-later** 发布，
完整许可全文见仓库根目录 [LICENSE](LICENSE)。

Copyright (C) 2026 wzhdgithub

下列第三方代码**实际用于**本项目的底栏实现，特此保留其版权与许可声明。
未使用的"仅参考过"的项目不列入本文件。

---

## 1. tiann / KernelSU — 直接来源

- 仓库：https://github.com/tiann/KernelSU
- 作者：weishu（tiann）与 KernelSU 贡献者
- 许可：**GPL-3.0-or-later**
  （其根目录 LICENSE 为 GPLv3 全文；README 声明 `kernel/` 目录为 GPL-2.0-only，其余部分为 GPL-3.0-or-later）
- 本项目使用的内容：
  - `app/src/main/java/com/tempmail/app/ui/glass/InnerShadow.kt`
    —— 移植自其 `manager/app/src/main/java/me/weishu/kernelsu/ui/component/liquid/InnerShadow.kt`
  - `app/src/main/java/com/tempmail/app/ui/glass/GlassInteraction.kt`
    —— 移植自其 `manager/.../ui/component/miuix/animation/` 与 `manager/.../ui/component/miuix/modifier/DragGestureInspector.kt`
    （`DampedDragAnimation`、`InteractiveHighlight`、`inspectDragGestures`）
  - `app/src/main/java/com/tempmail/app/ui/glass/GlassBottomBar.kt` 中的折射 / 色散着色器
    —— 移植自其 `manager/.../ui/component/liquid/Lens.kt`
  - 交互方案（按下放大镜、拖动切换、惯性吸附、越界橡皮筋、重力高光）参照其 FloatingBottomBar 实现路径
- 使用方式：**移植 / 修改后使用**（去掉了长按阈值与震动、重写了手势层与手感参数，见各文件头部注释）

## 2. Kyant0 / AndroidLiquidGlass — 上游作者

- 仓库：https://github.com/Kyant0/AndroidLiquidGlass
- 作者：Kyant
- 许可：**Apache License 2.0**，`Copyright 2025 Kyant`
- 本项目使用的内容（经 KernelSU 镜像而来，故在此保留其版权声明）：
  - 圆角矩形折射与色散 AGSL 着色器
    （其 `backdrop/src/commonMain/kotlin/com/kyant/backdrop/internal/Shaders.kt` 中的
    `RoundedRectRefractionShaderString` / `RoundedRectRefractionWithDispersionShaderString`）
  - `DampedDragAnimation`、`InteractiveHighlight` 与 `LiquidBottomTabs` 交互设计

## 3. compose-miuix-ui / miuix（Miuix）

- 仓库：https://github.com/compose-miuix-ui/miuix
- 许可：**Apache License 2.0**（其 Maven POM 声明 "The Apache Software License, Version 2.0"）
- 本项目使用的内容：
  - **作为依赖使用**：`top.yukonga.miuix.kmp:miuix-ui-android:0.9.3`、`top.yukonga.miuix.kmp:miuix-blur-android:0.9.3`
    （`miuix-blur` 提供 `drawBackdrop` / `layerBackdrop` / `blur` / `vibrancy` / `runtimeShaderEffect` 等底层能力）
  - 其官方示例 `LiquidGlassNavigationBar` 为底栏交互的来源之一
    （KernelSU 的 `liquid/*` 文件头注明 "Mirrored from compose-miuix-ui example"）

## 4. androidX.palette 与 material-color-utilities（莫奈取色）

- **androidx.palette**（`androidx.palette:palette:1.0.0`，Apache-2.0）
  —— 从用户选择的图片中提取代表色，作为莫奈取色的种子色（`ui/theme/dynamic/SeedExtractor.kt`）
- **material-color-utilities**（KMP 移植 `com.materialkolor:material-color-utilities:4.1.1`，Apache-2.0，
  上游为 Material Foundation 的 [material-color-utilities](https://github.com/material-foundation/material-color-utilities)）
  —— 由种子色推导完整的 Material 3 配色（`ui/theme/dynamic/DynamicColorSchemes.kt`）；
  该版本号**必须与 Miuix 传递依赖的版本一致**，避免同时加载两份 HCT 实现
- 内置预设色（`MonetPresets.kt`）为**手工挑选的代表色数值**，不包含任何画作图片或其数字化副本

## 5. 其它依赖（均为 Apache-2.0）

| 组件 | 许可 |
|---|---|
| Jetpack Compose（androidx.compose.*，BOM 2026.05.01） | Apache-2.0 |
| AndroidX Core / Lifecycle / Activity Compose | Apache-2.0 |
| OkHttp（com.squareup.okhttp3:okhttp:4.12.0） | Apache-2.0 |

---

## 关于本项目自有代码

除上述明确标注的文件外，`app/src/main/java/com/tempmail/app/` 下的其余代码
（应用逻辑、Material3 底栏分支、Miuix 悬浮 / 伪玻璃底栏、主题装配与动态配色映射、i18n 等）
为本项目原创或基于本项目早期版本演进，同样以 GPL-3.0-or-later 发布。