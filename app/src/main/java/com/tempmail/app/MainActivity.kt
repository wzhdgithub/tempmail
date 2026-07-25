package com.tempmail.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tempmail.app.ui.theme.TempMailTheme
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private data class Language(val code: String, val label: String)

private val ZH = Language("zh", "中文")
private val EN = Language("en", "English")

private data class Strings(
    val title: String, val generate: String, val generating: String,
    val yourEmail: String, val receivedCount: String, val copy: String, val copied: String,
    val refresh: String, val noNewMail: String, val parseError: String,
    val inbox: String, val history: String, val settings: String,
    val historyTitle: String, val status: String, val active: String, val expired: String,
    val check: String, val useNow: String,
    val back: String, val languageLabel: String, val darkMode: String,
    val about: String, val aboutDesc: String, val author: String, val authorHint: String,
    val version: String, val langSelect: String, val darkModeSetting: String
)

private fun strings(lang: String): Strings {
    if (lang == "en") return Strings(
        title = "Temp Mail",
        generate = "Generate", generating = "Generating...",
        yourEmail = "Your Temp Email", receivedCount = "Messages",
        copy = "Copy", copied = "Copied",
        refresh = "Refresh", noNewMail = "No new mail", parseError = "Parse error, showing raw data",
        inbox = "Inbox", history = "History", settings = "Settings",
        historyTitle = "Email History", status = "Status", active = "Active", expired = "Expired",
        check = "Check", useNow = "Use Now",
        back = "Back", languageLabel = "Language", darkMode = "Dark Mode",
        about = "About", aboutDesc = "A lightweight temp email app powered by PearAPI",
        author = "Author", authorHint = "github.com/wzhdgithub",
        version = "Version 1.0", langSelect = "Select Language", darkModeSetting = "Dark Mode"
    )
    return Strings(
        title = "临时邮箱",
        generate = "生成新邮箱", generating = "生成中...",
        yourEmail = "你的临时邮箱", receivedCount = "收件数",
        copy = "复制", copied = "已复制",
        refresh = "刷新", noNewMail = "暂无新邮件", parseError = "解析异常，查看原始数据",
        inbox = "收件箱", history = "历史", settings = "设置",
        historyTitle = "历史邮箱", status = "状态", active = "使用中", expired = "已过期",
        check = "检查", useNow = "使用",
        back = "返回", languageLabel = "语言", darkMode = "深色模式",
        about = "关于", aboutDesc = "基于 PearAPI 的轻量级临时邮箱应用",
        author = "作者信息", authorHint = "github.com/wzhdgithub",
        version = "版本 1.0", langSelect = "选择语言", darkModeSetting = "深色模式设置"
    )
}

enum class Tab(val icon: ImageVector) {
    Inbox(Icons.Default.Email),
    History(Icons.Default.DateRange),
    Settings(Icons.Default.Settings)
}

data class EmailItem(
    val from: String,
    val subject: String,
    val time: String,
    val body: String
)

data class HistoryEmail(
    val email: String,
    val isActive: Boolean
)

data class AppState(
    val email: String = "",
    val count: Int = 0,
    val rawMessages: List<String> = emptyList(),
    val items: List<EmailItem> = emptyList(),
    val isLoading: Boolean = false,
    val history: List<HistoryEmail> = emptyList(),
    val currentTab: Tab = Tab.Inbox,
    val isDarkMode: Boolean = false,
    val language: String = "zh"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var state by remember { mutableStateOf(AppState()) }
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val s = strings(state.language)

            val client = remember {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }

            fun doRefresh(e: String, onDone: (count: Int, raw: String) -> Unit) {
                state = state.copy(isLoading = true)
                Thread {
                    try {
                        val r = Request.Builder()
                            .url("https://api.pearapi.ai/api/email/?type=receive&email=$e")
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        if (j.optString("code") == "200") {
                            val raw = j.optString("receivedata", "")
                            val cnt = j.optString("count", "0").toIntOrNull() ?: 0
                            onDone(cnt, raw)
                        } else {
                            state = state.copy(isLoading = false)
                            scope.launch { snackbar.showSnackbar(j.optString("msg", "查询失败")) }
                        }
                    } catch (e: Exception) {
                        state = state.copy(isLoading = false)
                        scope.launch { snackbar.showSnackbar(e.message ?: "网络错误") }
                    }
                }.start()
            }

            TempMailTheme(darkTheme = state.isDarkMode) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbar) },
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = state.currentTab == tab,
                                    onClick = { state = state.copy(currentTab = tab) },
                                    icon = { Icon(tab.icon, null) },
                                    label = {
                                        Text(when (tab) {
                                            Tab.Inbox -> s.inbox
                                            Tab.History -> s.history
                                            Tab.Settings -> s.settings
                                        })
                                    }
                                )
                            }
                        }
                    }
                ) { p ->
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
                            Tab.Inbox -> InboxTab(p, state, snackbar, scope, context, s, client, ::doRefresh) { newState ->
                                state = newState
                            }
                            Tab.History -> HistoryTab(p, state, s)
                            Tab.Settings -> SettingsTab(p, state, s) { newState ->
                                state = newState
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InboxTab(
    p: PaddingValues,
    state: AppState,
    snackbar: SnackbarHostState,
    scope: kotlinx.coroutines.CoroutineScope,
    context: Context,
    s: Strings,
    client: OkHttpClient,
    doRefresh: (String, (Int, String) -> Unit) -> Unit,
    onState: (AppState) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Icon(Icons.Default.Email, null, Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(s.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                onState(state.copy(isLoading = true))
                Thread {
                    try {
                        val r = Request.Builder()
                            .url("https://api.pearapi.ai/api/email/?type=get")
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        if (j.optString("code") == "200") {
                            val newEmail = j.optString("email", "")
                            val newHistory = if (state.email.isNotBlank())
                                state.history + HistoryEmail(state.email, false)
                            else state.history
                            onState(state.copy(
                                email = newEmail, count = 0,
                                rawMessages = emptyList(), items = emptyList(),
                                history = newHistory, isLoading = false
                            ))
                        } else {
                            onState(state.copy(isLoading = false))
                            scope.launch { snackbar.showSnackbar(j.optString("msg", "获取失败")) }
                        }
                    } catch (e: Exception) {
                        onState(state.copy(isLoading = false))
                        scope.launch { snackbar.showSnackbar(e.message ?: "网络错误") }
                    }
                }.start()
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text(if (state.isLoading) s.generating else s.generate) }

        if (state.email.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text(s.yourEmail, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("${s.receivedCount}: ${state.count}", style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.email,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f))
                        TextButton(onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("email", state.email))
                            scope.launch { snackbar.showSnackbar(s.copied) }
                        }) { Text(s.copy) }
                        FilledTonalButton(
                            onClick = {
                                doRefresh(state.email) { cnt, raw ->
                                    val items = if (raw.isBlank()) emptyList()
                                        else parseEmails(raw)
                                    val newRaws = if (raw.isBlank()) state.rawMessages
                                        else state.rawMessages + listOf(raw)
                                    onState(state.copy(
                                        count = cnt, rawMessages = newRaws,
                                        items = items, isLoading = false
                                    ))
                                    scope.launch {
                                        if (raw.isBlank()) snackbar.showSnackbar(s.noNewMail)
                                        else if (items.isEmpty()) snackbar.showSnackbar(s.parseError)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(s.refresh) }
                    }
                }
            }
        }

        val display = if (state.items.isNotEmpty()) state.items else null
        if (display != null) {
            Spacer(Modifier.height(16.dp))
            Text(s.inbox, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            display.reversed().forEach { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)) {
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
                            Divider()
                            Spacer(Modifier.height(8.dp))
                            Text(item.body,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ))
                        }
                    }
                }
            }
        }

        if (state.rawMessages.isNotEmpty() && state.items.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("原始数据:", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.rawMessages.reversed().forEach { raw ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(raw, Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun HistoryTab(
    p: PaddingValues,
    state: AppState,
    s: Strings
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text(s.historyTitle, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        val allEmails = remember(state.email, state.history) {
            val list = mutableListOf<HistoryEmail>()
            if (state.email.isNotBlank()) list.add(HistoryEmail(state.email, true))
            list.addAll(state.history.reversed())
            list
        }

        if (allEmails.isEmpty()) {
            Text(s.noNewMail, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            allEmails.forEach { h ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(h.email,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (h.isActive)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    if (h.isActive) s.active else s.expired,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SettingsPage { Main, Language, DarkMode, About, Author }

@Composable
private fun SettingsTab(
    p: PaddingValues,
    state: AppState,
    s: Strings,
    onState: (AppState) -> Unit
) {
    var page by remember { mutableStateOf(SettingsPage.Main) }
    BackHandler(page != SettingsPage.Main) { page = SettingsPage.Main }

    Column(Modifier.fillMaxSize().padding(p).padding(24.dp)) {
        Box(Modifier.weight(1f), propagateMinConstraints = true) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
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
                    when (currentPage) {
                        SettingsPage.Main -> {
                            Text(s.settings, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(24.dp))

                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column {
                                    SettingsItem(
                                        label = s.languageLabel,
                                        value = if (state.language == "zh") ZH.label else EN.label,
                                        onClick = { page = SettingsPage.Language }
                                    )
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.darkMode,
                                        value = if (state.isDarkMode) "ON" else "OFF",
                                        onClick = { page = SettingsPage.DarkMode }
                                    )
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.about,
                                        onClick = { page = SettingsPage.About }
                                    )
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.author,
                                        onClick = { page = SettingsPage.Author }
                                    )
                                }
                            }
                        }

                        SettingsPage.Language -> {
                            IconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.langSelect, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column {
                                    LanguageOption(ZH.label, state.language == "zh",
                                        onClick = { onState(state.copy(language = "zh")); page = SettingsPage.Main })
                                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                                    LanguageOption(EN.label, state.language == "en",
                                        onClick = { onState(state.copy(language = "en")); page = SettingsPage.Main })
                                }
                            }
                        }

                        SettingsPage.DarkMode -> {
                            IconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.darkModeSetting, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Row(
                                    Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s.darkMode, style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = state.isDarkMode,
                                        onCheckedChange = { onState(state.copy(isDarkMode = it)) }
                                    )
                                }
                            }
                        }

                        SettingsPage.About -> {
                            IconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.about, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(20.dp)) {
                                    Text(s.aboutDesc, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        SettingsPage.Author -> {
                            IconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.author, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                                Column(Modifier.padding(20.dp)) {
                                    Text(s.authorHint, style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (page == SettingsPage.Main) {
            Text(s.version, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        }
    }
}

@Composable
private fun SettingsItem(label: String, value: String? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Default.KeyboardArrowRight, null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        if (selected) {
            Text("✓", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun parseEmails(raw: String): List<EmailItem> {
    val result = mutableListOf<EmailItem>()
    val arr = JSONArray(raw)
    for (i in 0 until arr.length()) {
        val obj = arr.getJSONObject(i)
        val bodyObj = obj.optJSONObject("body")
        result.add(EmailItem(
            from = obj.optString("from", ""),
            subject = obj.optString("subject", ""),
            time = obj.optString("time", ""),
            body = bodyObj?.optString("text", "") ?: ""
        ))
    }
    return result
}
