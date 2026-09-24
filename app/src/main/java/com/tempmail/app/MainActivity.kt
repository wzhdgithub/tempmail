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
import com.tempmail.app.ui.screens.settings.SettingsTab

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

            fun checkUpdate(isManual: Boolean = false) {
                if (isManual) scope.launch { snackbar.showSnackbar(s.updating) }
                scope.launch(Dispatchers.IO) {
                    try {
                        val r = Request.Builder()
                            .url("https://api.github.com/repos/wzhdgithub/tempmail/releases/latest")
                            .header("Accept", "application/vnd.github.v3+json")
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        val tag = j.optString("tag_name", "").removePrefix("v").trim()
                        if (tag.isBlank()) {
                            // 非 Release 响应（限流 403 / 拦截页 / API 变更），不能误报为"已最新"
                            if (isManual) withContext(Dispatchers.Main) {
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        val cur = BuildConfig.VERSION_NAME
                        if (versionCompare(tag, cur) <= 0) {
                            if (isManual) withContext(Dispatchers.Main) {
                                scope.launch { snackbar.showSnackbar(s.alreadyLatest) }
                            }
                            return@launch
                        }
                        val assets = j.optJSONArray("assets") ?: return@launch
                        val asset = assets.getJSONObject(0)
                        val url = asset.optString("browser_download_url", "")
                        if (url.isBlank()) return@launch
                        val releaseNotes = j.optString("body", "")
                        // 优先使用 GitHub Release asset 的 digest 字段，其次从发版说明中提取 64 位十六进制哈希
                        var sha = asset.optString("digest", "").removePrefix("sha256:").trim()
                        if (!sha.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                            sha = Regex("\\b[0-9a-fA-F]{64}\\b").find(releaseNotes)?.value ?: ""
                        }
                        withContext(Dispatchers.Main) {
                            updateTag = tag
                            updateUrl = url
                            updateSha = sha
                            updateBody = releaseNotes
                            showUpdateDialog = true
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
@Composable
private fun MiuixFloatingBottomBar(
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
private fun MiuixEdgeBottomBar(
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
private fun LiquidGlassBottomBar(
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
private fun GlassTabItem(
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

@Composable
private fun InboxTab(
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

@Composable
private fun HistoryTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    s: Strings,
    onUseEmail: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text(s.historyTitle, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        val allEmails = remember(state.email, state.iteToken, state.history) {
            val list = mutableListOf<HistoryEmail>()
            if (state.email.isNotBlank()) list.add(HistoryEmail(state.email, true, state.iteToken, state.mailboxExpiresAt))
            list.addAll(state.history.reversed())
            list
        }

        if (allEmails.isEmpty()) {
            Text(s.noNewMail, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            allEmails.forEach { h ->
                ThemedCard(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .then(if (!h.isActive) Modifier.clickable { onUseEmail(h.email) } else Modifier),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(h.email,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.weight(1f))
                            if (h.isActive) {
                                Surface(
                                    shape = themedCornerShape(6.dp, 12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        s.active,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Surface(
                                    shape = themedCornerShape(6.dp, 12.dp),
                                    color = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        s.expired,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(s.useNow,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
        // 玻璃底栏：内容可滚到浮起的底栏下方，末尾预留底栏高度使最后一项仍可完整显示
        if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
    }
}

private val mailLinkJs = """
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

private fun openMailLink(view: WebView?, uri: Uri?) {
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

private fun versionCompare(a: String, b: String): Int {
    val aParts = a.split(".").map { it.toIntOrNull() ?: 0 }
    val bParts = b.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLen = maxOf(aParts.size, bParts.size)
    for (i in 0 until maxLen) {
        val av = aParts.getOrElse(i) { 0 }
        val bv = bParts.getOrElse(i) { 0 }
        if (av != bv) return av - bv
    }
    return 0
}

