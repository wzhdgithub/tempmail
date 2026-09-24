package com.tempmail.app.ui.screens.settings

// HyperOS 主题设置页 + 其专用图标/预览/强调色下拉 —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。


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
@Composable
internal fun ThemeSettingsPage(
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    onState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var extracting by remember { mutableStateOf(false) }
    var accentMenuOpen by remember { mutableStateOf(false) }

    val monetOn = state.dynamicSeed != NoDynamicSeed
    val floatingOn = state.barStyle != BarStyle.Edge

    // 相册取色：与 Material3 版莫奈页共用同一条链路（SeedExtractor 内部切 IO 线程）
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            extracting = true
            val seed = extractSeedFromUri(ctx, uri)
            extracting = false
            if (seed != null) onState(state.copy(dynamicSeed = seed))
            else snackbar.showSnackbar(s.monetFailed)
        }
    }

    // 标题栏：左返回 + 居中大标题（改动已实时生效，返回只是离开本页）
    Box(Modifier.fillMaxWidth()) {
        ThemedIconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
        }
        Text(s.themeSettings, style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center))
    }
    Spacer(Modifier.height(20.dp))

    // 预览区域：与真实界面同源取色，实时反映明暗 / 种子色 / 底栏形态
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ThemePreviewMock(
            barStyle = state.barStyle,
            label = { tab -> tabLabelOf(tab, s) }
        )
    }
    Spacer(Modifier.height(28.dp))

    // 明暗三选一：跟随系统 / 浅色 / 深色
    ThemedSegmentedTabs(
        tabs = ThemeMode.entries.map { themeModeLabel(it, s) },
        selectedIndex = ThemeMode.entries.indexOf(state.themeMode),
        onSelect = { onState(state.copy(themeMode = ThemeMode.entries[it])) }
    )
    Spacer(Modifier.height(16.dp))

    // 卡片1：莫奈取色
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            ThemedListRow(
                title = s.monetEnable,
                icon = { ImageFrameIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = monetOn,
                        onCheckedChange = { on ->
                            // 开启时先落到「默认」强调色（应用主色蓝），随后可再选预设或图片
                            onState(state.copy(dynamicSeed = if (on) DefaultMonetSeed else NoDynamicSeed))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.monetAccent,
                icon = { Icon(Icons.Default.Edit, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                onClick = { accentMenuOpen = true },
                trailing = {
                    ThemedDropdownValue(
                        text = if (monetOn && state.dynamicSeed != DefaultMonetSeed)
                            s.monetAccentCustom else s.monetAccentDefault,
                        onClick = { accentMenuOpen = true }
                    )
                }
            )
            // 零高度锚点：紧贴该行下方，弹出菜单以它为基准向下展开
            Box(Modifier.fillMaxWidth().height(0.dp)) {
                AccentDropdown(
                    expanded = accentMenuOpen,
                    selectedSeed = state.dynamicSeed,
                    extracting = extracting,
                    s = s,
                    onDismiss = { accentMenuOpen = false },
                    onPick = { seed -> onState(state.copy(dynamicSeed = seed)) },
                    onPickImage = {
                        accentMenuOpen = false
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // 卡片2：底栏相关
    // 状态机（BarStyle，三项枚举让"无效组合"从类型上就不存在）：
    //   悬浮关 → 贴边底栏（图7 的贴边样式，液态玻璃不可用）
    //   悬浮开 + 液态玻璃关 → 普通悬浮底栏
    //   悬浮开 + 液态玻璃开 → 悬浮液态玻璃底栏
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            ThemedListRow(
                title = s.barStyleFloat,
                summary = s.barFloatDesc,
                icon = { FloatingBarIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = floatingOn,
                        onCheckedChange = { on ->
                            // 关掉悬浮时液态玻璃一并关闭（贴边底栏没有玻璃形态）
                            onState(state.copy(barStyle = if (on) BarStyle.Float else BarStyle.Edge))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.barStyleGlass,
                summary = s.barGlassDesc,
                icon = { GlassDropIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = state.barStyle == BarStyle.LiquidGlass,
                        enabled = floatingOn,
                        onCheckedChange = { on ->
                            onState(state.copy(barStyle = if (on) BarStyle.LiquidGlass else BarStyle.Float))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.barBlur,
                summary = s.barBlurDesc,
                icon = { BlurDotsIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    // 模糊只作用于液态玻璃底栏：未选中玻璃时该项不可用，避免出现"无效组合"
                    ThemedSwitch(
                        checked = state.glassBlurEnabled,
                        enabled = state.barStyle == BarStyle.LiquidGlass,
                        onCheckedChange = { onState(state.copy(glassBlurEnabled = it)) }
                    )
                }
            )
        }
    }
    Spacer(Modifier.height(24.dp))
}

/** 图片取色图标：圆角画框 + 太阳 + 山形（Material 图标集中没有 image，这里按参考图手绘）。 */
@Composable
internal fun ImageFrameIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val stroke = size.width * 0.085f
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(size.width * 0.2f),
            style = Stroke(width = stroke)
        )
        drawCircle(color = tint, radius = size.width * 0.075f,
            center = Offset(size.width * 0.33f, size.height * 0.35f))
        val hill = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.76f)
            lineTo(size.width * 0.43f, size.height * 0.50f)
            lineTo(size.width * 0.60f, size.height * 0.68f)
            lineTo(size.width * 0.70f, size.height * 0.58f)
            lineTo(size.width * 0.80f, size.height * 0.76f)
            close()
        }
        drawPath(hill, tint)
    }
}

/**
 * 手机预览示意图（实时预览）：完整反映当前主题的各处变化——
 *   - 配色：页面底色 / 卡片 / 强调容器 / 强调色 / 文字色，全部取自 themedSurfaceColors()
 *     （HyperOS 下就是 Miuix 组件真正在用的那套颜色），并用 260ms 颜色动画平滑过渡；
 *   - 底栏形态：悬浮（内缩 + 圆角 + 抬离底边）↔ 贴边（满宽 + 直角 + 紧贴底边）之间平滑位移/形变；
 *   - 尺寸与圆角：机身与色块的圆角取自当前主题的 shapes token，因此换主题风格时形状随之变化；
 *   - 字体：底栏项文字用当前主题的 labelSmall 渲染，字体样式变化同样可见。
 */
@Composable
internal fun ThemePreviewMock(
    barStyle: BarStyle,
    label: (Tab) -> String,
    modifier: Modifier = Modifier
) {
    val colors = themedSurfaceColors()
    val shapeSpec = tween<Color>(THEME_ANIM_MS)
    val sizeSpec = tween<Dp>(THEME_ANIM_MS)

    // 颜色：全部带动画，明暗/种子色切换时平滑渐变
    val background by animateColorAsState(colors.background, shapeSpec, label = "previewBackground")
    val card by animateColorAsState(colors.card, shapeSpec, label = "previewCard")
    val accent by animateColorAsState(colors.primary, shapeSpec, label = "previewAccent")
    val accentContainer by animateColorAsState(colors.primaryContainer, shapeSpec, label = "previewAccentContainer")
    val onSurface by animateColorAsState(colors.onSurface, shapeSpec, label = "previewOnSurface")
    val outline by animateColorAsState(colors.outlineVariant, shapeSpec, label = "previewOutline")

    // 形状：跟随主题的 shapes token（Material3 / Miuix 的圆角层级不同）
    val shapes = MaterialTheme.shapes
    val bodyRadius by animateDpAsState(cornerRadiusOf(shapes.extraLarge), sizeSpec, label = "previewBodyRadius")
    val blockRadius by animateDpAsState(cornerRadiusOf(shapes.small), sizeSpec, label = "previewBlockRadius")

    // 底栏形态：悬浮 = 内缩 + 圆角 + 抬离底边；贴边 = 满宽 + 直角 + 紧贴底边
    val floating = barStyle != BarStyle.Edge
    val barInset by animateDpAsState(if (floating) 8.dp else 0.dp, sizeSpec, label = "previewBarInset")
    val barCorner by animateDpAsState(if (floating) blockRadius else 0.dp, sizeSpec, label = "previewBarCorner")
    val barLift by animateDpAsState(if (floating) 6.dp else 0.dp, sizeSpec, label = "previewBarLift")
    val barHeight by animateDpAsState(if (floating) 30.dp else 34.dp, sizeSpec, label = "previewBarHeight")

    Box(
        modifier = modifier
            .width(132.dp)
            .height(206.dp)
            .clip(RoundedCornerShape(bodyRadius))
            .background(background)
            .border(1.5.dp, outline, RoundedCornerShape(bodyRadius))
    ) {
        // 内容区（含底部为底栏预留的高度，避免与底栏重叠）
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 10.dp, end = 10.dp)
                .padding(bottom = barHeight + barLift + 6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(30.dp)
                    .clip(RoundedCornerShape(blockRadius)).background(accentContainer))
                Box(Modifier.weight(1f).height(30.dp)
                    .clip(RoundedCornerShape(blockRadius)).background(card))
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().weight(1f)
                .clip(RoundedCornerShape(blockRadius)).background(card))
        }

        // 底栏：与真实底栏同构（4 个项 + 首项强调色），形态随 BarStyle 平滑变化
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = barInset, end = barInset, bottom = barLift)
                .fillMaxWidth()
                .clip(RoundedCornerShape(barCorner))
                .background(card)
        ) {
            // 贴边形态用一条顶部分割线与内容分层（悬浮形态下分割线随圆角淡出）
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer { alpha = if (floating) 0f else 1f }
                    .background(outline)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .padding(horizontal = if (floating) 6.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tab.entries.forEachIndexed { index, tab ->
                    val selected = index == 0
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (selected) accent else onSurface)
                        )
                        Text(
                            text = label(tab),
                            // 沿用主题 labelSmall 的字体族与字重，只把字号缩到示意用的尺寸
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                lineHeight = 8.sp
                            ),
                            color = if (selected) accent else onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/** 模糊图标：半调网点（呼应参考图里的模糊图标）。 */
@Composable
internal fun BlurDotsIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val step = size.width / 4f
        repeat(4) { row ->
            repeat(4) { col ->
                // 右下角逐渐变淡变小，形成"虚化"观感
                val fade = 1f - (row + col) / 7f
                drawCircle(
                    color = tint.copy(alpha = 0.35f + 0.65f * fade),
                    radius = step * 0.17f * (0.6f + 0.4f * fade),
                    center = Offset(step * (col + 0.5f), step * (row + 0.5f))
                )
            }
        }
    }
}

/** 悬浮底栏图标：圆角矩形机身 + 底部的实心条。 */
@Composable
internal fun FloatingBarIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val stroke = size.width * 0.085f
        val radius = size.width * 0.2f
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = stroke)
        )
        val barHeight = size.height * 0.22f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.24f, size.height * 0.62f),
            size = Size(size.width * 0.52f, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f)
        )
    }
}

/** 液态玻璃图标：水滴。 */
@Composable
internal fun GlassDropIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val r = size.width * 0.29f
        val center = Offset(size.width / 2f, size.height * 0.64f)
        drawCircle(color = tint, radius = r, center = center)
        val path = Path().apply {
            moveTo(size.width / 2f, size.height * 0.06f)
            lineTo(center.x + r * 0.82f, center.y - r * 0.5f)
            lineTo(center.x - r * 0.82f, center.y - r * 0.5f)
            close()
        }
        drawPath(path, tint)
    }
}

/**
 * 「强调色」下拉菜单：默认（应用主色蓝）/ 预设色带 / 从图片取色。
 * 用 Popup 锚定在强调色行下方，带淡入 + 轻微缩放的过渡（与底栏放大镜的出现动效一致）。
 */
@Composable
internal fun AccentDropdown(
    expanded: Boolean,
    selectedSeed: Int,
    extracting: Boolean,
    s: Strings,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit,
    onPickImage: () -> Unit
) {
    if (!expanded) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(180)) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, 6),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        ThemedCard(
            modifier = Modifier
                .width(268.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .graphicsLayer {
                    alpha = progress.value
                    val scale = 0.94f + 0.06f * progress.value
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(1f, 0f)
                },
            shape = MaterialTheme.shapes.large
        ) {
            Column {
                AccentMenuRow(
                    label = s.monetAccentDefault,
                    selected = selectedSeed == DefaultMonetSeed,
                    swatch = Color(DefaultMonetSeed),
                    onClick = { onPick(DefaultMonetSeed); onDismiss() }
                )
                ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    s.monetPresets,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonetPresets.forEach { preset ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(preset))
                                .border(
                                    width = if (selectedSeed == preset) 3.dp else 1.dp,
                                    color = if (selectedSeed == preset) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { onPick(preset); onDismiss() }
                        )
                    }
                }
                ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AccentMenuRow(
                    label = if (extracting) s.monetExtracting else s.monetPickImage,
                    selected = selectedSeed != DefaultMonetSeed && selectedSeed !in MonetPresets
                        && selectedSeed != NoDynamicSeed,
                    icon = { Icon(Icons.Default.Create, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                    onClick = onPickImage
                )
            }
        }
    }
}

/** 下拉菜单中的一行：可选色点 + 文案（选中时显示对勾）。 */
@Composable
internal fun AccentMenuRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatch: Color? = null,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (swatch != null) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(swatch))
            Spacer(Modifier.width(14.dp))
        } else if (icon != null) {
            icon()
            Spacer(Modifier.width(14.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * 莫奈取色子页：选择图片取色 / 预设色 / 配色风格 / 对比度 / 关闭。
 * 仅在 Material3 主题下可达（Miuix 主题的取色入口在「主题设置」页内）。
 */
