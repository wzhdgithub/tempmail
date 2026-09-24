package com.tempmail.app.data

// 每日诗词: 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。

import android.util.Log
import kotlin.random.Random
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal data class PoemLine(val line: String, val title: String, val author: String)

internal val poemApiUrls = listOf(
    "https://poetry.palemoky.com/api/v1/poems/random",
    "https://poetry.palemoky.com/api/poems/random"
)

internal fun fetchRandomPoemLine(client: OkHttpClient): PoemLine? {
    for (url in poemApiUrls) {
        val poem = try {
            val r = Request.Builder().url(url).get().build()
            val resp = client.newCall(r).execute()
            val body = resp.body?.string()
            resp.close()
            if (body != null) parsePoemResponse(body) else null
        } catch (e: Exception) {
            Log.d("POEM_DEBUG", "fetch $url failed: ${e.message}")
            null
        }
        if (poem != null) return poem
    }
    return null
}

internal fun parsePoemResponse(body: String): PoemLine? {
    return try {
        val data = JSONObject(body).optJSONObject("data") ?: return null
        val title = data.optString("title", "").trim()
        val author = data.optJSONObject("author")?.optString("name", "")?.trim() ?: ""
        val content = data.optJSONArray("content") ?: return null
        if (content.length() == 0) return null
        val line = content.optString(Random.nextInt(content.length())).trim()
        if (line.isBlank()) return null
        PoemLine(line, title, author)
    } catch (e: Exception) {
        Log.d("POEM_DEBUG", "parse failed: ${e.message}")
        null
    }
}
