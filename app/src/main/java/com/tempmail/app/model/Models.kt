package com.tempmail.app.model

// 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。
// 注意: Tab 与 SettingsPage 走 ordinal 持久化, 枚举成员顺序禁动;
// BarStyle/MailProvider 用 key+fromKey, 未知值回退默认。

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.graphics.vector.ImageVector
import com.tempmail.app.i18n.Strings
import com.tempmail.app.ui.theme.ThemeMode
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.dynamic.DynamicStyle
import com.tempmail.app.ui.theme.dynamic.NoDynamicSeed
import org.json.JSONArray
import org.json.JSONObject

internal fun tabLabelOf(tab: Tab, s: Strings): String = when (tab) {
    Tab.Inbox -> s.inbox
    Tab.History -> s.history
    Tab.Settings -> s.settings
}

enum class Tab(val icon: ImageVector) {
    Inbox(Icons.Default.Email),
    History(Icons.Default.DateRange),
    Settings(Icons.Default.Settings)
}

// 底栏形态，仅在 Miuix 主题下可选。持久化使用稳定字符串 key；
// fromKey 对未知值一律回退 Float（现有悬浮底栏），禁止直接 valueOf。
// 三个取值覆盖了"悬浮 / 液态玻璃"两个开关的全部有效组合，无效组合（如悬浮关闭但液态玻璃开启）
// 在类型层面就不存在：
//   Float      = 悬浮开 + 液态玻璃关 → 普通悬浮底栏
//   LiquidGlass= 悬浮开 + 液态玻璃开 → 悬浮液态玻璃底栏
//   Edge       = 悬浮关              → 贴边底栏（液态玻璃不可用）
enum class BarStyle(val key: String) {
    Float("float"),
    LiquidGlass("liquid_glass"),
    Edge("edge");

    companion object {
        fun fromKey(key: String?): BarStyle = entries.find { it.key == key } ?: Float
    }
}

// 邮箱服务源：PearAPI 为默认；instanttempemail.com（ITE）为备用源——
// 其域名（fpklm.com）不在常见一次性邮箱黑名单内，部分网站（如 qoder）会静默丢弃
// 发往 PearAPI 域名（catchmail.io / uberip.com 等）的验证码，却能正常投递到 ITE 域名。
// fromKey 对未知值一律回退 PearAPI，禁止直接 valueOf。
enum class MailProvider(val key: String) {
    PearApi("pearapi"),
    InstantTempEmail("ite");

    companion object {
        fun fromKey(key: String?): MailProvider = entries.find { it.key == key } ?: PearApi
    }
}

data class EmailItem(
    val from: String,
    val subject: String,
    val time: String,
    val body: String,
    val htmlBody: String = "",
    val timestamp: Long = 0L
)

data class HistoryEmail(
    val email: String,
    val isActive: Boolean,
    // instanttempemail.com 邮箱的查询令牌（收件箱钥匙）；PearAPI 邮箱无状态，恒为空串
    val token: String = "",
    // 邮箱过期时刻（epoch 毫秒，0=未知）：ITE 来自服务端 expires；PearAPI 为生成时刻+10 分钟。
    // 历史条目带上它，切回历史邮箱时倒计时才不会丢
    val expiresAt: Long = 0L
)

data class AppState(
    val email: String = "",
    val count: Int = 0,
    val rawMessages: List<String> = emptyList(),
    val items: List<EmailItem> = emptyList(),
    val isLoading: Boolean = false,
    val history: List<HistoryEmail> = emptyList(),
    val currentTab: Tab = Tab.Inbox,
    // 明暗三态（跟随系统 / 浅色 / 深色）：跟随系统只在内部解析成布尔，不改变系统本身设置
    val themeMode: ThemeMode = ThemeMode.Light,
    val language: String = "zh",
    val autoCheckUpdate: Boolean = true,
    val themeStyle: ThemeStyle = ThemeStyle.Material3,
    val barStyle: BarStyle = BarStyle.Float,
    // 模糊总开关：关闭后液态玻璃底栏退化为不带模糊的普通底栏（低版本仍可正常显示）
    val glassBlurEnabled: Boolean = true,
    // 动态配色（默认关闭）：seed = NoDynamicSeed 时两套主题的配色都与定制前完全一致
    val dynamicSeed: Int = NoDynamicSeed,
    val dynamicStyle: DynamicStyle = DynamicStyle.TonalSpot,
    val dynamicContrast: Float = 0f,
    // 邮箱服务源：仅决定"生成新邮箱"按钮调用哪个服务（持久化设置）。
    // 当前邮箱的刷新不用它判断——按 iteToken 是否非空自动分流，切换服务不会打断现有邮箱的收件
    val mailProvider: MailProvider = MailProvider.PearApi,
    // instanttempemail.com 当前邮箱的查询令牌；非空 ⇒ 当前邮箱属于 ITE，刷新走 ITE 接口
    val iteToken: String = "",
    // 当前邮箱过期时刻（epoch 毫秒，0=未知，不显示倒计时）。
    // ITE 由服务端 expires 提供（每次轮询回填校准）；PearAPI 接口只给时长"10 minutes"，
    // 故取生成时刻+10 分钟估算，与实际行为一致
    val mailboxExpiresAt: Long = 0L
)

/** 明暗三态读取：优先新键 themeMode，旧版本只有布尔 isDarkMode，做一次性兼容读取。 */
internal fun readThemeMode(prefs: android.content.SharedPreferences): ThemeMode {
    prefs.getString("themeMode", null)?.let { return ThemeMode.fromKey(it) }
    return if (prefs.getBoolean("isDarkMode", false)) ThemeMode.Dark else ThemeMode.Light
}

internal enum class SettingsPage { Main, Language, DarkMode, Theme, Monet, ThemeSettings, About, Author, MailProvider }

/** 用序号持久化子页（配置变更 / 进程重建后回到原页面，越界时回退设置主页）。 */
internal val SettingsPageSaver: Saver<SettingsPage, Int> = Saver(
    save = { it.ordinal },
    restore = { SettingsPage.entries.getOrElse(it) { SettingsPage.Main } }
)

internal fun appStateToJson(state: AppState): String {
    val j = JSONObject()
    j.put("email", state.email)
    j.put("count", state.count)
    j.put("language", state.language)
    j.put("themeMode", state.themeMode.key)
    j.put("autoCheckUpdate", state.autoCheckUpdate)
    j.put("themeStyle", state.themeStyle.key)
    j.put("barStyle", state.barStyle.key)
    j.put("glassBlurEnabled", state.glassBlurEnabled)
    j.put("dynamicSeed", state.dynamicSeed)
    j.put("dynamicStyle", state.dynamicStyle.key)
    j.put("dynamicContrast", state.dynamicContrast.toDouble())
    j.put("tab", state.currentTab.ordinal)
    j.put("mailProvider", state.mailProvider.key)
    j.put("iteToken", state.iteToken)
    j.put("mailboxExpiresAt", state.mailboxExpiresAt)
    // items 限制条数、正文截断、且不保存 htmlBody（完整 HTML 动辄数十 KB），
    // 防止写入 Bundle 越过 Binder 事务上限导致 TransactionTooLargeException
    val items = JSONArray()
    state.items.takeLast(20).forEach { item ->
        items.put(JSONObject().apply {
            put("from", item.from)
            put("subject", item.subject)
            put("time", item.time)
            put("body", item.body.take(2000))
            put("ts", item.timestamp)
        })
    }
    j.put("items", items)
    val raws = JSONArray()
    state.rawMessages.takeLast(5).forEach { raws.put(it) }
    j.put("raws", raws)
    val hist = JSONArray()
    state.history.forEach { h ->
        hist.put(JSONObject().apply {
            put("email", h.email)
            put("active", h.isActive)
            put("token", h.token)
            put("expiresAt", h.expiresAt)
        })
    }
    j.put("history", hist)
    return j.toString()
}

internal fun appStateFromJson(json: String): AppState? {
    return try {
        val j = JSONObject(json)
        val items = mutableListOf<EmailItem>()
        j.optJSONArray("items")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                items.add(
                    EmailItem(
                        from = o.optString("from", ""),
                        subject = o.optString("subject", ""),
                        time = o.optString("time", ""),
                        body = o.optString("body", ""),
                        htmlBody = o.optString("htmlBody", ""),
                        timestamp = o.optLong("ts", 0L)
                    )
                )
            }
        }
        val raws = mutableListOf<String>()
        j.optJSONArray("raws")?.let { arr ->
            for (i in 0 until arr.length()) raws.add(arr.optString(i))
        }
        val hist = mutableListOf<HistoryEmail>()
        j.optJSONArray("history")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                hist.add(HistoryEmail(o.optString("email", ""), o.optBoolean("active", false), o.optString("token", ""), o.optLong("expiresAt", 0L)))
            }
        }
        AppState(
            email = j.optString("email", ""),
            count = j.optInt("count", 0),
            rawMessages = raws,
            items = items,
            isLoading = false,
            history = hist,
            currentTab = Tab.entries.getOrElse(j.optInt("tab", 0)) { Tab.Inbox },
            themeMode = ThemeMode.fromKey(j.optString("themeMode", ThemeMode.Light.key)),
            language = j.optString("language", "zh"),
            autoCheckUpdate = j.optBoolean("autoCheckUpdate", true),
            themeStyle = ThemeStyle.fromKey(j.optString("themeStyle", ThemeStyle.Material3.key)),
            barStyle = BarStyle.fromKey(j.optString("barStyle", BarStyle.Float.key)),
            glassBlurEnabled = j.optBoolean("glassBlurEnabled", true),
            dynamicSeed = j.optInt("dynamicSeed", NoDynamicSeed),
            dynamicStyle = DynamicStyle.fromKey(j.optString("dynamicStyle", DynamicStyle.TonalSpot.key)),
            dynamicContrast = j.optDouble("dynamicContrast", 0.0).toFloat(),
            mailProvider = MailProvider.fromKey(j.optString("mailProvider", MailProvider.PearApi.key)),
            iteToken = j.optString("iteToken", ""),
            mailboxExpiresAt = j.optLong("mailboxExpiresAt", 0L)
        )
    } catch (e: Exception) {
        null
    }
}

internal val AppStateSaver: Saver<AppState, String> = Saver(
    save = { appStateToJson(it.copy(isLoading = false)) },
    restore = { appStateFromJson(it) }
)
