package com.tempmail.app.ui.theme.dynamic

// 内置预设：按莫奈作品的整体色调**手工挑选**的代表色（ARGB）。
//
// 刻意不打包任何画作图片：莫奈原作虽已进入公有领域，但现代扫描 / 数字修复版在部分司法辖区
// 可能另有权利，且打包图片会明显增大 APK。这里只保留"能生成整套主题的那一个颜色"，
// 用户也可以改用「选择图片」从自己的图片里取色。
val MonetPresets: List<Int> = listOf(
    0xFF3F6E7A.toInt(), // 睡莲池 · 蓝绿
    0xFF2B3F73.toInt(), // 夜色 · 深蓝
    0xFF7A8C3F.toInt(), // 草地 · 黄绿
    0xFFE08A5B.toInt(), // 日出 · 橙
    0xFFC95A78.toInt(), // 花园 · 玫红
    0xFF6E5AA8.toInt()  // 雾中花 · 紫
)