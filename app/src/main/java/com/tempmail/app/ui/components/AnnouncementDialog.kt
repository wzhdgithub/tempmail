package com.tempmail.app.ui.components

// 远程公告弹窗: 双主题各自原生观感, 不做割裂的统一样式。
// - Material3 主题: 现有 M3 AlertDialog（与更新/下载对话框同一组件体系）
// - HyperOS(Miuix) 主题: themedSurfaceColors 取 Miuix 色板自绘弹窗卡片
//   （Miuix 卡片层色 + 大圆角 + ThemedTextButton 桥接按钮），不经 M3 对话框壳
// 内容区共用: 标题 -> 正文 -> 查看详情(url 可选) -> 发布时间 -> 操作按钮(本次关闭/不再提醒)

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tempmail.app.data.Announcement
import com.tempmail.app.i18n.Strings
import com.tempmail.app.ui.theme.LocalThemeStyle
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.ThemedTextButton
import com.tempmail.app.ui.theme.themedSurfaceColors

@Composable
internal fun AnnouncementDialog(
    announcement: Announcement,
    s: Strings,
    onClose: () -> Unit,
    onMute: () -> Unit
) {
    if (LocalThemeStyle.current == ThemeStyle.Material3) {
        AlertDialog(
            onDismissRequest = onClose,
            title = { Text(s.announcement) },
            text = { AnnouncementBody(announcement, s) },
            confirmButton = {
                ThemedTextButton(onClick = onMute) { Text(s.announceMute) }
            },
            dismissButton = {
                ThemedTextButton(onClick = onClose) { Text(s.close) }
            }
        )
    } else {
        val colors = themedSurfaceColors()
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                        .widthIn(max = 340.dp)
                        .background(colors.card, RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Text(
                        s.announcement,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    AnnouncementBody(announcement, s)
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemedTextButton(onClick = onClose) { Text(s.close) }
                        ThemedTextButton(onClick = onMute) { Text(s.announceMute) }
                    }
                }
            }
        }
    }
}

/** 弹窗内容区（两分支共用）: 正文 -> 查看详情链接 -> 发布时间；颜色走跨主题桥接。 */
@Composable
private fun AnnouncementBody(a: Announcement, s: Strings) {
    val ctx = LocalContext.current
    val colors = themedSurfaceColors()
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(a.content, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
        if (a.url.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                s.announcementView,
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                modifier = Modifier.clickable {
                    try {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(a.url)))
                    } catch (_: Exception) { }
                }
            )
        }
        if (a.publishTime.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                a.publishTime,
                style = MaterialTheme.typography.labelSmall,
                color = colors.outlineVariant
            )
        }
    }
}
