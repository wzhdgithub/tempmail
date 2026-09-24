package com.tempmail.app

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
import com.tempmail.app.ui.components.LiquidGlassBottomBar
import com.tempmail.app.ui.components.MiuixEdgeBottomBar
import com.tempmail.app.ui.components.MiuixFloatingBottomBar
import com.tempmail.app.ui.screens.HistoryTab
import com.tempmail.app.ui.screens.InboxTab
import com.tempmail.app.ui.screens.settings.SettingsTab
import com.tempmail.app.updater.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 默认的 auto() 会在 3 键导航（API 29+ 由系统绘制 scrim）与 API 26–28（直接使用
        // DefaultLightScrim = #E6FFFFFF）上把导航栏刷成近白色，在悬浮底栏下方形成纯白长条。
        // 显式传透明 scrim：light() 的 nightMode ≠ AUTO，可同时关闭系统强制对比。
        // 导航栏图标明暗仍由下方 Compose 逻辑按应用内深色开关控制。
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("app", MODE_PRIVATE) }
            var state by rememberSaveable(stateSaver = AppStateSaver) {
                mutableStateOf(AppState(
                    language = prefs.getString("language", "zh") ?: "zh",
                    themeMode = readThemeMode(prefs),
                    autoCheckUpdate = prefs.getBoolean("autoCheckUpdate", true),
                    themeStyle = ThemeStyle.fromKey(prefs.getString("themeStyle", ThemeStyle.Material3.key)),
                    barStyle = BarStyle.fromKey(prefs.getString("barStyle", BarStyle.Float.key)),
                    glassBlurEnabled = prefs.getBoolean("glassBlurEnabled", true),
                    dynamicSeed = prefs.getInt("dynamicSeed", NoDynamicSeed),
                    dynamicStyle = DynamicStyle.fromKey(prefs.getString("dynamicStyle", DynamicStyle.TonalSpot.key)),
                    dynamicContrast = prefs.getFloat("dynamicContrast", 0f),
                    mailProvider = MailProvider.fromKey(prefs.getString("mailProvider", MailProvider.PearApi.key))
                ))
            }
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val s = strings(state.language)

            // 设置页当前子页放在根布局持有（而不是设置页内部）：
            // 底栏形态切换会让 GlassShell 走不同的布局分支（Scaffold ↔ 玻璃 Box+Scaffold），
            // 分支切换会改变该子树的组合 key，状态若留在分支内部就会被重置——
            // 表现就是"一改液态玻璃/模糊就闪回设置主页"。放在分支之外即可保持当前子页。
            val settingsPageState = rememberSaveable(stateSaver = SettingsPageSaver) {
                mutableStateOf(SettingsPage.Main)
            }

            // 明暗三态解析成实际要用的布尔：跟随系统时与系统夜间模式实时同步，
            // 浅色/深色为显式覆盖。整棵 UI 树只认这个 darkTheme，不再各自读系统设置。
            val systemDark = isSystemInDarkTheme()
            val darkTheme = state.themeMode.isDark(systemDark)

            // 状态栏/导航栏图标颜色需跟随应用实际明暗（跟随系统时会随系统切换一起变）
            val view = LocalView.current
            LaunchedEffect(darkTheme) {
                val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            var showDisclaimer by remember { mutableStateOf(!prefs.getBoolean("disclaimer_accepted", false)) }
            var selectedGender by remember { mutableStateOf("") }

            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateUrl by remember { mutableStateOf("") }
            var updateTag by remember { mutableStateOf("") }
            var updateBody by remember { mutableStateOf("") }
            var updateSha by remember { mutableStateOf("") }
            var showDownloadProgress by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableStateOf(0) }
            var poem by remember { mutableStateOf<PoemLine?>(null) }

            val client = remember {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }

            // 从"未知来源"授权页返回后自动续跑下载（仍未授权则 downloadInstall 内再次跳转）
            var retryDownload by remember { mutableStateOf(false) }
            val installPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                retryDownload = true
            }

            var downloadCall by remember { mutableStateOf<Call?>(null) }

            fun downloadInstall(url: String, expectedSha256: String) {
                if (expectedSha256.isBlank()) {
                    // 缺少校验信息时拒绝下载，防止更新链路被篡改
                    scope.launch { snackbar.showSnackbar(s.updateFail) }
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        installPermissionLauncher.launch(intent)
                        return
                    }
                }
                showDownloadProgress = true
                downloadProgress = 0
                scope.launch(Dispatchers.IO) {
                    var file: File? = null
                    try {
                        val base = context.getExternalFilesDir(null)
                        if (base == null) {
                            withContext(Dispatchers.Main) {
                                showDownloadProgress = false
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        val dir = File(base, "updates")
                        dir.mkdirs()
                        val f = File(dir, "app-release.apk")
                        file = f
                        val dl = Request.Builder().url(url).get().build()
                        val call = client.newCall(dl)
                        downloadCall = call
                        val resp = call.execute()
                        val total = resp.body?.contentLength() ?: -1L
                        val source = resp.body?.byteStream() ?: return@launch
                        val md = MessageDigest.getInstance("SHA-256")
                        f.outputStream().use { out ->
                            val buf = ByteArray(8192)
                            var read: Int
                            var sofar = 0L
                            while (source.read(buf).also { read = it } != -1) {
                                md.update(buf, 0, read)
                                out.write(buf, 0, read)
                                sofar += read
                                if (total > 0) {
                                    val pct = (sofar * 100 / total).toInt()
                                    withContext(Dispatchers.Main) { downloadProgress = pct }
                                }
                            }
                        }
                        resp.close()
                        val actual = md.digest().joinToString("") { "%02x".format(it) }
                        if (!actual.equals(expectedSha256, ignoreCase = true)) {
                            f.delete()
                            withContext(Dispatchers.Main) {
                                showDownloadProgress = false
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        withContext(Dispatchers.Main) { showDownloadProgress = false }
                        val uri = FileProvider.getUriForFile(context,
                            "${context.packageName}.fileprovider", f)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // 用户主动取消不提示错误；失败/取消都删除不完整的安装包
                        try { file?.delete() } catch (_: Exception) { }
                        withContext(Dispatchers.Main) {
                            showDownloadProgress = false
                            if (downloadCall?.isCanceled() != true) {
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                        }
                    } finally {
                        downloadCall = null
                    }
                }
            }

            // 授权返回后由标志位驱动重试下载
            LaunchedEffect(retryDownload) {
                if (retryDownload) {
                    retryDownload = false
                    downloadInstall(updateUrl, updateSha)
                }
            }

            // 检查更新编排: 网络/解析已抽至 updater/AppUpdater.kt, 此处只做 UI 反馈。
            // 行为与原实现一一对应: 限流/异常 → updateFail(手动时), 无资产 → 静默,
            // 版本不高于当前 → alreadyLatest(手动时), 有新版本 → 填状态并弹更新对话框。
            fun checkUpdate(isManual: Boolean = false) {
                if (isManual) scope.launch { snackbar.showSnackbar(s.updating) }
                scope.launch(Dispatchers.IO) {
                    try {
                        when (val r = parseRelease(fetchLatestReleaseBody(client))) {
                            is ReleaseCheck.InvalidRelease -> if (isManual) withContext(Dispatchers.Main) {
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            is ReleaseCheck.NoAsset -> Unit
                            is ReleaseCheck.Release -> {
                                val cur = BuildConfig.VERSION_NAME
                                if (versionCompare(r.tag, cur) <= 0) {
                                    if (isManual) withContext(Dispatchers.Main) {
                                        scope.launch { snackbar.showSnackbar(s.alreadyLatest) }
                                    }
                                } else withContext(Dispatchers.Main) {
                                    updateTag = r.tag
                                    updateUrl = r.url
                                    updateSha = r.sha256
                                    updateBody = r.notes
                                    showUpdateDialog = true
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (isManual) withContext(Dispatchers.Main) {
                            scope.launch { snackbar.showSnackbar(s.updateFail) }
                        }
                    }
                }
            }

            // silent=true 供自动轮询使用：不置 isLoading（避免生成按钮周期性闪禁用）、不弹错误提示
            fun doRefresh(e: String, onDone: (count: Int, raw: String) -> Unit, silent: Boolean = false) {
                if (!silent) state = state.copy(isLoading = true)
                scope.launch(Dispatchers.IO) {
                    try {
                        val apiUrl = "https://api.pearapi.ai/api/email/".toHttpUrl().newBuilder()
                            .addQueryParameter("type", "receive")
                            .addQueryParameter("email", e)
                            .build()
                        val r = Request.Builder()
                            .url(apiUrl)
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        if (j.optString("code") == "200") {
                            val raw = j.optString("receivedata", "")
                            val cnt = j.optString("count", "0").toIntOrNull() ?: 0
                            withContext(Dispatchers.Main) { onDone(cnt, raw) }
                        } else {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(j.optString("msg", s.queryFailed)) }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            state = state.copy(isLoading = false)
                            if (!silent) scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            }

            // instanttempemail.com 收件箱查询：GET /api/inbox/{token}
            // 200 → {address, emails:[...], expires}；404 → 邮箱已过期（7 天有效期）
            fun doRefreshIte(token: String, onDone: (count: Int, raw: String) -> Unit, silent: Boolean = false) {
                if (!silent) state = state.copy(isLoading = true)
                scope.launch(Dispatchers.IO) {
                    try {
                        val apiUrl = "https://instanttempemail.com/api/inbox/".toHttpUrl().newBuilder()
                            .addPathSegment(token)
                            .build()
                        val r = Request.Builder()
                            .url(apiUrl)
                            .get().build()
                        val resp = client.newCall(r).execute()
                        val body = resp.body?.string() ?: ""
                        if (resp.code == 404) {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(s.mailboxExpired) }
                            }
                        } else if (resp.isSuccessful) {
                            val cnt = try {
                                JSONObject(body).optJSONArray("emails")?.length() ?: 0
                            } catch (_: Exception) { 0 }
                            withContext(Dispatchers.Main) { onDone(cnt, body) }
                        } else {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(s.queryFailed) }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            state = state.copy(isLoading = false)
                            if (!silent) scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            }

            // 收件箱刷新统一入口：手动按钮与自动轮询共用同一套解析/合并/提示逻辑。
            // silent=true（自动轮询）：不打扰用户——无"暂无邮件"提示，仅在新邮件到达时提醒
            fun performInboxRefresh(silent: Boolean) {
                val emailAtRefresh = state.email
                val tokenAtRefresh = state.iteToken
                // 当前邮箱归属按 iteToken 是否非空判断：非空 ⇒ ITE 邮箱（token 即收件箱钥匙）
                val useIte = tokenAtRefresh.isNotBlank()
                fun deliver(cnt: Int, raw: String) {
                    val newItems = if (raw.isBlank()) emptyList()
                        else if (useIte) parseIteEmails(raw, s.unknownSender)
                        else parseEmails(raw, s.unknownSender)
                    // ITE 每次轮询都返回服务端 expires：回填校准本地过期时刻（以服务端为准）
                    val serverExpires = if (useIte && raw.isNotBlank()) {
                        try { parseEmailTime(JSONObject(raw).optString("expires", "")) } catch (_: Exception) { 0L }
                    } else 0L
                    var mergedSize = -1
                    var prevSize = -1
                    // 基于写入时的最新状态更新，避免覆盖刷新期间切换的设置项
                    val cur0 = state
                    state = if (cur0.email != emailAtRefresh) {
                        cur0.copy(isLoading = false)
                    } else {
                        val merged = mergeEmailItems(cur0.items, newItems)
                        mergedSize = merged.size
                        prevSize = cur0.items.size
                        Log.d("MAIL_DEBUG", "API返回count=$cnt 解析后=${newItems.size} 已有=${cur0.items.size} 合并后=${merged.size}")
                        // rawMessages 只在本次确实解析到邮件时追加：
                        // ITE 空收件箱返回完整 JSON（非空串），若按 raw 非空判断，
                        // 每次刷新都会往"原始数据"区塞一条相同内容
                        val newRaws = if (newItems.isEmpty()) cur0.rawMessages
                            else (cur0.rawMessages + raw).takeLast(5)
                        cur0.copy(
                            count = cnt, rawMessages = newRaws,
                            items = merged, isLoading = false,
                            mailboxExpiresAt = if (serverExpires > 0) serverExpires else cur0.mailboxExpiresAt
                        )
                    }
                    val added = if (mergedSize >= 0 && prevSize >= 0) mergedSize - prevSize else 0
                    scope.launch {
                        if (added > 0) {
                            // 新邮件到达：手动/自动都提醒（自动轮询存在的主要意义）
                            snackbar.showSnackbar(if (added == 1) s.newMail else "${s.newMail} ×$added")
                        } else if (!silent) {
                            // 用解析结果而非 raw 判空：ITE 空收件箱的 raw 是完整 JSON（非空串）
                            if (newItems.isEmpty()) snackbar.showSnackbar(s.noNewMail)
                            else if (mergedSize == 0) snackbar.showSnackbar(s.parseError)
                        }
                    }
                }
                if (useIte) doRefreshIte(tokenAtRefresh, { cnt, raw -> deliver(cnt, raw) }, silent)
                else doRefresh(emailAtRefresh, { cnt, raw -> deliver(cnt, raw) }, silent)
            }

            // 前台感知：后台时暂停轮询，省电省流量（ON_RESUME 恢复）
            var isAppInForeground by remember { mutableStateOf(true) }
            DisposableEffect(this@MainActivity) {
                val observer = LifecycleEventObserver { _, event ->
                    isAppInForeground = event == Lifecycle.Event.ON_RESUME
                }
                this@MainActivity.lifecycle.addObserver(observer)
                onDispose { this@MainActivity.lifecycle.removeObserver(observer) }
            }

            // 自动轮询倒计时（秒）：收件箱刷新按钮旁显示，让用户直观看到下次刷新时机
            var pollCountdownSec by remember { mutableStateOf(0) }

            // 收件箱自动轮询：前台 + 非免责页 + 停留在收件箱 + 已有邮箱时，每 10 秒静默刷新一次；
            // 请求进行中（isLoading）跳过该轮，避免与手动刷新叠加。
            // 本地 fun 的 state 读取走 rememberSaveable 委托，循环内拿到的始终是最新值。
            // key 用 s 而非 Unit：协程捕获的是首次组合的函数实例，切换语言后需重启才能用上新文案
            LaunchedEffect(s) {
                while (true) {
                    // 秒级倒数驱动收件箱的 "9s" 显示；倒数到 0 触发一轮静默刷新
                    for (t in 10 downTo 1) {
                        pollCountdownSec = t
                        delay(1000)
                    }
                    pollCountdownSec = 0
                    if (isAppInForeground && !showDisclaimer &&
                        state.currentTab == Tab.Inbox &&
                        state.email.isNotBlank() && !state.isLoading) {
                        performInboxRefresh(silent = true)
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (state.autoCheckUpdate) checkUpdate()
            }

            LaunchedEffect(Unit) {
                poem = withContext(Dispatchers.IO) { fetchRandomPoemLine(client) }
            }

            // 清理上次更新遗留的安装包
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        File(context.getExternalFilesDir(null), "updates/app-release.apk").delete()
                    } catch (_: Exception) { }
                }
            }

            TempMailTheme(
                darkTheme = darkTheme,
                themeStyle = state.themeStyle,
                // 动态配色：未启用时传 null，两套主题的配色都与定制前完全一致
                dynamicSeed = state.dynamicSeed.takeIf { it != NoDynamicSeed },
                dynamicStyle = state.dynamicStyle,
                dynamicContrast = state.dynamicContrast
            ) {
                // 两套主题共用同一份页面内容，仅外层布局与底栏形态不同
                val tabLabel: (Tab) -> String = { tab -> tabLabelOf(tab, s) }
                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局。
                // glassOverlap 为页面滚动内容末尾需预留的底栏高度，避免最后一项被底栏遮挡。
                val glassActive = state.themeStyle == ThemeStyle.HyperOS &&
                    state.barStyle == BarStyle.LiquidGlass &&
                    state.glassBlurEnabled &&
                    !showDisclaimer && isGlassBlurSupported()
                val glassOverlap = if (glassActive) GlassBarSpace else 0.dp
                // 一次性提示：首次启用液态玻璃底栏时告知"长按可拖动切换"
                LaunchedEffect(glassActive) {
                    if (glassActive && !prefs.getBoolean("glass_hint_shown", false)) {
                        prefs.edit().putBoolean("glass_hint_shown", true).apply()
                        snackbar.showSnackbar(s.glassHint)
                    }
                }
                val disclaimerPage: @Composable (Modifier) -> Unit = { pageModifier ->
                    Column(
                        pageModifier.verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text("免责声明", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        Text(disclaimerText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Text(s.genderSelect, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("你真的选对了吗?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        val genders = listOf("male" to s.genderMale, "female" to s.genderFemale, "other" to s.genderOther)
                        genders.forEach { (value, label) ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable { selectedGender = value }
                                    .clip(MaterialTheme.shapes.small)
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedGender == value, onClick = { selectedGender = value })
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        ThemedButton(
                            onClick = {
                                if (selectedGender.isEmpty()) {
                                    scope.launch { snackbar.showSnackbar(s.genderSelect) }
                                } else if (selectedGender == "other") {
                                    prefs.edit().putBoolean("disclaimer_accepted", true).apply()
                                    showDisclaimer = false
                                } else {
                                    scope.launch { snackbar.showSnackbar(s.genderOccupied) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("同意并进入")
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
                val tabPages: @Composable (PaddingValues) -> Unit = { contentPadding ->
                    AnimatedContent(
                        targetState = state.currentTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally { it * direction } + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally { it * -direction } + fadeOut(tween(150)))
                        },
                        label = "tabContent"
                    ) { tab ->
                        when (tab) {
                            Tab.Inbox -> InboxTab(contentPadding, glassOverlap, state, snackbar, scope, context, s, client, poem, darkTheme, pollCountdownSec, { performInboxRefresh(silent = false) }) { updater ->
                                state = updater(state)
                            }
                            Tab.History -> HistoryTab(contentPadding, glassOverlap, state, s) { email ->
                                // 恢复历史邮箱时需同时恢复其查询令牌（ITE 邮箱的收件箱钥匙）与过期时刻
                                val histEntry = state.history.find { it.email == email }
                                val newHistory = if (state.email.isNotBlank() && state.email != email)
                                    state.history + HistoryEmail(state.email, false, state.iteToken, state.mailboxExpiresAt)
                                else state.history
                                val filteredHistory = newHistory.filter { it.email != email }
                                state = state.copy(
                                    email = email,
                                    count = 0,
                                    rawMessages = emptyList(),
                                    items = emptyList(),
                                    history = filteredHistory,
                                    iteToken = histEntry?.token ?: "",
                                    mailboxExpiresAt = histEntry?.expiresAt ?: 0L
                                )
                            }
                            Tab.Settings -> SettingsTab(contentPadding, glassOverlap, state, s, snackbar, scope, client, settingsPageState, onCheckUpdate = { manual -> checkUpdate(manual) }) { newState ->
                                if (newState.language != state.language) {
                                    prefs.edit().putString("language", newState.language).apply()
                                }
                                if (newState.themeMode != state.themeMode) {
                                    prefs.edit().putString("themeMode", newState.themeMode.key).apply()
                                }
                                if (newState.autoCheckUpdate != state.autoCheckUpdate) {
                                    prefs.edit().putBoolean("autoCheckUpdate", newState.autoCheckUpdate).apply()
                                }
                                if (newState.themeStyle != state.themeStyle) {
                                    prefs.edit().putString("themeStyle", newState.themeStyle.key).apply()
                                }
                                if (newState.barStyle != state.barStyle) {
                                    prefs.edit().putString("barStyle", newState.barStyle.key).apply()
                                }
                                if (newState.glassBlurEnabled != state.glassBlurEnabled) {
                                    prefs.edit().putBoolean("glassBlurEnabled", newState.glassBlurEnabled).apply()
                                }
                                if (newState.dynamicSeed != state.dynamicSeed) {
                                    prefs.edit().putInt("dynamicSeed", newState.dynamicSeed).apply()
                                }
                                if (newState.dynamicStyle != state.dynamicStyle) {
                                    prefs.edit().putString("dynamicStyle", newState.dynamicStyle.key).apply()
                                }
                                if (newState.dynamicContrast != state.dynamicContrast) {
                                    prefs.edit().putFloat("dynamicContrast", newState.dynamicContrast).apply()
                                }
                                if (newState.mailProvider != state.mailProvider) {
                                    prefs.edit().putString("mailProvider", newState.mailProvider.key).apply()
                                }
                                state = newState
                            }
                        }
                    }
                }

                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局
                // 外层 Box：让"主题变更过渡层"成为 GlassShell 的兄弟节点，才能盖住包括底栏在内的整屏
                Box(Modifier.fillMaxSize()) {
                    GlassShell(
                        glass = glassActive,
                        darkTheme = darkTheme,
                        items = Tab.entries.map { GlassBarItem(it.icon, tabLabel(it)) },
                        selectedIndex = Tab.entries.indexOf(state.currentTab),
                        onSelect = { state = state.copy(currentTab = Tab.entries[it]) },
                        snackbarHost = { SnackbarHost(snackbar) },
                        fallbackBar = {
                            if (!showDisclaimer) {
                                if (state.themeStyle == ThemeStyle.Material3) {
                                    // Material3：原始默认样式（与底栏定制前完全一致）
                                    NavigationBar {
                                        Tab.entries.forEach { tab ->
                                            NavigationBarItem(
                                                selected = state.currentTab == tab,
                                                onClick = { state = state.copy(currentTab = tab) },
                                                icon = { Icon(tab.icon, tabLabel(tab)) },
                                                label = { Text(tabLabel(tab)) }
                                            )
                                        }
                                    }
                                } else {
                                    // Miuix：底栏形态可选（Material3 主题不提供，保持原样）。
                                    // 用 Crossfade 包一层：切换形态时淡入淡出过渡，避免生硬跳变
                                    Crossfade(
                                        targetState = state.barStyle,
                                        animationSpec = tween(THEME_ANIM_MS),
                                        label = "barStyle"
                                    ) { barStyle ->
                                        when (barStyle) {
                                            BarStyle.LiquidGlass -> LiquidGlassBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                            BarStyle.Float -> MiuixFloatingBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                            BarStyle.Edge -> MiuixEdgeBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { p ->
                        if (showDisclaimer) {
                            disclaimerPage(
                                Modifier.fillMaxSize().statusBarsPadding().padding(p).padding(horizontal = 24.dp)
                            )
                        } else {
                            tabPages(p)
                        }
                    }
                    // 配色过渡由主题层的逐 token 动画负责（见 TempMailTheme / animateColorScheme），
                    // 这里不再需要"旧底色遮罩"——那种做法会在切换后先闪一帧新配色再盖上遮罩
                }
                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        title = { Text("${s.newVer}: v$updateTag") },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                if (updateBody.isNotBlank()) {
                                    Text(updateBody, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        },
                        confirmButton = {
                            ThemedTextButton(onClick = {
                                showUpdateDialog = false
                                downloadInstall(updateUrl, updateSha)
                            }) { Text(s.updateNow) }
                        },
                        dismissButton = {
                            ThemedTextButton(onClick = { showUpdateDialog = false }) { Text(s.updateLater) }
                        }
                    )
                }
                if (showDownloadProgress) {
                    AlertDialog(
                        onDismissRequest = {
                            downloadCall?.cancel()
                            showDownloadProgress = false
                        },
                        title = { Text(s.updating) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                ThemedLinearProgress(progress = { downloadProgress / 100f }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Text("${downloadProgress}%")
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            ThemedTextButton(onClick = {
                                downloadCall?.cancel()
                                showDownloadProgress = false
                            }) { Text(s.cancel) }
                        }
                    )
                }
            }
        }
    }
}

// ==================== Miuix 主题底栏（三种形态可选） ====================

/**
 * Miuix 悬浮底栏：既有样式，视觉与行为保持与定制前一致。
 */
