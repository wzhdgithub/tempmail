package com.tempmail.app.ui.theme.dynamic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamiccolor.DynamicColor
import com.materialkolor.dynamiccolor.MaterialDynamicColors
import com.materialkolor.hct.Hct
import com.materialkolor.scheme.SchemeContent
import com.materialkolor.scheme.SchemeExpressive
import com.materialkolor.scheme.SchemeTonalSpot
import com.materialkolor.scheme.SchemeVibrant

// 动态配色（Monet 取色 → Material 3 配色）：
//   图片 → androidx.palette 提取 seed → 本文件用 material-color-utilities 生成完整的
//   Material 3 Light/Dark ColorScheme（全部 token 都由 seed 推导）。
//
// 只服务于 Material3 主题分支：TempMailTheme 仅在 ThemeStyle.Material3 下使用本文件的结果，
// HyperOS(Miuix) 分支与自定义底栏完全不读取这里的任何内容。

/** 动态配色未启用时的 seed 值（持久化用 Int，避免可空类型在 prefs/JSON 里的歧义）。 */
const val NoDynamicSeed = -1

/** 动态配色风格：对应 MCU 的 Scheme 变体，key 用于持久化（未知值回退默认）。 */
enum class DynamicStyle(val key: String) {
    /** 默认观感最接近 Material You 系统配色 */
    TonalSpot("tonal_spot"),

    /** 更鲜艳，主色饱和度更高 */
    Vibrant("vibrant"),

    /** 更活泼，色相分布更分散（适合"作品取色"这类偏艺术化的场景） */
    Expressive("expressive"),

    /** 忠实于原图色彩（主色就是 seed 本身） */
    Content("content");

    companion object {
        fun fromKey(key: String?): DynamicStyle = entries.find { it.key == key } ?: TonalSpot
    }
}

/** 同一 seed 生成的一对配色，供主题装配直接取用。 */
@Immutable
data class DynamicColorPair(val light: ColorScheme, val dark: ColorScheme)

/**
 * seed(ARGB) + 风格 + 对比度 → 完整的 Material 3 双套 ColorScheme。
 * 纯函数（无 IO、无平台依赖），同一个 seed 永远得到同一结果，因此只需要持久化 seed。
 */
fun buildDynamicColorSchemes(
    seedArgb: Int,
    style: DynamicStyle = DynamicStyle.TonalSpot,
    contrastLevel: Float = 0f
): DynamicColorPair {
    val hct = Hct.fromInt(seedArgb)
    return DynamicColorPair(
        light = buildScheme(hct, style, isDark = false, contrastLevel = contrastLevel),
        dark = buildScheme(hct, style, isDark = true, contrastLevel = contrastLevel)
    )
}

/** 组合期使用：只在 seed / 风格 / 对比度变化时重算一次。 */
@Composable
fun rememberDynamicColorSchemes(
    seedArgb: Int,
    style: DynamicStyle = DynamicStyle.TonalSpot,
    contrastLevel: Float = 0f
): DynamicColorPair = remember(seedArgb, style, contrastLevel) {
    buildDynamicColorSchemes(seedArgb, style, contrastLevel)
}

/** 单个明暗分支：MCU 的 scheme 变体 + 逐 token 映射到 Material 3 的 ColorScheme。 */
private fun buildScheme(
    hct: Hct,
    style: DynamicStyle,
    isDark: Boolean,
    contrastLevel: Float
): ColorScheme {
    val contrast = contrastLevel.toDouble()
    val scheme = when (style) {
        DynamicStyle.TonalSpot -> SchemeTonalSpot(hct, isDark, contrast)
        DynamicStyle.Vibrant -> SchemeVibrant(hct, isDark, contrast)
        DynamicStyle.Expressive -> SchemeExpressive(hct, isDark, contrast)
        DynamicStyle.Content -> SchemeContent(hct, isDark, contrast)
    }

    val roles = MaterialDynamicColors()
    fun role(select: MaterialDynamicColors.() -> DynamicColor): Color =
        Color(roles.select().getArgb(scheme))

    // 以 M3 默认 scheme 为底，再把由 seed 推导出的全部 token 覆盖上去
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = role { primary() },
        onPrimary = role { onPrimary() },
        primaryContainer = role { primaryContainer() },
        onPrimaryContainer = role { onPrimaryContainer() },
        inversePrimary = role { inversePrimary() },
        primaryFixed = role { primaryFixed() },
        primaryFixedDim = role { primaryFixedDim() },
        onPrimaryFixed = role { onPrimaryFixed() },
        onPrimaryFixedVariant = role { onPrimaryFixedVariant() },
        secondary = role { secondary() },
        onSecondary = role { onSecondary() },
        secondaryContainer = role { secondaryContainer() },
        onSecondaryContainer = role { onSecondaryContainer() },
        secondaryFixed = role { secondaryFixed() },
        secondaryFixedDim = role { secondaryFixedDim() },
        onSecondaryFixed = role { onSecondaryFixed() },
        onSecondaryFixedVariant = role { onSecondaryFixedVariant() },
        tertiary = role { tertiary() },
        onTertiary = role { onTertiary() },
        tertiaryContainer = role { tertiaryContainer() },
        onTertiaryContainer = role { onTertiaryContainer() },
        tertiaryFixed = role { tertiaryFixed() },
        tertiaryFixedDim = role { tertiaryFixedDim() },
        onTertiaryFixed = role { onTertiaryFixed() },
        onTertiaryFixedVariant = role { onTertiaryFixedVariant() },
        error = role { error() },
        onError = role { onError() },
        errorContainer = role { errorContainer() },
        onErrorContainer = role { onErrorContainer() },
        background = role { background() },
        onBackground = role { onBackground() },
        surface = role { surface() },
        surfaceDim = role { surfaceDim() },
        surfaceBright = role { surfaceBright() },
        surfaceContainerLowest = role { surfaceContainerLowest() },
        surfaceContainerLow = role { surfaceContainerLow() },
        surfaceContainer = role { surfaceContainer() },
        surfaceContainerHigh = role { surfaceContainerHigh() },
        surfaceContainerHighest = role { surfaceContainerHighest() },
        onSurface = role { onSurface() },
        surfaceVariant = role { surfaceVariant() },
        onSurfaceVariant = role { onSurfaceVariant() },
        surfaceTint = role { surfaceTint() },
        inverseSurface = role { inverseSurface() },
        inverseOnSurface = role { inverseOnSurface() },
        outline = role { outline() },
        outlineVariant = role { outlineVariant() },
        scrim = role { scrim() }
    )
}