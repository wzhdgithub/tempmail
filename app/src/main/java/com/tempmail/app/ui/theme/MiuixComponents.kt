package com.tempmail.app.ui.theme

// Miuix 组件桥接层：
//   - ThemeStyle.HyperOS 时使用真正的 Miuix（HyperOS）组件；
//   - ThemeStyle.Material3 时仍走原有 Material3 组件，保证该主题与迁移前保持一致。
// 组件按主题二选一，调用方只需把 M3 组件名换成 Themed* 封装。

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.HorizontalDivider as MiuixDivider
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator as MiuixLinearProgress
import top.yukonga.miuix.kmp.basic.Switch as MiuixSwitch
import top.yukonga.miuix.kmp.basic.SwitchDefaults as MiuixSwitchDefaults
import top.yukonga.miuix.kmp.theme.Colors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import androidx.compose.material3.Button as M3Button
import androidx.compose.material3.Card as M3Card
import androidx.compose.material3.HorizontalDivider as M3Divider
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.LinearProgressIndicator as M3LinearProgress
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.TextButton as M3TextButton

/** 把本项目的 HyperOS 配色映射到 Miuix 颜色体系（仅覆盖用得到的关键 token）。 */
private fun hyperMiuixColors(dark: Boolean): Colors = if (dark) {
    darkColorScheme(
        primary = HyperBlueDark,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2B3B54),
        onPrimaryContainer = HyperBlueDark,
        secondary = Color(0xFF505050),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF434343),
        onSecondaryContainer = Color(0xFFD9D9D9),
        tertiaryContainer = Color(0xFF2B3B54),
        onTertiaryContainer = HyperBlueDark,
        background = HyperSurfaceDark,
        onBackground = Color(0xFFF2F2F2),
        onBackgroundVariant = Color(0xFFA6A6A6),
        surface = HyperCardDark,
        onSurface = Color(0xFFF2F2F2),
        surfaceVariant = HyperCardDark,
        onSurfaceSecondary = Color(0xFFA6A6A6),
        onSurfaceVariantSummary = Color(0xFF808080),
        onSurfaceVariantActions = Color(0xFF666666),
        surfaceContainer = HyperCardDark,
        onSurfaceContainer = Color(0xFFF2F2F2),
        surfaceContainerHigh = Color(0xFF222222),
        surfaceContainerHighest = Color(0xFF2B2B2B),
        outline = Color(0xFF404040),
        dividerLine = Color(0xFF333333),
        error = Color(0xFFF12522),
        windowDimming = Color(0x99000000)
    )
} else {
    lightColorScheme(
        primary = HyperBlueLight,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF5D9BFF),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFFE6E6E6),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0F0F0),
        onSecondaryContainer = Color(0xFF1A1A1A),
        tertiaryContainer = Color(0xFFEAF2FF),
        onTertiaryContainer = HyperBlueLight,
        background = HyperSurfaceLight,
        onBackground = Color.Black,
        onBackgroundVariant = Color(0xFF5F5F5F),
        surface = HyperCardLight,
        onSurface = Color.Black,
        surfaceVariant = HyperCardLight,
        onSurfaceSecondary = Color(0xFF5F5F5F),
        onSurfaceVariantSummary = Color(0xFF808080),
        onSurfaceVariantActions = Color(0xFF999999),
        surfaceContainer = HyperCardLight,
        onSurfaceContainer = Color.Black,
        surfaceContainerHigh = Color(0xFFF0F0F0),
        surfaceContainerHighest = Color(0xFFE6E6E6),
        outline = Color(0xFFD9D9D9),
        dividerLine = Color(0xFFE8E8E8),
        error = Color(0xFFE94634),
        windowDimming = Color(0x66000000)
    )
}

/** HyperOS 主题下包一层 Miuix 主题（提供 Miuix 颜色/文字样式）；Material3 主题直接透传。 */
@Composable
fun MiuixThemeIfNeeded(
    themeStyle: ThemeStyle,
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    if (themeStyle == ThemeStyle.HyperOS) {
        MiuixTheme(colors = hyperMiuixColors(darkTheme), content = content)
    } else {
        content()
    }
}

private fun cornerRadiusOf(shape: Shape): Dp =
    if (shape is RoundedCornerShape) {
        // 以 density=1 换算：px 值即 dp 值
        shape.topStart.toPx(Size(1000f, 1000f), Density(1f)).dp
    } else {
        20.dp
    }

@Composable
fun ThemedCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        // Miuix Card 自带内容内边距，这里置零以沿用调用方的 padding 布局
        MiuixCard(
            modifier = modifier,
            cornerRadius = cornerRadiusOf(shape),
            insideMargin = PaddingValues(0.dp),
            content = content
        )
    } else {
        M3Card(modifier = modifier, shape = shape, content = content)
    }
}

@Composable
fun ThemedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        MiuixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            colors = MiuixSwitchDefaults.switchColors(),
            enabled = enabled
        )
    } else {
        M3Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            colors = themedSwitchColors(),
            enabled = enabled
        )
    }
}

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.large,
    elevation: ButtonElevation? = null,
    content: @Composable RowScope.() -> Unit
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        MiuixButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            cornerRadius = cornerRadiusOf(shape),
            // Miuix 默认按钮是浅灰次要按钮，这里显式用主色以对齐本应用的主按钮观感
            colors = MiuixButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.primary,
                disabledColor = MiuixTheme.colorScheme.disabledPrimary,
                contentColor = MiuixTheme.colorScheme.onPrimary,
                disabledContentColor = MiuixTheme.colorScheme.disabledOnPrimary
            ),
            content = content
        )
    } else {
        M3Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = shape,
            elevation = elevation ?: ButtonDefaults.buttonElevation(),
            content = content
        )
    }
}

@Composable
fun ThemedTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        // Miuix 的 TextButton 只接受字符串文案，这里用文本配色的 Miuix Button 承载内容 lambda
        MiuixButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            minWidth = 0.dp,
            colors = MiuixButtonDefaults.buttonColors(
                color = Color.Transparent,
                disabledColor = Color.Transparent,
                contentColor = MiuixTheme.colorScheme.primary,
                disabledContentColor = MiuixTheme.colorScheme.disabledOnSecondaryVariant
            ),
            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            content = content
        )
    } else {
        M3TextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@Composable
fun ThemedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        MiuixIconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    } else {
        M3IconButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@Composable
fun ThemedDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        MiuixDivider(
            modifier = modifier,
            thickness = thickness,
            color = MiuixTheme.colorScheme.dividerLine
        )
    } else {
        M3Divider(modifier = modifier, thickness = thickness, color = color)
    }
}

@Composable
fun ThemedLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        MiuixLinearProgress(modifier = modifier, progress = progress())
    } else {
        M3LinearProgress(progress = progress, modifier = modifier)
    }
}