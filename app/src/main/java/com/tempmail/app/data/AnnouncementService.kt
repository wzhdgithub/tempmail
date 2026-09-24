package com.tempmail.app.data

// 远程公告: GitHub Pages 静态 JSON, 启动时静默拉取。
// 源地址按更新检查所用的仓库(wzhdgithub/tempmail)推导; 若 Pages 路径不同, 只改 announcementUrl。
// 拉取/解析失败一律返回 null, 由调用方静默忽略——公告永远不影响主流程。

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

data class Announcement(
    val version: Int,
    val title: String,
    val content: String,
    val publishTime: String,
    val url: String
)

private const val announcementUrl = "https://wzhdgithub.github.io/tempmail/announcement.json"

/** 拉取并解析公告; 任何异常(网络/格式/enabled=false/缺字段)都返回 null。 */
internal suspend fun fetchAnnouncement(client: OkHttpClient): Announcement? = withContext(Dispatchers.IO) {
    try {
        val r = Request.Builder().url(announcementUrl).get().build()
        val body = client.newCall(r).execute().body?.string() ?: ""
        parseAnnouncement(body)
    } catch (_: Exception) {
        null
    }
}

internal fun parseAnnouncement(body: String): Announcement? {
    return try {
        val j = JSONObject(body)
        // enabled=false 即远程下架; version 是已读去重的依据, 缺失视为无效公告
        if (!j.optBoolean("enabled", false)) return null
        val version = j.optInt("version", 0)
        if (version <= 0) return null
        val title = j.optString("title", "").trim()
        val content = j.optString("content", "").trim()
        if (title.isBlank() || content.isBlank()) return null
        Announcement(
            version = version,
            title = title,
            content = content,
            publishTime = j.optString("publishTime", "").trim(),
            url = j.optString("url", "").trim()
        )
    } catch (_: Exception) {
        null
    }
}
