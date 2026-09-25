package com.tempmail.app.ui.components

// 远程公告弹窗: 双主题各自原生观感, 不做割裂的统一样式。
// - Material3 主题: 现有 M3 AlertDialog（与更新/下载对话框同一组件体系）
// - HyperOS(Miuix) 主题: 自绘居中弹窗卡片, 度量与排版完全对齐 Miuix 库对话框规范
//   （DialogDefaults 内/外边距与最大宽、windowDimming 遮罩、标题居中 title4+Medium、
//   正文 body1 次要色、按钮区 MIUI 式底部等宽居中排列），文字一律用 MiuixTheme.textStyles，
//   不再出现 M3 排版/布局特征
// 内容区共用: 标题 -> 正文 -> 查看详情(url 可选) -> 发布时间 -> 操作按钮(本次关闭/不再提醒)

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tempmail.app.data.Announcement
import com.tempmail.app.i18n.Strings
import com.tempmail.app.ui.theme.LocalThemeStyle
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.ThemedTextButton
import com.tempmail.app.ui.theme.themedSurfaceColors
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
        // HyperOS(Miuix): 自绘居中弹窗卡片，度量取 Miuix 库 DialogDefaults 标准
        // （外边距 12 / 内边距 24 / 最大宽 420），遮罩用主题 windowDimming，
        // 标题/正文/按钮全走 Miuix 文字样式，观感对齐 MIUI 系统对话框。
        val colors = themedSurfaceColors()
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MiuixTheme.colorScheme.windowDimming)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClose() }
                    .padding(
                        horizontal = DialogDefaults.outsideMargin.width,
                        vertical = DialogDefaults.outsideMargin.height
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { }
                        .fillMaxWidth()
                        .widthIn(max = DialogDefaults.MaxWidth)
                        .background(colors.card, RoundedCornerShape(24.dp))
                        .padding(
                            horizontal = DialogDefaults.insideMargin.width,
                            vertical = DialogDefaults.insideMargin.height
                        )
                ) {
                    // 标题: 与 Miuix 对话框一致 —— 居中 + title4 字号 + Medium 字重
                    Text(
                        s.announcement,
                        modifier = Modifier.fillMaxWidth(),
                        style = MiuixTheme.textStyles.title4.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    AnnouncementBody(announcement, s)
                    Spacer(Modifier.height(24.dp))
                    // 按钮区: MIUI 对话框式 —— 底部一行两个等宽居中按钮，替代 M3 的右下角文字按钮
                    Row(Modifier.fillMaxWidth()) {
                        ThemedTextButton(onClick = onClose, modifier = Modifier.weight(1f)) {
                            Text(
                                s.close,
                                modifier = Modifier.fillMaxWidth(),
                                style = MiuixTheme.textStyles.button,
                                textAlign = TextAlign.Center
                            )
                        }
                        ThemedTextButton(onClick = onMute, modifier = Modifier.weight(1f)) {
                            Text(
                                s.announceMute,
                                modifier = Modifier.fillMaxWidth(),
                                style = MiuixTheme.textStyles.button,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 弹窗内容区（两分支共用）: 正文 -> 查看详情链接 -> 发布时间。
 *  HyperOS 走 Miuix 文字样式（正文 body1 + 次要色，对齐 Miuix 对话框摘要排版），
 *  Material3 维持原有 M3 排版与颜色。 */
@Composable
private fun AnnouncementBody(a: Announcement, s: Strings) {
    val ctx = LocalContext.current
    val colors = themedSurfaceColors()
    val hyper = LocalThemeStyle.current == ThemeStyle.HyperOS
    Column(Modifier.verticalScroll(rememberScrollState())) {
        Text(
            a.content,
            style = if (hyper) MiuixTheme.textStyles.body1 else MaterialTheme.typography.bodyMedium,
            color = if (hyper) MiuixTheme.colorScheme.onSurfaceSecondary else colors.onSurface
        )
        if (a.url.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(
                s.announcementView,
                style = if (hyper) MiuixTheme.textStyles.body2 else MaterialTheme.typography.labelLarge,
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
                style = if (hyper) MiuixTheme.textStyles.footnote1 else MaterialTheme.typography.labelSmall,
                color = colors.outlineVariant
            )
        }
    }
}
