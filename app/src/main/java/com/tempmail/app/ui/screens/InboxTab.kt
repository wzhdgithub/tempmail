package com.tempmail.app.ui.screens

// 收件箱页(含 WebView 正文渲染与链接拦截) —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。

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
internal fun InboxTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    s: Strings,
    client: OkHttpClient,
    poem: PoemLine?,
    // 当前明暗主题：邮件正文 WebView 的深色适配需要（算法暗化/forceDark）
    darkTheme: Boolean,
    // 自动轮询倒计时（秒，0=正在刷新或不显示）：让用户直观看到下次刷新时机
    pollCountdownSec: Int,
    onManualRefresh: () -> Unit,
    onState: ((AppState) -> AppState) -> Unit
) {
    var showBodyDialog by remember { mutableStateOf(false) }
    var dialogBody by remember { mutableStateOf("") }
    var dialogHtml by remember { mutableStateOf("") }
    var showPoem by remember { mutableStateOf(false) }

    // 倒计时时钟：仅已知过期时刻时每秒跳动（无过期信息则不空转）
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.mailboxExpiresAt) {
        if (state.mailboxExpiresAt <= 0L) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(poem != null) {
        if (poem != null) showPoem = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp)
            .statusBarsPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showPoem) Spacer(Modifier.height(24.dp))
        AnimatedVisibility(
            visible = showPoem,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = tween(600)
            )
        ) {
            poem?.let { pm ->
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(pm.line,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("——《${pm.title}》${if (pm.author.isNotBlank()) " ${pm.author}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        Icon(Icons.Default.Email, contentDescription = s.title, Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(s.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(28.dp))

        ThemedButton(
            onClick = {
                onState { it.copy(isLoading = true) }
                scope.launch(Dispatchers.IO) {
                    try {
                        if (state.mailProvider == MailProvider.InstantTempEmail) {
                            // instanttempemail.com：POST /api/create → {address, expires, token}
                            // 邮箱 7 天有效；token 是收件箱查询钥匙，必须随邮箱一起保存
                            val r = Request.Builder()
                                .url("https://instanttempemail.com/api/create")
                                .post("".toRequestBody(null))
                                .build()
                            val body = client.newCall(r).execute().body?.string() ?: ""
                            val j = JSONObject(body)
                            val newEmail = j.optString("address", "")
                            val newToken = j.optString("token", "")
                            if (newEmail.isNotBlank() && newToken.isNotBlank()) {
                                // expires 为 ISO8601 UTC（含微秒），parseEmailTime 已兼容该格式
                                val newExpiresAt = parseEmailTime(j.optString("expires", ""))
                                withContext(Dispatchers.Main) {
                                    // 基于写入时的最新状态合并历史，避免覆盖请求期间的其他状态变更
                                    onState { cur ->
                                        val newHistory = if (cur.email.isNotBlank())
                                            cur.history + HistoryEmail(cur.email, false, cur.iteToken, cur.mailboxExpiresAt)
                                        else cur.history
                                        cur.copy(
                                            email = newEmail, count = 0,
                                            rawMessages = emptyList(), items = emptyList(),
                                            history = newHistory, isLoading = false,
                                            iteToken = newToken,
                                            mailboxExpiresAt = newExpiresAt
                                        )
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onState { it.copy(isLoading = false) }
                                    scope.launch { snackbar.showSnackbar(s.fetchFailed) }
                                }
                            }
                        } else {
                            val r = Request.Builder()
                                .url("https://api.pearapi.ai/api/email/?type=get")
                                .get().build()
                            val body = client.newCall(r).execute().body?.string() ?: ""
                            val j = JSONObject(body)
                            if (j.optString("code") == "200") {
                                val newEmail = j.optString("email", "")
                                // PearAPI 只返回时长（"time":"10 minutes"），无绝对时间戳，
                                // 以生成时刻+10 分钟作为过期时刻（与实测行为一致）
                                val newExpiresAt = System.currentTimeMillis() + 10 * 60_000L
                                withContext(Dispatchers.Main) {
                                    // 基于写入时的最新状态合并历史，避免覆盖请求期间的其他状态变更
                                    onState { cur ->
                                        val newHistory = if (cur.email.isNotBlank())
                                            cur.history + HistoryEmail(cur.email, false, cur.iteToken, cur.mailboxExpiresAt)
                                        else cur.history
                                        cur.copy(
                                            email = newEmail, count = 0,
                                            rawMessages = emptyList(), items = emptyList(),
                                            history = newHistory, isLoading = false,
                                            iteToken = "",
                                            mailboxExpiresAt = newExpiresAt
                                        )
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onState { it.copy(isLoading = false) }
                                    scope.launch { snackbar.showSnackbar(j.optString("msg", s.fetchFailed)) }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onState { it.copy(isLoading = false) }
                            scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = themedCornerShape(14.dp, 20.dp)
        ) { Text(if (state.isLoading) s.generating else s.generate) }

        if (state.email.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            // 自动轮询倒计时胶囊：从下方邮箱卡片的「复制/刷新」行移出，独立放在生成按钮与
            // 邮箱卡片之间的空白区（页面中轴居中，wrap 内容宽度适配小屏）；倒数到 0 触发一次
            // 静默刷新，轮询逻辑不变。轮询周期内 0 态不足一帧即被重置，故保持条件渲染不加动效
            if (pollCountdownSec > 0) {
                ThemedPollCountdown(seconds = pollCountdownSec, label = s.autoRefresh)
                Spacer(Modifier.height(16.dp))
            }
            ThemedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(20.dp)) {
                    Text(s.yourEmail, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("${s.receivedCount}: ${state.items.size}", style = MaterialTheme.typography.labelSmall)
                    // 邮箱过期倒计时：只在已知过期时刻（>0）时显示，统一为 "...后过期" 文案
                    if (state.mailboxExpiresAt > 0L) {
                        val remaining = state.mailboxExpiresAt - nowMs
                        val danger = remaining in 1..5 * 60_000L
                        Text(
                            if (remaining <= 0L) s.mailboxExpired
                            else String.format(s.expiresInFmt, formatDurationWords(remaining)),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (remaining <= 0L || danger) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.email,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f))
                        ThemedTextButton(onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("email", state.email))
                            scope.launch { snackbar.showSnackbar(s.copied) }
                        }) { Text(s.copy) }
                        FilledTonalButton(
                            onClick = onManualRefresh,
                            shape = MaterialTheme.shapes.medium
                        ) { Text(s.refresh) }
                    }
                }
            }
        }

        if (state.items.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(s.inbox, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val sorted = remember(state.items) {
                state.items.sortedWith(
                    compareByDescending<EmailItem> { it.timestamp }.thenByDescending { it.time }
                )
            }
            sorted.forEach { item ->
                ThemedCard(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            dialogBody = if (item.body.isNotBlank()) item.body
                                else if (item.htmlBody.isNotBlank()) stripHtml(item.htmlBody)
                                else ""
                            dialogHtml = item.htmlBody
                            showBodyDialog = true
                        },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.from, style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(item.subject, style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(item.time, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.body.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            ThemedDivider()
                            Spacer(Modifier.height(8.dp))
                            Text(item.body,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        if (state.rawMessages.isNotEmpty() && state.items.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(s.rawData, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.rawMessages.reversed().forEach { raw ->
                ThemedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(raw, Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // 玻璃底栏：内容可滚到浮起的底栏下方，末尾预留底栏高度使最后一项仍可完整滚出
        // 必须在滚动 Column 内部才有效；放在 Column 外只是父布局里的游离元素，最后一项会被底栏遮住
        if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
    }

    if (showBodyDialog) {
        val code = extractVerificationCode(dialogBody)
        AlertDialog(
            onDismissRequest = { showBodyDialog = false },
            title = { Text(s.inbox) },
            text = {
                Column {
                    // 对话框关闭时销毁 WebView，防止每打开一封邮件就泄漏一个原生实例
                    val webViewHolder = remember { arrayOfNulls<WebView>(1) }
                    DisposableEffect(Unit) {
                        onDispose {
                            webViewHolder[0]?.let { wv ->
                                wv.stopLoading()
                                wv.loadUrl("about:blank")
                                wv.destroy()
                            }
                            webViewHolder[0] = null
                        }
                    }
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = false
                                // 直接加载远程图片（产品决策）：验证码邮件的 logo/图形验证码
                                // 需要图片才能识别，优先可用性
                                settings.loadsImagesAutomatically = true
                                // 深色适配：亮色 HTML 在深色主题下会有白底黑字的割裂感。
                                // API 33+ 用算法暗化（系统 WebView 官方替代方案）；
                                // 低版本退回 forceDark（targetSdk 33+ 时仅在 33 以下设备生效，恰好互补）
                                if (darkTheme) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        settings.isAlgorithmicDarkeningAllowed = true
                                    } else {
                                        @Suppress("DEPRECATION")
                                        settings.forceDark = WebSettings.FORCE_DARK_ON
                                    }
                                }
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                webChromeClient = object : WebChromeClient() {
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?
                                    ): Boolean {
                                        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                                        val newWebView = WebView(view?.context ?: ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.allowFileAccess = false
                                            webViewClient = object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(
                                                    v: WebView?,
                                                    r: WebResourceRequest?
                                                ): Boolean {
                                                    openMailLink(v, r?.url)
                                                    v?.destroy()
                                                    return true
                                                }

                                                @Deprecated("Deprecated in Java")
                                                override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
                                                    openMailLink(v, url?.let { Uri.parse(it) })
                                                    v?.destroy()
                                                    return true
                                                }
                                            }
                                        }
                                        transport.webView = newWebView
                                        resultMsg.sendToTarget()
                                        return true
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        Log.d("MAIL_LINK", "override request: ${request?.url}")
                                        openMailLink(view, request?.url)
                                        return true
                                    }

                                    @Deprecated("Deprecated in Java")
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        Log.d("MAIL_LINK", "override url: $url")
                                        openMailLink(view, url?.let { Uri.parse(it) })
                                        return true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        try {
                                            view?.evaluateJavascript(mailLinkJs, null)
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                                if (dialogHtml.isNotBlank()) {
                                    loadDataWithBaseURL("https://example.com", dialogHtml, "text/html", "UTF-8", null)
                                } else if (looksLikeHtml(dialogBody)) {
                                    loadDataWithBaseURL("https://example.com", dialogBody, "text/html", "UTF-8", null)
                                } else {
                                    // 纯文本不再用 text/plain 加载（WebView 默认黑字，深色主题下不可读），
                                    // 包一层带主题色 <pre> 的 HTML，明暗主题下都可读
                                    val textColor = if (darkTheme) "#E6E1E5" else "#1C1B1F"
                                    val esc = Html.escapeHtml(dialogBody)
                                    loadDataWithBaseURL("https://example.com",
                                        "<html><body style=\"margin:0;padding:4px\">" +
                                            "<pre style=\"white-space:pre-wrap;word-wrap:break-word;" +
                                            "font-family:monospace;font-size:14px;color:$textColor\">$esc</pre>" +
                                            "</body></html>",
                                        "text/html", "UTF-8", null)
                                }
                            }.also { webViewHolder[0] = it }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                    )
                }
            },
            confirmButton = {
                ThemedTextButton(onClick = { showBodyDialog = false }) { Text(s.close) }
            },
            dismissButton = code?.let { c ->
                {
                    ThemedTextButton(onClick = {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("verification_code", c))
                        scope.launch { snackbar.showSnackbar("${s.codeCopied}: $c") }
                    }) { Text(s.copyCode) }
                }
            }
        )
    }
}

internal val mailLinkJs = """
    (function() {
        document.addEventListener('click', function(e) {
            var el = e.target;
            while (el && el !== document) {
                if (el.tagName === 'A' && el.href) {
                    e.preventDefault();
                    window.location.href = el.href;
                    return;
                }
                el = el.parentNode;
            }
        }, true);
    })();
""".trimIndent()

internal fun openMailLink(view: WebView?, uri: Uri?) {
    val u = uri ?: return
    val scheme = u.scheme?.lowercase()
    if (scheme != "http" && scheme != "https" && scheme != "mailto" && scheme != "tel") return
    Log.d("MAIL_LINK", "open: $u")
    try {
        val ctx = view?.context ?: return
        ctx.startActivity(Intent(Intent.ACTION_VIEW, u))
    } catch (e: Exception) {
        Log.d("MAIL_LINK", "open failed: $e")
    }
}
