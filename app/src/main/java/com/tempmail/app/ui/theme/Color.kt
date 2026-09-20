package com.tempmail.app.ui.theme

import androidx.compose.ui.graphics.Color

val Blue700 = Color(0xFF1976D2)
val SurfaceLight = Color(0xFFF8F9FA)
val SurfaceDark = Color(0xFF121212)
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1E1E1E)

// HyperOS 风格配色
val HyperBlueLight = Color(0xFF0072E3)
val HyperBlueDark = Color(0xFF4DA3FF)
val HyperSurfaceLight = Color(0xFFF5F5F5)
val HyperSurfaceDark = Color(0xFF0A0A0A)
val HyperCardLight = Color(0xFFFFFFFF)
val HyperCardDark = Color(0xFF1A1A1A)

// 主题风格。持久化使用稳定字符串 key；fromKey 对未知值一律回退 Material3，禁止直接 valueOf
enum class ThemeStyle(val key: String) {
    Material3("material3"),
    HyperOS("hyperos");

    companion object {
        fun fromKey(key: String?): ThemeStyle = entries.find { it.key == key } ?: Material3
    }
}

// 明暗三态。持久化同样使用稳定字符串 key；
// fromKey 对未知值回退 Light —— 旧版本只有布尔 isDarkMode 且默认 false（= 浅色），迁移后行为一致
enum class ThemeMode(val key: String) {
    System("system"),
    Light("light"),
    Dark("dark");

    /** 是否使用深色配色：System 交给系统夜间模式决定，其余两态为显式覆盖。 */
    fun isDark(systemDark: Boolean): Boolean = when (this) {
        System -> systemDark
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.find { it.key == key } ?: Light
    }
}
