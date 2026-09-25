package com.tempmail.app.ui.screens.settings

// 设置页跨子页共用小组件 —— 从 MainActivity.kt 原样搬出(仅 private -> internal, 未改任何逻辑)。


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
import com.tempmail.app.ui.theme.LocalThemeStyle
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun SettingsItem(label: String, value: String? = null, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f))
        if (selected) {
            Text("✓", style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * 「关于」页链接行（KernelSU 同款布局）：整行可点打开浏览器，尾部右箭头。
 * HyperOS 走 Miuix BasicComponent（MIUI 行排版与按压缩放反馈，箭头取动作图标色），
 * Material3 走手写行；观感各自融入主题。
 */
@Composable
internal fun AboutLinkRow(title: String, url: String) {
    val ctx = LocalContext.current
    val hyper = LocalThemeStyle.current == ThemeStyle.HyperOS
    val chevronTint = if (hyper) MiuixTheme.colorScheme.onSurfaceVariantActions
                      else MaterialTheme.colorScheme.onSurfaceVariant
    ThemedListRow(
        title = title,
        onClick = {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: Exception) { }
        },
        trailing = {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null, tint = chevronTint)
        }
    )
}
