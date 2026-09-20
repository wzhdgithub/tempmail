package com.tempmail.app.ui.theme.dynamic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 从用户选择的图片中提取「种子色」：解码时先降采样，再用 androidx.palette 找代表色。
// 只返回一个 Int（seed）并由调用方持久化；完整配色由 material-color-utilities 用该 seed
// 确定性重建，因此无需保存图片、无需保留 Bitmap、启动时也不必重算。

/** 解码目标尺寸（最长边）：取色只看颜色分布，200px 足够，同时避免大图 OOM。 */
private const val TARGET_MAX_SIZE = 200

/** 量化颜色数：越大越慢越细，16 是 Palette 的常用取值。 */
private const val MAX_COLOR_COUNT = 16

/** 读取图片并返回种子色（ARGB）；读取或取色失败返回 null。 */
suspend fun extractSeedFromUri(context: Context, uri: Uri): Int? = withContext(Dispatchers.IO) {
    val bitmap = decodeSampled(context, uri) ?: return@withContext null
    try {
        pickSeed(Palette.from(bitmap).maximumColorCount(MAX_COLOR_COUNT).generate())
    } finally {
        bitmap.recycle()
    }
}

/** 先用 inJustDecodeBounds 读出尺寸，再按 2 的幂降采样解码，避免整张大图进内存。 */
private fun decodeSampled(context: Context, uri: Uri): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    val longestSide = maxOf(bounds.outWidth, bounds.outHeight)
    if (longestSide <= 0) return null

    var sampleSize = 1
    while (longestSide / sampleSize > TARGET_MAX_SIZE) sampleSize *= 2

    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

/**
 * 取色优先级：Vibrant（画面里最"活"的颜色，做主题最出效果）→ DarkVibrant → Muted →
 * LightVibrant → 出现次数最多的 swatch；纯灰图等无有效 swatch 时返回 null（调用方提示失败）。
 */
private fun pickSeed(palette: Palette): Int? =
    (palette.vibrantSwatch
        ?: palette.darkVibrantSwatch
        ?: palette.mutedSwatch
        ?: palette.lightVibrantSwatch
        ?: palette.swatches.maxByOrNull { it.population })
        ?.rgb