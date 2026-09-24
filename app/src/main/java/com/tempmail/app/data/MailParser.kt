package com.tempmail.app.data

// 邮件解析纯函数: 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。

import android.os.Build
import android.text.Html
import android.util.Log
import com.tempmail.app.model.EmailItem
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

internal fun parseEmails(raw: String, defaultFrom: String = ""): List<EmailItem> {
    val result = mutableListOf<EmailItem>()
    val text = raw.trim()
    if (text.isBlank()) return result
    try {
        if (text.startsWith("[")) {
            val arr = JSONArray(text)
            for (i in 0 until arr.length()) {
                try {
                    result.add(parseEmailObject(arr.getJSONObject(i)))
                } catch (e: Exception) {
                    Log.d("MAIL_DEBUG", "parse item $i failed: ${arr.opt(i)}", e)
                }
            }
        } else if (text.startsWith("{")) {
            try {
                result.add(parseEmailObject(JSONObject(text)))
            } catch (e: Exception) {
                Log.d("MAIL_DEBUG", "parse single object failed: $text", e)
            }
        } else {
            Log.d("MAIL_DEBUG", "receivedata is plain text: $text")
            result.add(EmailItem(from = "", subject = "", time = "", body = text))
        }
    } catch (e: Exception) {
        Log.d("MAIL_DEBUG", "parseEmails outer failed for: $raw", e)
    }
    if (result.isEmpty() && text.isNotBlank()) {
        result.add(EmailItem(from = "", subject = "", time = "", body = text))
    }
    // 纯文本或缺字段邮件没有发件人信息，统一用本地化占位避免列表出现空白发件人
    return result.map { if (it.from.isBlank() && defaultFrom.isNotBlank()) it.copy(from = defaultFrom) else it }
}

internal fun parseEmailObject(obj: JSONObject): EmailItem {
    val from = obj.optString("from", "")
    val subject = obj.optString("subject", "")
    val time = obj.optString("time", "")
    var body = ""
    var htmlBody = ""

    fun extractFromJson(value: Any?): Pair<String, String> {
        var txt = ""
        var html = ""
        when (value) {
            is JSONObject -> {
                txt = value.optString("text", "")
                val h = value.optString("html", "")
                if (h.isNotBlank()) {
                    html = h
                    if (txt.isBlank()) txt = stripHtml(h)
                }
                if (txt.isBlank() && html.isBlank()) {
                    for (key in value.keys()) {
                        val v = value.opt(key)
                        when (v) {
                            is String -> {
                                if (v.isNotBlank()) {
                                    txt = if (looksLikeHtml(v)) stripHtml(v) else v
                                    if (looksLikeHtml(v)) html = v
                                    break
                                }
                            }
                            is JSONObject -> {
                                val (t, h2) = extractFromJson(v)
                                if (t.isNotBlank() || h2.isNotBlank()) {
                                    txt = t; html = h2; break
                                }
                            }
                        }
                    }
                }
            }
            is String -> {
                if (looksLikeHtml(value)) {
                    html = value
                    txt = stripHtml(value)
                } else {
                    txt = value
                }
            }
            else -> {
                val s = value?.toString() ?: ""
                if (s.isNotBlank() && s != "null" && s != "{}") {
                    txt = if (looksLikeHtml(s)) stripHtml(s) else s
                    if (looksLikeHtml(s)) html = s
                }
            }
        }
        return txt to html
    }

    val bodyField = try { obj.get("body") } catch (_: Exception) { null }
    if (bodyField != null) {
        val (t, h) = extractFromJson(bodyField)
        body = t; htmlBody = h
    }

    if (body.isBlank() && htmlBody.isBlank()) {
        val fallbackKeys = listOf("content", "raw", "data", "message", "body_text", "body_html", "plain")
        for (key in fallbackKeys) {
            val v = try { obj.get(key) } catch (_: Exception) { null } ?: continue
            val (t, h) = extractFromJson(v)
            if (t.isNotBlank() || h.isNotBlank()) {
                body = t; htmlBody = h; break
            }
        }
    }

    if (body.isBlank() && htmlBody.isBlank() && bodyField != null) {
        when (bodyField) {
            is JSONObject, is JSONArray -> {
                // JSON 结构直接 toString 会得到 {"text":"","html":""}，禁止显示给用户
            }
            else -> {
                val raw = bodyField.toString()
                if (raw.isNotBlank() && raw != "null" && raw != "{}") {
                    body = if (looksLikeHtml(raw)) stripHtml(raw) else raw
                }
            }
        }
    }

    val displayBody = body.ifBlank {
        if (htmlBody.isNotBlank()) stripHtml(htmlBody) else ""
    }
    Log.d("MAIL_DEBUG", "parseEmailObject: from=$from sub=${subject.take(30)} body=${displayBody.take(80)}")
    return EmailItem(from = from, subject = subject, time = time, body = displayBody, htmlBody = htmlBody,
        timestamp = parseEmailTime(time))
}

internal fun looksLikeHtml(text: String): Boolean {
    val lower = text.lowercase()
    return lower.contains("<html") || lower.contains("<!doc") ||
           lower.contains("<div") || lower.contains("<p") ||
           lower.contains("<br") || lower.contains("<table") ||
           lower.contains("<a ") || lower.contains("<span") ||
           lower.contains("<h1") || lower.contains("<h2") ||
           lower.contains("<h3") || lower.contains("<style") ||
           lower.contains("<font") || lower.contains("<img") ||
           lower.contains("<tr") || lower.contains("<td")
}

internal fun stripHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        .toString().trim()
}

internal fun emailKey(item: EmailItem): String {
    return "${item.from}|${item.subject}|${item.time}"
}

internal fun mergeEmailItems(oldList: List<EmailItem>, newList: List<EmailItem>): List<EmailItem> {
    val result = oldList.toMutableList()
    val existingKeys = oldList.map { emailKey(it) }.toMutableSet()
    var added = 0
    for (item in newList) {
        val key = emailKey(item)
        if (key !in existingKeys) {
            result.add(item)
            existingKeys.add(key)
            added++
        }
    }
    Log.d("MAIL_DEBUG", "mergeEmailItems old=${oldList.size} new=${newList.size} added=$added total=${result.size}")
    return result
}

internal fun extractVerificationCode(text: String): String? {
    Regex("(?:验证码|校验码|激活码|确认码|动态码)[是为:：\\s]+([A-Za-z0-9]{4,8})")
        .find(text)?.groupValues?.get(1)?.let { return it }
    Regex("(?i)(?:verification\\s+)?code\\s*(?:is|:|-)\\s+([A-Za-z0-9]{4,8})")
        .find(text)?.groupValues?.get(1)?.let { return it }
    Regex("(?i)code\\s+(?:below|above|here)[\\s:]*\\n?\\s*([0-9]{4,8})")
        .find(text)?.groupValues?.get(1)?.let { return it }
    // 兜底规则：仅当 20XX 整体是 4 位数字（后不接数字）才视为年份排除，202501 这类 6 位验证码不受影响
    Regex("(?:^|\\s)((?!20\\d{2}(?![0-9]))[0-9]{4,8})(?:\\s|$|\\.|,)")
        .find(text)?.groupValues?.get(1)?.let { return it }
    return null
}

// 将 API 返回的时间字符串解析为毫秒时间戳，兼容多种格式；解析失败返回 0（排序时沉底）
internal fun parseEmailTime(time: String): Long {
    val t = time.trim()
    if (t.isEmpty()) return 0L
    // 纯数字时间戳：10 位按秒、13 位按毫秒
    if (t.all { it.isDigit() }) {
        return when (t.length) {
            10 -> t.toLongOrNull()?.times(1000) ?: 0L
            13 -> t.toLongOrNull() ?: 0L
            else -> 0L
        }
    }
    // 先截掉 ISO8601 的小数秒（如 .621297）：SimpleDateFormat 的 S 只支持毫秒，
    // 6 位小数会被误当作毫秒数，多加约 10 分钟
    val normalized = t.replace(Regex("\\.\\d+"), "")
    val formats = arrayOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd HH:mm",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy-MM-dd"
    )
    for (f in formats) {
        try {
            val sdf = SimpleDateFormat(f, Locale.US)
            sdf.isLenient = false
            if (f.endsWith("'Z'") || f.endsWith("XXX")) {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            val d = sdf.parse(normalized)
            if (d != null) return d.time
        } catch (_: Exception) {
        }
    }
    return 0L
}

// instanttempemail.com 的 received_at（ISO8601 UTC，含微秒）转本地展示时间，
// 与 PearAPI 的 "yyyy-MM-dd HH:mm:ss" 展示格式保持一致
internal fun formatIteTime(iso: String): String {
    val ts = parseEmailTime(iso)
    if (ts == 0L) return iso
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date(ts))
}

// 剩余毫秒 → 人类可读时长（取相邻两级单位，单位缩写全球通用）："6d 23h" / "5h 23m" / "23m 45s" / "45s"
internal fun formatDurationWords(remMs: Long): String {
    val totalSec = remMs / 1000
    val d = totalSec / 86400
    val h = totalSec % 86400 / 3600
    val m = totalSec % 3600 / 60
    val sec = totalSec % 60
    return when {
        d > 0 -> "${d}d ${h}h"
        h > 0 -> "${h}h ${m}m"
        m > 0 -> "${m}m ${sec}s"
        else -> "${sec}s"
    }
}

// instanttempemail.com 收件箱响应解析：GET /api/inbox/{token} →
// {address, emails:[{id, from, subject, body_text, body_html, received_at, is_read}], expires}
// emails 为原生 JSON 数组，字段名与 PearAPI 的 receivedata 不同，单独映射
internal fun parseIteEmails(body: String, defaultFrom: String): List<EmailItem> {
    val result = mutableListOf<EmailItem>()
    try {
        val arr = JSONObject(body).optJSONArray("emails") ?: return emptyList()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            try {
                val from = o.optString("from", "")
                val subject = o.optString("subject", "")
                val receivedAt = o.optString("received_at", "")
                val html = o.optString("body_html", "")
                var text = o.optString("body_text", "")
                if (text.isBlank() && html.isNotBlank()) text = stripHtml(html)
                result.add(
                    EmailItem(
                        from = if (from.isBlank()) defaultFrom else from,
                        subject = subject,
                        time = formatIteTime(receivedAt),
                        body = text,
                        htmlBody = html,
                        timestamp = parseEmailTime(receivedAt)
                    )
                )
            } catch (e: Exception) {
                Log.d("MAIL_DEBUG", "parse ite item $i failed: ${arr.opt(i)}", e)
            }
        }
    } catch (e: Exception) {
        Log.d("MAIL_DEBUG", "parseIteEmails failed for: ${body.take(200)}", e)
    }
    return result
}

// AppState 序列化为 JSON，用于 rememberSaveable 在配置更改/进程重建后恢复状态。
// isLoading 不持久化（请求协程已随组合销毁），rawMessages 只保留最近 5 条控制 Bundle 体积。
