package com.tempmail.app.ui.screens.settings

// 动态配色(莫奈取色)子页 —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.provider.Settings
import android.text.Html
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.view.WindowCompat
import com.tempmail.app.ui.glass.GlassBarItem
import com.tempmail.app.ui.glass.GlassBarSpace
import com.tempmail.app.ui.glass.GlassShell
import com.tempmail.app.ui.glass.isGlassBlurSupported
import com.tempmail.app.ui.theme.TempMailTheme
import com.tempmail.app.ui.theme.THEME_ANIM_MS
import com.tempmail.app.ui.theme.ThemeMode
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.dynamic.DefaultMonetSeed
import com.tempmail.app.ui.theme.dynamic.DynamicStyle
import com.tempmail.app.ui.theme.dynamic.MonetPresets
import com.tempmail.app.ui.theme.dynamic.NoDynamicSeed
import com.tempmail.app.ui.theme.dynamic.extractSeedFromUri
import com.tempmail.app.ui.theme.ThemedButton
import com.tempmail.app.ui.theme.ThemedCard
import com.tempmail.app.ui.theme.ThemedDivider
import com.tempmail.app.ui.theme.ThemedDropdownValue
import com.tempmail.app.ui.theme.ThemedIconButton
import com.tempmail.app.ui.theme.ThemedLinearProgress
import com.tempmail.app.ui.theme.ThemedListRow
import com.tempmail.app.ui.theme.ThemedPollCountdown
import com.tempmail.app.ui.theme.ThemedSegmentedTabs
import com.tempmail.app.ui.theme.ThemedSwitch
import com.tempmail.app.ui.theme.ThemedTextButton
import com.tempmail.app.ui.theme.cornerRadiusOf
import com.tempmail.app.ui.theme.themedBarContainerColor
import com.tempmail.app.ui.theme.themedCornerShape
import com.tempmail.app.ui.theme.themedSurfaceColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import kotlin.random.Random
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import com.tempmail.app.data.*
import com.tempmail.app.i18n.*
import com.tempmail.app.model.*
@Composable
internal fun MonetPage(
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    onState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var extracting by remember { mutableStateOf(false) }

    // 系统相册选择器（Android 13+ 用 Photo Picker，低版本回退文档选择器），无需任何权限
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            extracting = true
            // 解码与取色都在 SeedExtractor 内部的 Dispatchers.IO 上执行
            val seed = extractSeedFromUri(ctx, uri)
            extracting = false
            if (seed != null) {
                onState(state.copy(dynamicSeed = seed))
            } else {
                snackbar.showSnackbar(s.monetFailed)
            }
        }
    }

    ThemedIconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
    }
    Spacer(Modifier.height(8.dp))
    Text(s.monet, style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(6.dp))
    Text(
        s.monetDesc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(20.dp))

    val scheme = MaterialTheme.colorScheme
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(20.dp)) {
            Text(s.monetPresets, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MonetPresets.forEach { preset ->
                    ColorDot(
                        color = Color(preset),
                        selected = state.dynamicSeed == preset,
                        onClick = { onState(state.copy(dynamicSeed = preset)) }
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            // 当前生效的配色速览（直接读当前 ColorScheme，改 seed 后立即变化）
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(
                    scheme.primary, scheme.secondary, scheme.tertiary,
                    scheme.primaryContainer, scheme.secondaryContainer, scheme.surfaceContainerHighest
                ).forEach { ColorDot(color = it, selected = false) }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    ThemedButton(
        onClick = {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !extracting
    ) {
        Text(if (extracting) s.monetExtracting else s.monetPickImage)
    }

    Spacer(Modifier.height(20.dp))
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            DynamicStyle.entries.forEachIndexed { index, style ->
                if (index > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // 风格名沿用 MCU 官方叫法（与 Material3 / Miuix 一样视为品牌名，不翻译）
                LanguageOption(style.label, state.dynamicStyle == style) {
                    onState(state.copy(dynamicStyle = style))
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            LanguageOption(s.monetContrastDefault, state.dynamicContrast == 0f) {
                onState(state.copy(dynamicContrast = 0f))
            }
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            LanguageOption(s.monetContrastHigh, state.dynamicContrast == 0.5f) {
                onState(state.copy(dynamicContrast = 0.5f))
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            SettingsItem(
                label = s.monetClose,
                value = if (state.dynamicSeed == NoDynamicSeed) "OFF" else null,
                onClick = { onState(state.copy(dynamicSeed = NoDynamicSeed)) }
            )
        }
    }
    Spacer(Modifier.height(24.dp))
}

/** 色点：预设色可点击；用于展示当前配色的那一组不可点击。 */
@Composable
internal fun ColorDot(color: Color, selected: Boolean, onClick: (() -> Unit)? = null) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    )
}
