package com.tempmail.app.data

// 邮箱服务数据源: PearAPI(默认) / instanttempemail.com(备用)。
// 网络请求从 MainActivity.doRefresh/doRefreshIte 原样搬出, 只返回数据;
// isLoading 状态写入与 snackbar 反馈留在调用方——Repository 不碰 Compose/状态。

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/** 业务层失败: userMsg 为服务端返回的原始 msg(可能为空, 由调用方决定兜底文案) */
class MailApiException(val userMsg: String) : Exception(userMsg)

/** ITE 邮箱已过期(HTTP 404, 服务端 7 天有效期) */
class MailboxExpiredException : Exception("mailbox expired")

object MailRepository {
    /** PearAPI 收件箱查询, 成功返回 (count, receivedata 原始 JSON)。 */
    suspend fun fetchInbox(client: OkHttpClient, email: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        val apiUrl = "https://api.pearapi.ai/api/email/".toHttpUrl().newBuilder()
            .addQueryParameter("type", "receive")
            .addQueryParameter("email", email)
            .build()
        val r = Request.Builder()
            .url(apiUrl)
            .get().build()
        val body = client.newCall(r).execute().body?.string() ?: ""
        val j = JSONObject(body)
        if (j.optString("code") == "200") {
            Pair(j.optString("count", "0").toIntOrNull() ?: 0, j.optString("receivedata", ""))
        } else {
            throw MailApiException(j.optString("msg", ""))
        }
    }

    /** ITE 收件箱查询, 成功返回 (emails 数量, 原始响应 JSON——顶层 expires 供调用方回填过期时刻)。 */
    suspend fun fetchIteInbox(client: OkHttpClient, token: String): Pair<Int, String> = withContext(Dispatchers.IO) {
        val apiUrl = "https://instanttempemail.com/api/inbox/".toHttpUrl().newBuilder()
            .addPathSegment(token)
            .build()
        val r = Request.Builder()
            .url(apiUrl)
            .get().build()
        val resp = client.newCall(r).execute()
        val body = resp.body?.string() ?: ""
        if (resp.code == 404) {
            throw MailboxExpiredException()
        } else if (resp.isSuccessful) {
            val cnt = try {
                JSONObject(body).optJSONArray("emails")?.length() ?: 0
            } catch (_: Exception) { 0 }
            Pair(cnt, body)
        } else {
            throw MailApiException("")
        }
    }
}
