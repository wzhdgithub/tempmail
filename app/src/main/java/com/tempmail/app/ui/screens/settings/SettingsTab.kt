package com.tempmail.app.ui.screens.settings

// 设置主页与全部子页入口 —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
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
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin
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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.tempmail.app.data.*
import com.tempmail.app.BuildConfig
import com.tempmail.app.R
import com.tempmail.app.i18n.*
import com.tempmail.app.model.*
import com.tempmail.app.ui.theme.LocalThemeStyle
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
/** 明暗三态的显示文案（设置主页的值、深色模式子页、主题设置页的分段控件共用）。 */
internal fun themeModeLabel(mode: ThemeMode, s: Strings): String = when (mode) {
    ThemeMode.System -> s.themeFollowSystem
    ThemeMode.Light -> s.themeLight
    ThemeMode.Dark -> s.themeDark
}

/** 邮箱服务健康检测结果。testing=true 表示探测进行中；ok 三态：null=未检测。 */
internal data class ServiceStatus(val testing: Boolean = false, val ok: Boolean? = null, val latencyMs: Long = 0)

/**
 * 邮箱服务健康探测：向服务的只读端点发一次 GET，返回（是否在线, 耗时毫秒）。
 * - PearAPI：用 receive 查询一个不存在的邮箱（只读，不会分配新邮箱），预期 200；
 * - ITE：查询不存在的 token，预期 404（"邮箱不存在"恰说明 API 本身在正常应答）。
 * 2xx~4xx 均视为服务在线；仅 5xx 与网络异常（超时/DNS 失败）判为不可用。
 */
internal suspend fun checkMailServiceHealth(client: OkHttpClient, provider: MailProvider): Pair<Boolean, Long> =
    withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val url = when (provider) {
                MailProvider.PearApi -> "https://api.pearapi.ai/api/email/".toHttpUrl().newBuilder()
                    .addQueryParameter("type", "receive")
                    .addQueryParameter("email", "healthcheck@healthcheck.invalid")
                    .build()
                MailProvider.InstantTempEmail ->
                    "https://instanttempemail.com/api/inbox/00000000-0000-0000-0000-000000000000".toHttpUrl()
            }
            client.newCall(Request.Builder().url(url).get().build()).execute().use { resp ->
                Pair(resp.code in 200..499, System.currentTimeMillis() - start)
            }
        } catch (_: Exception) {
            Pair(false, System.currentTimeMillis() - start)
        }
    }

@Composable
internal fun SettingsTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    client: OkHttpClient,
    pageState: MutableState<SettingsPage>,
    onCheckUpdate: (Boolean) -> Unit,
    onState: (AppState) -> Unit
) {
    // 子页状态由根布局持有（见 setContent 中的说明）：底栏布局分支切换时不会被重置
    var page by pageState
    BackHandler(page != SettingsPage.Main) { page = SettingsPage.Main }

    // 关于页整页动态背景（KernelSU 同款配色）：数个大尺寸柔边色斑（径向渐变圆）
    // 沿各自的椭圆轨迹独立漂移、相互穿插融合 → 不规则色块的"多色流动"观感。
    // 全部轨迹用 sin(2π(t+φ))，7s 一轮无缝循环；深色模式取同色相暗版避免刺眼。
    // 背景独立成层并注册为 layerBackdrop，供链接卡片 drawBackdrop 做毛玻璃。
    // 纯 drawBehind，无 BlurMaskFilter/RenderEffect。
    val aboutBackdrop = rememberLayerBackdrop()
    // 关于页动态色源：背景色斑与 Logo/软件名染色共用同一组相位，
    // Logo/名字内部的颜色就是背景同源色、随同一节奏流动 → 真实的"背景映射"。
    // 色系：雾紫 / 雾粉 / 雾蓝（KernelSU 蓝色版同款观感）；7s 一轮。
    val aboutDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val aboutPurple = if (aboutDark) Color(0xFF4A3D6E) else Color(0xFFC7B0F2)
    val aboutPink = if (aboutDark) Color(0xFF57344C) else Color(0xFFF3BCD9)
    val aboutBlue = if (aboutDark) Color(0xFF2E3D5C) else Color(0xFFC3D6F2)
    // 映射色（Logo/名字染色）：浅色模式 = 背景色加深提饱和；深色模式 = 轻微提亮。
    // 【加深调节】mapDeepen：映射色再向黑收的比例。0f = 不加深，越大越深；
    // 建议 0f ~ 0.25f，超过 0.3 会开始发灰。想单独调某个颜色，直接改下面三个 0x 色值。
    val mapDeepen = if (aboutDark) 0.05f else 0.20f
    fun deepen(c: Color): Color = lerp(c, Color.Black, mapDeepen)
    val mapPurple = deepen(if (aboutDark) Color(0xFF6A57A0) else Color(0xFF9F7FE8))
    val mapPink = deepen(if (aboutDark) Color(0xFF7E4A6B) else Color(0xFFE890BE))
    val mapBlue = deepen(if (aboutDark) Color(0xFF485F8F) else Color(0xFF7E9BD8))
    val aboutPhase by rememberInfiniteTransition(label = "aboutGradient").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "aboutGradientPhase"
    )
    val aboutBackground: Modifier = if (page == SettingsPage.About) {
        val base = if (aboutDark) Color(0xFF1D1A24) else Color(0xFFF7F4FB)
        Modifier.drawBehind {
            val w = size.width
            val h = size.height
            val minDim = min(w, h)
            val tau = 2f * PI.toFloat()
            drawRect(base)
            // 色斑参数：颜色、中心基点(比例)、漂移幅度(比例)、轨迹相位(x,y)、半径(短边比例)。
            // 中心 0.55 半径内保持主浓度、向外羽化到透明，斑与斑交叠处自然融合。
            fun blob(
                color: Color, alpha: Float,
                bx: Float, by: Float, ax: Float, ay: Float,
                px: Float, py: Float, r: Float
            ) {
                val cx = w * (bx + ax * sin(tau * (aboutPhase + px)))
                val cy = h * (by + ay * sin(tau * (aboutPhase + py)))
                val radius = r * minDim
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to color.copy(alpha = alpha),
                            0.55f to color.copy(alpha = alpha * 0.7f),
                            1f to Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = radius
                    ),
                    radius = radius,
                    center = Offset(cx, cy)
                )
            }
            // 六斑三色对称分布（紫 左上/右下、粉 右上/左下、蓝 左中/右中），
            // x 相位左右错开、y 相位上下错开 → 象限内游走、边缘交叠，重心不会挤到一侧。
            blob(aboutPurple, 0.85f, 0.26f, 0.22f, 0.20f, 0.11f, 0.00f, 0.25f, 0.62f)
            blob(aboutPink,   0.85f, 0.74f, 0.30f, 0.20f, 0.11f, 0.50f, 0.75f, 0.60f)
            blob(aboutBlue,   0.80f, 0.14f, 0.55f, 0.20f, 0.11f, 0.25f, 0.50f, 0.58f)
            blob(aboutBlue,   0.80f, 0.86f, 0.52f, 0.20f, 0.11f, 0.75f, 0.00f, 0.58f)
            blob(aboutPink,   0.80f, 0.28f, 0.85f, 0.20f, 0.11f, 0.00f, 0.75f, 0.62f)
            blob(aboutPurple, 0.80f, 0.70f, 0.78f, 0.20f, 0.11f, 0.50f, 0.25f, 0.64f)
        }
    } else Modifier

    Box(Modifier.fillMaxSize()) {
        // 背景独立子层：layerBackdrop 只捕获纯渐变（不含前景内容），
        // 链接卡片 drawBackdrop 取它做毛玻璃，避免卡片把自身模糊进背景形成反馈。
        // 非 About 页 aboutBackground 为空 Modifier，此层无视觉影响。
        Box(
            Modifier
                .matchParentSize()
                .layerBackdrop(aboutBackdrop)
                .then(aboutBackground)
        )
        Column(Modifier.fillMaxSize().padding(p).statusBarsPadding()) {
            Box(Modifier.weight(1f), propagateMinConstraints = true) {
                Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val direction = if (forward) 1 else -1
                        (slideInHorizontally { it * direction } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { it * -direction } + fadeOut(tween(150)))
                    },
                    label = "settingsPage"
                ) { currentPage ->
                    // AnimatedContent 的内容作用域不会垂直堆叠同级元素：包一层 Column，
                    // 否则页面内"返回按钮 + 标题 + 卡片"会互相覆盖（标题与返回按钮被卡片盖住）
                    Column {
                    when (currentPage) {
                        SettingsPage.Main -> {
                            Text(s.settings, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(24.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    SettingsItem(
                                        label = s.languageLabel,
                                        value = allLanguages.find { it.code == state.language }?.label ?: "中文",
                                        onClick = { page = SettingsPage.Language }
                                    )
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.mailService,
                                        value = if (state.mailProvider == MailProvider.InstantTempEmail) "接口2" else "接口1",
                                        onClick = { page = SettingsPage.MailProvider }
                                    )
                                    // 明暗模式与底栏选项：HyperOS 主题下合并进「主题设置」，
                                    // Material3 主题下沿用原有的独立入口，两者不重复出现
                                    if (state.themeStyle == ThemeStyle.HyperOS) {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.themeSettings,
                                            value = themeModeLabel(state.themeMode, s),
                                            onClick = { page = SettingsPage.ThemeSettings }
                                        )
                                    } else {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.darkMode,
                                            value = themeModeLabel(state.themeMode, s),
                                            onClick = { page = SettingsPage.DarkMode }
                                        )
                                    }
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.themeStyle,
                                        value = if (state.themeStyle == ThemeStyle.HyperOS) s.themeHyperOS else s.themeDefault,
                                        onClick = { page = SettingsPage.Theme }
                                    )
                                    // 动态配色入口只在 Material3 主题下保留：
                                    // HyperOS 主题的取色入口在「主题设置」页内，避免两处重复
                                    if (state.themeStyle == ThemeStyle.Material3) {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.monet,
                                            value = if (state.dynamicSeed == NoDynamicSeed) "OFF"
                                            else "#%06X".format(state.dynamicSeed and 0xFFFFFF),
                                            onClick = { page = SettingsPage.Monet }
                                        )
                                    }
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                label = s.checkUpdate,
                                onClick = { onCheckUpdate(true) }
                            )
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            Row(
                                Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(s.autoCheckUpdate, style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f))
                                ThemedSwitch(
                                    checked = state.autoCheckUpdate,
                                    onCheckedChange = { onState(state.copy(autoCheckUpdate = it)) }
                                )
                            }
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                label = s.about,
                                onClick = { page = SettingsPage.About }
                            )
                                }
                            }
                        }

                        SettingsPage.Language -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.langSelect, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    allLanguages.forEachIndexed { i, lang ->
                                        if (i > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(lang.label, state.language == lang.code,
                                            onClick = { onState(state.copy(language = lang.code)); page = SettingsPage.Main })
                                    }
                                }
                            }
                        }

                        SettingsPage.MailProvider -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.mailService, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(12.dp))
                            Text(s.mailServiceDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))

                            // 服务状态检测：进入本页自动探测一次，之后可随时手动重测。
                            // 状态用 remember 持有——离开子页即释放，回来重新探测，天然拿到最新状态
                            val statuses = remember { mutableStateMapOf<MailProvider, ServiceStatus>() }
                            fun runCheck() {
                                MailProvider.entries.forEach { statuses[it] = ServiceStatus(testing = true) }
                                // 两个服务并行探测，各自完成后独立更新自己的行
                                MailProvider.entries.forEach { p ->
                                    scope.launch {
                                        val (ok, ms) = checkMailServiceHealth(client, p)
                                        statuses[p] = ServiceStatus(ok = ok, latencyMs = ms)
                                    }
                                }
                            }
                            LaunchedEffect(Unit) { runCheck() }
                            val checking = statuses.values.any { it.testing }

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(s.serviceStatus, style = MaterialTheme.typography.titleSmall)
                                        Spacer(Modifier.weight(1f))
                                        ThemedTextButton(onClick = { runCheck() }, enabled = !checking) {
                                            Text(s.testNow)
                                        }
                                    }
                                    MailProvider.entries.forEachIndexed { i, p ->
                                        if (i > 0) ThemedDivider()
                                        val st = statuses[p]
                                        // 状态点：灰=检测中/未检测，绿=正常，红=不可用
                                        val dotColor = when {
                                            st == null || st.testing -> MaterialTheme.colorScheme.onSurfaceVariant
                                            st.ok == true -> Color(0xFF34C759)
                                            else -> Color(0xFFFF3B30)
                                        }
                                        val statusText = when {
                                            st == null || st.testing -> s.statusTesting
                                            st.ok == true -> "${s.statusOk} · ${st.latencyMs} ms"
                                            else -> s.statusDown
                                        }
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                if (p == MailProvider.InstantTempEmail) "接口2" else "接口1",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text(statusText,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (st?.ok == false) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    MailProvider.entries.forEachIndexed { i, p ->
                                        if (i > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(
                                            if (p == MailProvider.InstantTempEmail) "接口2" else "接口1",
                                            state.mailProvider == p,
                                            onClick = { onState(state.copy(mailProvider = p)); page = SettingsPage.Main }
                                        )
                                    }
                                }
                            }
                        }

                        SettingsPage.DarkMode -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.darkModeSetting, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            // 三态：跟随系统 / 浅色 / 深色（与 Miuix 主题设置页保持一致）
                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    ThemeMode.entries.forEachIndexed { index, mode ->
                                        if (index > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(themeModeLabel(mode, s), state.themeMode == mode) {
                                            onState(state.copy(themeMode = mode))
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.Theme -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.themeStyle, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    LanguageOption(s.themeDefault, state.themeStyle == ThemeStyle.Material3,
                                        onClick = { onState(state.copy(themeStyle = ThemeStyle.Material3)); page = SettingsPage.Main })
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    LanguageOption(s.themeHyperOS, state.themeStyle == ThemeStyle.HyperOS,
                                        onClick = { onState(state.copy(themeStyle = ThemeStyle.HyperOS)); page = SettingsPage.Main })
                                }
                            }
                        }

                        SettingsPage.ThemeSettings -> ThemeSettingsPage(
                            state = state,
                            s = s,
                            snackbar = snackbar,
                            scope = scope,
                            onState = onState,
                            onBack = { page = SettingsPage.Main }
                        )

                        SettingsPage.Monet -> MonetPage(
                            state = state,
                            s = s,
                            snackbar = snackbar,
                            scope = scope,
                            onState = onState,
                            onBack = { page = SettingsPage.Main }
                        )

                        SettingsPage.About -> {
                            val hyper = LocalThemeStyle.current == ThemeStyle.HyperOS
                            val surfaceColors = themedSurfaceColors()
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            // 大标题：KernelSU 同款特大号左对齐
                            Text(
                                s.about,
                                style = if (hyper) MiuixTheme.textStyles.title1
                                        else MaterialTheme.typography.displaySmall
                            )
                            Spacer(Modifier.height(56.dp))
                            // 居中标识区：App 图标块 + 应用名 + 版本号
                            Column(
                                Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Logo 染色：背景同源紫粉映射（加深版），随相位流动。
                                // 灰度底保留信封折线层次，SrcAtop 把加深后的背景色染进
                                // Logo 内部；offscreen 合成层隔离混色，边缘清晰、轻微半透明。
                                val logoMix = (sin(2f * PI.toFloat() * aboutPhase) + 1f) / 2f
                                val tintAlpha = if (aboutDark) 0.75f else 0.85f
                                Box(
                                    Modifier
                                        .size(96.dp)
                                        .graphicsLayer {
                                            compositingStrategy = CompositingStrategy.Offscreen
                                            alpha = 0.95f
                                        }
                                        .clip(RoundedCornerShape(22.dp))
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.colorMatrix(
                                            ColorMatrix().apply { setToSaturation(0f) }
                                        ),
                                        modifier = Modifier.size(96.dp)
                                    )
                                    Canvas(Modifier.size(96.dp)) {
                                        // 背景映射：加深后的背景同源紫粉渐变染进 Logo 内部，
                                        // 中段随 aboutPhase 在紫粉间流动（与背景同步）
                                        drawRect(
                                            brush = Brush.linearGradient(
                                                colorStops = arrayOf(
                                                    0f to mapPurple.copy(alpha = tintAlpha),
                                                    0.5f to lerp(mapBlue, mapPink, logoMix)
                                                        .copy(alpha = tintAlpha * 0.85f),
                                                    1f to mapPink.copy(alpha = tintAlpha)
                                                ),
                                                start = Offset.Zero,
                                                end = Offset(size.width, size.height)
                                            ),
                                            blendMode = BlendMode.SrcAtop
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                // 软件名与 Logo 同款：加深的背景同源紫粉渐变，中段随相位流动
                                val nameBrush = Brush.linearGradient(
                                    colorStops = arrayOf(
                                        0f to mapPurple.copy(alpha = 0.92f),
                                        0.5f to lerp(mapBlue, mapPink, logoMix).copy(alpha = 0.88f),
                                        1f to mapPink.copy(alpha = 0.92f)
                                    )
                                )
                                Text(
                                    stringResource(R.string.app_name),
                                    style = if (hyper) MiuixTheme.textStyles.title1.copy(brush = nameBrush)
                                            else MaterialTheme.typography.headlineLarge.copy(brush = nameBrush)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "v" + BuildConfig.VERSION_NAME,
                                    style = if (hyper) MiuixTheme.textStyles.body2
                                            else MaterialTheme.typography.bodyMedium,
                                    color = if (hyper) MiuixTheme.colorScheme.onSurfaceSecondary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(56.dp))
                            // 链接行组：半透明卡片透出动态背景；HyperOS + API33+ 走真毛玻璃
                            // （drawBackdrop 模糊背后的渐变层），否则退化为纯半透明色。
                            // HyperOS 行仍渲染为 Miuix BasicComponent（MIUI 排版 + 按压反馈）
                            val cardShape = MaterialTheme.shapes.large
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(cardShape)
                                    .then(
                                        if (hyper && isGlassBlurSupported()) {
                                            Modifier.drawBackdrop(
                                                backdrop = aboutBackdrop,
                                                shape = { cardShape },
                                                effects = {
                                                    padding = maxOf(padding, 40.dp.toPx())
                                                    blur(14.dp.toPx(), 14.dp.toPx())
                                                },
                                                onDrawSurface = {
                                                    drawRect(surfaceColors.card.copy(alpha = 0.45f))
                                                }
                                            )
                                        } else {
                                            Modifier.background(surfaceColors.card.copy(alpha = 0.45f))
                                        }
                                    )
                            ) {
                                Column {
                                    AboutLinkRow(s.projectRepo, "https://github.com/wzhdgithub/tempmail")
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    AboutLinkRow(s.authorHomepage, "https://github.com/wzhdgithub")
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    AboutLinkRow("Blog", "https://wzhblog6.pwapi.cn/")
                                }
                            }
                        }

                    }
                    }
                }
                // 玻璃底栏：滚动内容末尾预留底栏高度，使其可完整滚出（底栏浮在其上）。
                // 必须在滚动 Column 内部才有效：放在 Column 外只是 Box 里的游离元素，
                // 最后一项会被底栏遮住、点不到（与 InboxTab 曾经的错位相同）
                if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
            }
        }
        }
    }
}

/**
 * Miuix 风格「主题设置」子页（排版参照参考图）：
 *   居中标题 + 手机预览示意图 → 明暗三选一 → 卡片1（莫奈开关 / 强调色）→ 卡片2（底栏选项）。
 *
 * 仅在 HyperOS 主题下可达（入口在设置主页），页面元素全部经 ui/theme/MiuixComponents.kt 的
 * Themed* 桥接，HyperOS 下渲染为真正的 Miuix 组件；配色与 Material3 版莫奈页共用同一份
 * dynamicSeed / dynamicStyle，因此两套主题的取色结果始终一致。
 *
 * 生效方式：**实时生效**。本页任何改动都立刻写回全局状态，预览区域与当前界面（含底栏）
 * 同步更新，改完不需要返回主界面即可看到效果；页面本身留在原地，方便连续调整。
 * 预览区域与真实界面同源取色（themedSurfaceColors），并用动画平滑过渡（颜色 / 尺寸 / 底栏形态）。
 */
