package com.tempmail.app.ui.theme

// Miuix 组件桥接层：
//   - ThemeStyle.HyperOS 时使用真正的 Miuix（HyperOS）组件；
//   - ThemeStyle.Material3 时仍走原有 Material3 组件，保证该主题与迁移前保持一致。
// 组件按主题二选一，调用方只需把 M3 组件名换成 Themed* 封装。

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tempmail.app.ui.theme.dynamic.DynamicStyle
import top.yukonga.miuix.kmp.basic.BasicComponent as MiuixBasicComponent
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

/**
 * 动态配色（莫奈取色）下的 Miuix 配色：由 Material 3 动态配色逐 token 映射而来。
 *
 * 关键点：卡片与页面都要**带上种子色的色调**，不能在浅色下映射到 surfaceContainerLowest
 * （那是纯白）——否则开启莫奈后页面底色变了、卡片仍是固定白色，出现视觉冲突。
 * 层级关系保持"卡片与页面相差一档"：浅色下页面取 surfaceContainerLow（最浅的一档），
 * 卡片取 surfaceContainer；深色下页面取 surfaceContainerLowest（最暗的一档），卡片取 surfaceContainer（更亮）。
 */
private fun dynamicMiuixColors(scheme: ColorScheme, dark: Boolean): Colors = if (dark) {
    darkColorScheme(
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer,
        secondary = scheme.secondary,
        onSecondary = scheme.onSecondary,
        secondaryContainer = scheme.secondaryContainer,
        onSecondaryContainer = scheme.onSecondaryContainer,
        tertiaryContainer = scheme.tertiaryContainer,
        onTertiaryContainer = scheme.onTertiaryContainer,
        error = scheme.error,
        onError = scheme.onError,
        // 深色：页面取最暗的一档，卡片亮一档（同样是带色调的深灰）
        background = scheme.surfaceContainerLowest,
        onBackground = scheme.onSurface,
        onBackgroundVariant = scheme.onSurfaceVariant,
        surface = scheme.surfaceContainer,
        onSurface = scheme.onSurface,
        surfaceVariant = scheme.surfaceContainer,
        onSurfaceSecondary = scheme.onSurfaceVariant,
        onSurfaceVariantSummary = scheme.onSurfaceVariant,
        onSurfaceVariantActions = scheme.onSurfaceVariant,
        surfaceContainer = scheme.surfaceContainer,
        onSurfaceContainer = scheme.onSurface,
        surfaceContainerHigh = scheme.surfaceContainerHigh,
        surfaceContainerHighest = scheme.surfaceContainerHighest,
        outline = scheme.outline,
        dividerLine = scheme.outlineVariant,
        windowDimming = Color(0x99000000)
    )
} else {
    lightColorScheme(
        primary = scheme.primary,
        onPrimary = scheme.onPrimary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer,
        secondary = scheme.secondary,
        onSecondary = scheme.onSecondary,
        secondaryContainer = scheme.secondaryContainer,
        onSecondaryContainer = scheme.onSecondaryContainer,
        tertiaryContainer = scheme.tertiaryContainer,
        onTertiaryContainer = scheme.onTertiaryContainer,
        error = scheme.error,
        onError = scheme.onError,
        // 浅色：页面取最浅的一档（surfaceContainerLow），卡片取 surfaceContainer —— 两者都带种子色色调
        background = scheme.surfaceContainerLow,
        onBackground = scheme.onSurface,
        onBackgroundVariant = scheme.onSurfaceVariant,
        surface = scheme.surfaceContainer,
        onSurface = scheme.onSurface,
        surfaceVariant = scheme.surfaceContainer,
        onSurfaceSecondary = scheme.onSurfaceVariant,
        onSurfaceVariantSummary = scheme.onSurfaceVariant,
        onSurfaceVariantActions = scheme.onSurfaceVariant,
        surfaceContainer = scheme.surfaceContainer,
        onSurfaceContainer = scheme.onSurface,
        surfaceContainerHigh = scheme.surfaceContainerHigh,
        surfaceContainerHighest = scheme.surfaceContainerHighest,
        outline = scheme.outline,
        dividerLine = scheme.outlineVariant,
        windowDimming = Color(0x66000000)
    )
}

/**
 * 把 Miuix 配色做成"可平滑过渡"的配色：与 MaterialTheme 侧用同一条节奏（THEME_ANIM_MS）逐 token 插值。
 * 未列出的 token 保持目标的原值（`Colors.copy` 带默认参数），因此不会出现"部分 token 被工厂默认值覆盖"的问题。
 */
@Composable
private fun animateMiuixColors(target: Colors): Colors {
    @Composable
    fun anim(label: String, select: (Colors) -> Color): Color =
        animateColorAsState(
            targetValue = select(target),
            animationSpec = tween(THEME_ANIM_MS),
            label = label
        ).value
    return target.copy(
        primary = anim("primary") { it.primary },
        onPrimary = anim("onPrimary") { it.onPrimary },
        primaryVariant = anim("primaryVariant") { it.primaryVariant },
        onPrimaryVariant = anim("onPrimaryVariant") { it.onPrimaryVariant },
        primaryContainer = anim("primaryContainer") { it.primaryContainer },
        onPrimaryContainer = anim("onPrimaryContainer") { it.onPrimaryContainer },
        secondary = anim("secondary") { it.secondary },
        onSecondary = anim("onSecondary") { it.onSecondary },
        secondaryVariant = anim("secondaryVariant") { it.secondaryVariant },
        onSecondaryVariant = anim("onSecondaryVariant") { it.onSecondaryVariant },
        secondaryContainer = anim("secondaryContainer") { it.secondaryContainer },
        onSecondaryContainer = anim("onSecondaryContainer") { it.onSecondaryContainer },
        secondaryContainerVariant = anim("secondaryContainerVariant") { it.secondaryContainerVariant },
        onSecondaryContainerVariant = anim("onSecondaryContainerVariant") { it.onSecondaryContainerVariant },
        tertiaryContainer = anim("tertiaryContainer") { it.tertiaryContainer },
        onTertiaryContainer = anim("onTertiaryContainer") { it.onTertiaryContainer },
        tertiaryContainerVariant = anim("tertiaryContainerVariant") { it.tertiaryContainerVariant },
        error = anim("error") { it.error },
        onError = anim("onError") { it.onError },
        errorContainer = anim("errorContainer") { it.errorContainer },
        onErrorContainer = anim("onErrorContainer") { it.onErrorContainer },
        background = anim("background") { it.background },
        onBackground = anim("onBackground") { it.onBackground },
        onBackgroundVariant = anim("onBackgroundVariant") { it.onBackgroundVariant },
        surface = anim("surface") { it.surface },
        onSurface = anim("onSurface") { it.onSurface },
        surfaceVariant = anim("surfaceVariant") { it.surfaceVariant },
        onSurfaceSecondary = anim("onSurfaceSecondary") { it.onSurfaceSecondary },
        onSurfaceVariantSummary = anim("onSurfaceVariantSummary") { it.onSurfaceVariantSummary },
        onSurfaceVariantActions = anim("onSurfaceVariantActions") { it.onSurfaceVariantActions },
        surfaceContainer = anim("surfaceContainer") { it.surfaceContainer },
        onSurfaceContainer = anim("onSurfaceContainer") { it.onSurfaceContainer },
        onSurfaceContainerVariant = anim("onSurfaceContainerVariant") { it.onSurfaceContainerVariant },
        surfaceContainerHigh = anim("surfaceContainerHigh") { it.surfaceContainerHigh },
        onSurfaceContainerHigh = anim("onSurfaceContainerHigh") { it.onSurfaceContainerHigh },
        surfaceContainerHighest = anim("surfaceContainerHighest") { it.surfaceContainerHighest },
        onSurfaceContainerHighest = anim("onSurfaceContainerHighest") { it.onSurfaceContainerHighest },
        outline = anim("outline") { it.outline },
        dividerLine = anim("dividerLine") { it.dividerLine },
        windowDimming = anim("windowDimming") { it.windowDimming },
        disabledPrimary = anim("disabledPrimary") { it.disabledPrimary },
        disabledOnPrimary = anim("disabledOnPrimary") { it.disabledOnPrimary },
        disabledPrimaryButton = anim("disabledPrimaryButton") { it.disabledPrimaryButton },
        disabledOnPrimaryButton = anim("disabledOnPrimaryButton") { it.disabledOnPrimaryButton },
        disabledSecondary = anim("disabledSecondary") { it.disabledSecondary },
        disabledOnSecondary = anim("disabledOnSecondary") { it.disabledOnSecondary },
        disabledSecondaryVariant = anim("disabledSecondaryVariant") { it.disabledSecondaryVariant },
        disabledOnSecondaryVariant = anim("disabledOnSecondaryVariant") { it.disabledOnSecondaryVariant },
        disabledOnSurface = anim("disabledOnSurface") { it.disabledOnSurface },
        sliderKeyPoint = anim("sliderKeyPoint") { it.sliderKeyPoint },
        sliderKeyPointForeground = anim("sliderKeyPointForeground") { it.sliderKeyPointForeground },
        sliderBackground = anim("sliderBackground") { it.sliderBackground }
    )
}

/**
 * HyperOS 主题下包一层 Miuix 主题（提供 Miuix 颜色/文字样式）；Material3 主题直接透传。
 *
 * dynamicScheme 非空（用户启用了莫奈取色）时，Miuix 配色改由该动态配色映射而来；
 * 为空时沿用项目手工定义的 HyperOS 配色，与启用前逐像素一致。
 * 两种情况下都会把配色接到颜色过渡上（与 MaterialTheme 同步渐变，避免出现"半边先变色"）。
 */
@Composable
fun MiuixThemeIfNeeded(
    themeStyle: ThemeStyle,
    darkTheme: Boolean,
    dynamicScheme: ColorScheme? = null,
    content: @Composable () -> Unit
) {
    if (themeStyle == ThemeStyle.HyperOS) {
        val targetColors = if (dynamicScheme == null) hyperMiuixColors(darkTheme)
        else dynamicMiuixColors(dynamicScheme, darkTheme)
        MiuixTheme(colors = animateMiuixColors(targetColors), content = content)
    } else {
        content()
    }
}

/**
 * 取形状的圆角半径（dp）：用于把主题的 shapes token 传给需要具体数值的场景
 * （如 Miuix Card 的 cornerRadius、主题设置页预览示意图的圆角）。
 */
internal fun cornerRadiusOf(shape: Shape): Dp =
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

/**
 * 底栏容器色：HyperOS 取 Miuix 的容器色，Material3 维持原有的 surfaceVariant。
 * 未开启莫奈时两者数值相同（Miuix 侧映射的就是 HyperCardLight/Dark），
 * 开启莫奈后 Miuix 容器色会带上种子色色调，避免底栏仍是固定白色。
 */
@Composable
fun themedBarContainerColor(): Color =
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) MiuixTheme.colorScheme.surfaceVariant
    else MaterialTheme.colorScheme.surfaceVariant

/**
 * 当前主题下"实际会被用到的"几个面/色 token。
 * HyperOS 取 Miuix 色板（组件真正用的那套），Material3 取 MaterialTheme；
 * 供预览示意图等跨主题组件取色，避免直接读 MaterialTheme 而漏掉 Miuix 的映射结果。
 */
@Immutable
data class ThemedSurfaceColors(
    val background: Color,
    val card: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onSurface: Color,
    val outlineVariant: Color
)

@Composable
fun themedSurfaceColors(): ThemedSurfaceColors =
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        val c = MiuixTheme.colorScheme
        ThemedSurfaceColors(
            background = c.background,
            card = c.surfaceVariant,
            primary = c.primary,
            primaryContainer = c.primaryContainer,
            onSurface = c.onBackground,
            outlineVariant = c.dividerLine
        )
    } else {
        val c = MaterialTheme.colorScheme
        ThemedSurfaceColors(
            background = c.background,
            card = c.surfaceVariant,
            primary = c.primary,
            primaryContainer = c.primaryContainer,
            onSurface = c.onSurface,
            outlineVariant = c.outlineVariant
        )
    }

/**
 * 分段控件（三选一）：选中项为实心胶囊 + 深色文字，未选中项透明。
 * HyperOS 下取 Miuix 容层级色（surfaceContainerHigh），Material3 下取 secondaryContainer；
 * 颜色切换带 200ms 过渡（与底栏放大镜的出现动效同一节奏）。
 */
@Composable
fun ThemedSegmentedTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hyper = LocalThemeStyle.current == ThemeStyle.HyperOS
    val selectedBackground = if (hyper) MiuixTheme.colorScheme.surfaceContainerHigh
    else MaterialTheme.colorScheme.secondaryContainer
    val selectedContent = if (hyper) MiuixTheme.colorScheme.onSurfaceContainerHigh
    else MaterialTheme.colorScheme.onSecondaryContainer
    val normalContent = if (hyper) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val background by animateColorAsState(
                targetValue = if (selected) selectedBackground else Color.Transparent,
                animationSpec = tween(200),
                label = "segmentedBackground"
            )
            val contentColor by animateColorAsState(
                targetValue = if (selected) selectedContent else normalContent,
                animationSpec = tween(200),
                label = "segmentedContent"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(background)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = if (hyper) MiuixTheme.textStyles.body1 else MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * 设置行：左侧图标 + 主标题（可选副标题）+ 右侧控件。
 * HyperOS 用 Miuix 的 BasicComponent（自带 MIUI 排版与按压缩放反馈），
 * Material3 用手写等高行，保证该子页在两种主题下都可用。
 */
@Composable
fun ThemedListRow(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    icon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    if (LocalThemeStyle.current == ThemeStyle.HyperOS) {
        // Miuix 的 startAction/endActions 是可选插槽，用恒非空 lambda 包一层避免可空类型差异
        MiuixBasicComponent(
            title = title,
            summary = summary,
            startAction = { icon?.invoke() },
            endActions = { trailing?.invoke(this) },
            onClick = onClick ?: {},
            modifier = modifier
        )
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(16.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (summary != null) {
                    Text(
                        summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}

/** 下拉控件的收起态：灰色当前值 + 上下双箭头（Miuix 的 Spinner 观感）。 */
@Composable
fun ThemedDropdownValue(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hyper = LocalThemeStyle.current == ThemeStyle.HyperOS
    val contentColor = if (hyper) MiuixTheme.colorScheme.onSurfaceVariantSummary
    else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = if (hyper) MiuixTheme.textStyles.body2 else MaterialTheme.typography.bodyMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(4.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}