package com.tempmail.app.updater

// 应用更新: 版本比较 + GitHub Release 网络请求/解析。
// 网络与解析逻辑从 MainActivity.checkUpdate 原样搬出(未改任何行为);
// UI 反馈(snackbar/对话框状态)仍留在调用方。

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** 版本号逐段比较: a<b 返回负, a>b 返回正, 相等返回 0; 缺段按 0 处理。 */
internal fun versionCompare(a: String, b: String): Int {
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

/** 同步请求最新 Release 原始 JSON(须在 IO 线程调用; 异常向调用方抛出, 由其 catch 处理)。 */
internal fun fetchLatestReleaseBody(client: OkHttpClient): String =
    client.newCall(
        Request.Builder()
            .url("https://api.github.com/repos/wzhdgithub/tempmail/releases/latest")
            .header("Accept", "application/vnd.github.v3+json")
            .get().build()
    ).execute().body?.string() ?: ""

/** Release 解析结果三态, 与原 checkUpdate 的分支一一对应。 */
internal sealed interface ReleaseCheck {
    /** 非 Release 响应(限流 403 / 拦截页 / API 变更): tag 为空, 不能误报"已最新" */
    data object InvalidRelease : ReleaseCheck
    /** assets 缺失或下载 url 为空: 与原逻辑一致, 静默忽略 */
    data object NoAsset : ReleaseCheck
    data class Release(
        val tag: String,
        val url: String,
        val sha256: String,
        val notes: String
    ) : ReleaseCheck
}

internal fun parseRelease(body: String): ReleaseCheck {
    val j = JSONObject(body)
    val tag = j.optString("tag_name", "").removePrefix("v").trim()
    if (tag.isBlank()) return ReleaseCheck.InvalidRelease
    val assets = j.optJSONArray("assets") ?: return ReleaseCheck.NoAsset
    val asset = assets.getJSONObject(0)
    val url = asset.optString("browser_download_url", "")
    if (url.isBlank()) return ReleaseCheck.NoAsset
    val releaseNotes = j.optString("body", "")
    // 优先使用 GitHub Release asset 的 digest 字段, 其次从发版说明中提取 64 位十六进制哈希
    var sha = asset.optString("digest", "").removePrefix("sha256:").trim()
    if (!sha.matches(Regex("^[0-9a-fA-F]{64}$"))) {
        sha = Regex("\\b[0-9a-fA-F]{64}\\b").find(releaseNotes)?.value ?: ""
    }
    return ReleaseCheck.Release(tag, url, sha, releaseNotes)
}
