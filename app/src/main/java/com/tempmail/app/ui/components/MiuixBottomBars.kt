package com.tempmail.app.ui.components

// Miuix 主题三形态底栏 + 选中项组件 —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.provider.Settings
import android.text.Html
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import com.tempmail.app.ui.glass.GlassBarItem
import com.tempmail.app.ui.glass.GlassBarSpace
import com.tempmail.app.ui.glass.GlassShell
import com.tempmail.app.ui.glass.isGlassBlurSupported
import com.tempmail.app.ui.theme.TempMailTheme
import com.tempmail.app.ui.theme.THEME_ANIM_MS
import com.tempmail.app.ui.theme.ThemeMode
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.dynamic.DefaultMonetSeed
import com.tempmail.app.ui.theme.dynamic.DynamicStyle
import com.tempmail.app.ui.theme.dynamic.MonetPresets
import com.tempmail.app.ui.theme.dynamic.NoDynamicSeed
import com.tempmail.app.ui.theme.dynamic.extractSeedFromUri
import com.tempmail.app.ui.theme.ThemedButton
import com.tempmail.app.ui.theme.ThemedCard
import com.tempmail.app.ui.theme.ThemedDivider
import com.tempmail.app.ui.theme.ThemedDropdownValue
import com.tempmail.app.ui.theme.ThemedIconButton
import com.tempmail.app.ui.theme.ThemedLinearProgress
import com.tempmail.app.ui.theme.ThemedListRow
import com.tempmail.app.ui.theme.ThemedPollCountdown
import com.tempmail.app.ui.theme.ThemedSegmentedTabs
import com.tempmail.app.ui.theme.ThemedSwitch
import com.tempmail.app.ui.theme.ThemedTextButton
import com.tempmail.app.ui.theme.cornerRadiusOf
import com.tempmail.app.ui.theme.themedBarContainerColor
import com.tempmail.app.ui.theme.themedCornerShape
import com.tempmail.app.ui.theme.themedSurfaceColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import kotlin.random.Random
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import com.tempmail.app.data.*
import com.tempmail.app.i18n.*
import com.tempmail.app.model.*
import com.tempmail.app.ui.screens.settings.SettingsTab
@Composable
internal fun MiuixFloatingBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    // 指示器必须用不透明色：M3 绘制时以 .copy(alpha = animationProgress)
    // 覆盖该色的 alpha（选中稳定后为 1f），传入带透明度的颜色会被静默还原成实心色。
    // 故按 12% 比例预先合成到容器色上，亮/暗模式均自动匹配底色。
    // 容器色走主题桥接：开启莫奈后取带色调的 Miuix 容器色，不再是固定白色
    val barColor = themedBarContainerColor()
    val indicatorColor = MaterialTheme.colorScheme.primary
        .copy(alpha = 0.12f)
        .compositeOver(barColor)
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp)
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                clip = true
            ),
        containerColor = barColor,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp),
        content = {
            Tab.entries.forEach { tab ->
                val selected = current == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            tab.icon,
                            label(tab),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            label(tab),
                            color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = indicatorColor
                    )
                )
            }
        }
    )
}

/**
 * 贴边底栏：悬浮关闭时的形态（对应参考图7）。
 * 与悬浮底栏同一套色板与选中态规则，但**不浮起**——铺满整宽、不留横向边距、不投影，
 * 靠顶部一条细分隔线与内容分层，底栏背景一直延伸到屏幕底边。
 */
@Composable
internal fun MiuixEdgeBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    val barColor = themedBarContainerColor()
    Column(
        Modifier
            .fillMaxWidth()
            .background(barColor)
    ) {
        ThemedDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                val selected = current == tab
                val contentColor = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(tab) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                ) {
                    Icon(tab.icon, label(tab), tint = contentColor,
                        modifier = Modifier.size(24.dp))
                    Text(label(tab), style = MaterialTheme.typography.labelSmall,
                        color = contentColor)
                }
            }
        }
    }
}

/**
 * 液态玻璃底栏（伪玻璃回退实现，用于 API 33 以下或模糊不可用的设备）：
 * 视觉与交互规格参考 skill-liquid-glass
 * （玻璃本体 + 高光描边 + 外层柔和阴影 + 滑动指示器 + 按压缩放回弹）。
 *
 * 注：真实模糊/折射由 ui/glass/GlassBottomBar.kt 在 API 33+ 提供；
 * 本组件仅以渐变 + 描边 + 阴影模拟玻璃质感，保证低版本观感一致性与稳定性。
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
internal fun LiquidGlassBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val tabs = Tab.entries
    val glassShape = RoundedCornerShape(28.dp)
    // 玻璃层次：不透明基底之上叠一层极淡的纵向明暗（顶部受光、底部压暗），形成厚度。
    // 基底必须不透明 —— 否则 10dp 阴影会从半透明本体下方透出，把底栏越往底部压得越灰。
    val bodyBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.10f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.05f)
        )
    )
    // 高光描边：左上来光最亮，过渡到极淡的主色收边
    val edgeBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.10f),
            scheme.primary.copy(alpha = 0.22f)
        )
    )
    // 不透明基底色：走主题桥接，开启莫奈后为带色调的 Miuix 容器色（未开启时与原来的 surface 同值）
    val baseColor = themedBarContainerColor()
    // 指示器由本组件自行绘制（不经过 M3 NavigationBar），故可直接使用半透明色，
    // 预合成到基底色上以保证在深浅两种背景下都有足够存在感
    val indicatorColor = scheme.primary.copy(alpha = 0.16f)
        .compositeOver(baseColor)
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // 阴影层：不透明底色承载投影，使阴影只出现在圆角轮廓之外
        Box(
            Modifier
                .matchParentSize()
                .shadow(elevation = 10.dp, shape = glassShape, clip = true)
                .background(baseColor, glassShape)
        )
        BoxWithConstraints(
            Modifier
                .matchParentSize()
                .background(bodyBrush, glassShape)
                .border(1.dp, edgeBrush, glassShape)
        ) {
            val itemWidth = maxWidth / tabs.size
            val indicatorWidth = minOf(64.dp, itemWidth - 8.dp)
            val index = tabs.indexOf(current).coerceAtLeast(0)
            val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val slot = if (rtl) tabs.size - 1 - index else index
            val indicatorX by animateDpAsState(
                targetValue = itemWidth * slot + (itemWidth - indicatorWidth) / 2,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                label = "glassIndicatorX"
            )
            // 滑动指示器：绘制在图标之下
            Box(
                Modifier
                    .offset(x = indicatorX, y = 8.dp)
                    .size(indicatorWidth, 32.dp)
                    .background(indicatorColor, RoundedCornerShape(percent = 50))
            )
            // 顶部受光已由 bodyBrush 的渐变承担，此处不再叠加内高光
            Row(
                Modifier.fillMaxSize().selectableGroup(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    GlassTabItem(
                        modifier = Modifier.weight(1f),
                        tab = tab,
                        selected = current == tab,
                        label = label(tab),
                        onClick = { onSelect(tab) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun GlassTabItem(
    modifier: Modifier,
    tab: Tab,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 按压回弹：按下收缩、松开弹回，模拟玻璃被按压的液体反馈
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMedium),
        label = "glassPressScale"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tab.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant
        )
    }
}
