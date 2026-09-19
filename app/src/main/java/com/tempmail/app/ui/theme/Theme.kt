package com.tempmail.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// 原有 Material3 配色（保持不变）
private val LightColorScheme = lightColorScheme(
    primary = Blue700,
    background = SurfaceLight,
    surface = CardLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    background = SurfaceDark,
    surface = CardDark
)

// HyperOS 风格配色（仅改视觉参数，组件体系仍是 Material3）。
// 层次参考 Miuix：亮色 = 浅灰底 + 纯白卡片，暗色 = 近黑底 + 深灰卡片，
// 文字靠主/次两级中性灰分层，tonal 按钮用浅灰容器，点缀浅蓝容器。
// 注意（material3 1.1.2 token 映射）：填充 Card 容器色取 surfaceVariant，
// AlertDialog / NavigationBar 取 surface，FilledTonalButton 取 secondaryContainer，
// Switch 未选中轨道默认也取 surfaceVariant（本项目通过 themedSwitchColors 改指 secondaryContainer）。
// 1.1.2 的 ColorScheme 尚无 surfaceBright/surfaceDim/surfaceContainer* 系列参数。
private val HyperLightColorScheme = lightColorScheme(
    primary = HyperBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF2FF),
    onPrimaryContainer = Color(0xFF005CBA),
    inversePrimary = HyperBlueDark,
    secondary = Color(0xFFE6E6E6),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFF0F0F0),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = HyperBlueLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEAF2FF),
    onTertiaryContainer = HyperBlueLight,
    background = HyperSurfaceLight,
    onBackground = Color.Black,
    surface = HyperCardLight,
    onSurface = Color.Black,
    surfaceVariant = HyperCardLight,
    onSurfaceVariant = Color(0xFF5F5F5F),
    surfaceTint = HyperBlueLight,
    inverseSurface = Color(0xFF1A1A1A),
    inverseOnSurface = Color(0xFFF2F2F2),
    error = Color(0xFFE94634),
    onError = Color.White,
    errorContainer = Color(0xFFFDF6F4),
    onErrorContainer = Color(0xFF8C1D18),
    outline = Color(0xFFD9D9D9),
    outlineVariant = Color(0xFFE8E8E8),
    scrim = Color.Black
)

private val HyperDarkColorScheme = darkColorScheme(
    primary = HyperBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2B3B54),
    onPrimaryContainer = HyperBlueDark,
    inversePrimary = HyperBlueLight,
    secondary = Color(0xFF505050),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF434343),
    onSecondaryContainer = Color(0xFFD9D9D9),
    tertiary = HyperBlueDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2B3B54),
    onTertiaryContainer = HyperBlueDark,
    background = HyperSurfaceDark,
    onBackground = Color(0xFFF2F2F2),
    surface = HyperCardDark,
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = HyperCardDark,
    onSurfaceVariant = Color(0xFFA6A6A6),
    surfaceTint = HyperBlueDark,
    inverseSurface = Color(0xFFE6E6E6),
    inverseOnSurface = Color(0xFF1A1A1A),
    error = Color(0xFFF12522),
    onError = Color.White,
    errorContainer = Color(0xFF2E0603),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF333333),
    scrim = Color.Black
)

// 与 Material3 系统默认完全一致（4/8/12/16/28），保证 Material3 模式像素级不变
private val MaterialShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// HyperOS 层级化圆角：12/16/20/24，整体更圆润但保留组件间层级差异
private val HyperShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val LocalThemeStyle = compositionLocalOf { ThemeStyle.Material3 }

// M3 默认 token 无法精确表达的硬编码圆角（如 14dp/6dp），按主题风格分别取值，
// 保证 Material3 模式数值与硬编码时期完全一致
@Composable
fun themedCornerShape(materialDp: Dp, hyperDp: Dp): RoundedCornerShape =
    RoundedCornerShape(if (LocalThemeStyle.current == ThemeStyle.HyperOS) hyperDp else materialDp)

// HyperOS 卡片为白/深灰（surfaceVariant 与卡片同色），M3 Switch 未选中轨道默认
// 也取 surfaceVariant，会导致轨道融入卡片。HyperOS 下改用 secondaryContainer 灰色
// 轨道 + 白色滑块 + 无边框的 MIUI 风格；Material3 模式返回全默认，行为与此前完全一致。
@Composable
fun themedSwitchColors(): SwitchColors =
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) SwitchDefaults.colors(
        uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
        uncheckedThumbColor = Color.White,
        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
    ) else SwitchDefaults.colors()

@Composable
fun TempMailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeStyle: ThemeStyle = ThemeStyle.Material3,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeStyle) {
        ThemeStyle.HyperOS -> if (darkTheme) HyperDarkColorScheme else HyperLightColorScheme
        ThemeStyle.Material3 -> if (darkTheme) DarkColorScheme else LightColorScheme
    }
    val shapes = when (themeStyle) {
        ThemeStyle.HyperOS -> HyperShapes
        ThemeStyle.Material3 -> MaterialShapes
    }
    CompositionLocalProvider(LocalThemeStyle provides themeStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
            typography = Typography
        ) {
            // HyperOS 主题下再叠加一层 Miuix 主题，供 Miuix 组件读取其颜色与文字样式；
            // Material3 主题直接透传，组件与像素表现与迁移前一致。
            MiuixThemeIfNeeded(themeStyle = themeStyle, darkTheme = darkTheme) {
                content()
            }
        }
    }
}
