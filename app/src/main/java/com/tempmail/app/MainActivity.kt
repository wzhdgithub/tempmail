package com.tempmail.app

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

private data class Language(val code: String, val label: String)

private val allLanguages = listOf(
    Language("zh", "中文"),
    Language("en", "English"),
    Language("ja", "日本語"),
    Language("ko", "한국어"),
    Language("fr", "Français"),
    Language("de", "Deutsch"),
    Language("es", "Español"),
    Language("pt", "Português"),
    Language("ru", "Русский"),
    Language("it", "Italiano"),
    Language("ar", "العربية"),
    Language("hi", "हिन्दी"),
    Language("vi", "Tiếng Việt"),
    Language("th", "ไทย"),
    Language("id", "Bahasa Indonesia")
)

private data class Strings(
    val title: String, val generate: String, val generating: String,
    val yourEmail: String, val receivedCount: String, val copy: String, val copied: String,
    val refresh: String, val noNewMail: String, val parseError: String,
    val inbox: String, val history: String, val settings: String,
    val historyTitle: String, val status: String, val active: String, val expired: String,
    val check: String, val useNow: String,
    val back: String, val languageLabel: String, val darkMode: String,
    val about: String, val aboutDesc: String, val checkUpdate: String, val updating: String,
    val newVer: String, val downloadNow: String, val alreadyLatest: String,
    val updateFail: String, val author: String,
    val version: String, val langSelect: String, val darkModeSetting: String,
    val updateNow: String, val updateLater: String, val autoCheckUpdate: String,
    val genderMale: String, val genderFemale: String, val genderOther: String,
    val genderOccupied: String, val genderSelect: String,
    val close: String, val copyCode: String, val codeCopied: String,
    val networkError: String, val queryFailed: String, val fetchFailed: String,
    val unknownSender: String,
    val cancel: String, val rawData: String,
    val authorHomepage: String, val projectRepo: String,
    val themeStyle: String, val themeDefault: String, val themeHyperOS: String,
    val barStyle: String, val barStyleFloat: String, val barStyleGlass: String,
    val glassHint: String,
    // 动态配色（莫奈取色）。15 种语言均已翻译，这里的默认值作为未知语言的英文兜底。
    val monet: String = "Dynamic Colors",
    val monetDesc: String = "Pick an image and its color becomes a full Material 3 theme",
    val monetPickImage: String = "Choose image",
    val monetPresets: String = "Presets",
    val monetStyle: String = "Color style",
    val monetContrast: String = "Contrast",
    val monetContrastDefault: String = "Default",
    val monetContrastHigh: String = "High",
    val monetClose: String = "Turn off",
    val monetExtracting: String = "Extracting color…",
    val monetFailed: String = "Couldn't extract a color, try another image",
    // 主题设置页（Miuix 主题）。同样 15 种语言均已翻译，默认值作为未知语言的英文兜底。
    val themeSettings: String = "Theme Settings",
    val themeFollowSystem: String = "Follow system",
    val themeLight: String = "Light",
    val themeDark: String = "Dark",
    val monetEnable: String = "Enable Monet colors",
    val monetAccent: String = "Accent color",
    val monetAccentDefault: String = "Default",
    val monetAccentCustom: String = "Custom",
    val barBlur: String = "Blur",
    val barBlurDesc: String = "Blur the top and bottom bars",
    val barFloatDesc: String = "Floating bottom bar in Apple style",
    val barGlassDesc: String = "Liquid glass effect for the floating bar",
    // 邮箱服务双源（PearAPI 默认 / instanttempemail.com 备用）。15 种语言均已翻译，默认值作为未知语言的英文兜底。
    val mailService: String = "Mail Service",
    val mailServiceDesc: String = "Some sites silently block PearAPI domains; switch to the backup source and generate a new address if codes never arrive",
    val mailboxExpired: String = "Mailbox expired, please generate a new one",
    // 邮箱服务状态检测（设置-邮箱服务子页）：进入页面自动探测，也可手动重测
    val serviceStatus: String = "Service Status",
    val statusTesting: String = "Testing…",
    val statusOk: String = "Normal",
    val statusDown: String = "Unreachable",
    val testNow: String = "Test now",
    // 自动轮询发现新邮件时的提醒
    val newMail: String = "New mail received",
    // 邮箱过期倒计时（收件箱卡片）：%s 为 "6d 23h" / "5h 23m" / "45s" 这类时长
    val expiresInFmt: String = "Expires in %s",
    // 自动轮询倒计时胶囊（收件箱页，生成按钮与邮箱卡片之间的空白区）
    val autoRefresh: String = "Auto refresh"
)

private fun strings(lang: String): Strings = when (lang) {
    "en" -> Strings(
        title = "Temp Mail",
        generate = "Generate", generating = "Generating...",
        yourEmail = "Your Temp Email", receivedCount = "Messages",
        copy = "Copy", copied = "Copied",
        refresh = "Refresh", noNewMail = "No new mail", parseError = "Parse error, showing raw data",
        inbox = "Inbox", history = "History", settings = "Settings",
        historyTitle = "Email History", status = "Status", active = "Active", expired = "Expired",
        check = "Check", useNow = "Use Now",
        back = "Back", languageLabel = "Language", darkMode = "Dark Mode",
        about = "About", aboutDesc = "A lightweight temp email app powered by PearAPI",
        checkUpdate = "Check Update", updating = "Checking...",
        newVer = "New version available", downloadNow = "Download",
        alreadyLatest = "Already up to date", updateFail = "Check failed",
        author = "Author",
        version = "Version", langSelect = "Select Language", darkModeSetting = "Dark Mode",
        updateNow = "Update Now", updateLater = "Later", autoCheckUpdate = "Auto Check Update",
        genderMale = "Male", genderFemale = "Female", genderOther = "Other",
        genderOccupied = "This gender is already taken", genderSelect = "Select Gender",
        close = "Close", copyCode = "Copy Code", codeCopied = "Code copied",
        networkError = "Network error", queryFailed = "Query failed", fetchFailed = "Fetch failed",
        unknownSender = "Unknown sender",
        cancel = "Cancel", rawData = "Raw data:",
        authorHomepage = "Author Homepage", projectRepo = "Project Repository",
        themeStyle = "Theme", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Bottom Bar", barStyleFloat = "Floating", barStyleGlass = "Liquid Glass", glassHint = "Long-press the bar and drag to switch tabs"
    )
    "ja" -> Strings(
        title = "一時メール",
        generate = "生成", generating = "生成中...",
        yourEmail = "一時メールアドレス", receivedCount = "メッセージ",
        copy = "コピー", copied = "コピー済み",
        refresh = "更新", noNewMail = "新しいメールはありません", parseError = "解析エラー、生データを表示",
        inbox = "受信箱", history = "履歴", settings = "設定",
        historyTitle = "メール履歴", status = "状態", active = "有効", expired = "期限切れ",
        check = "確認", useNow = "使用",
        back = "戻る", languageLabel = "言語", darkMode = "ダークモード",
        about = "について", aboutDesc = "PearAPI を利用した軽量一時メールアプリ",
        checkUpdate = "更新を確認", updating = "確認中...",
        newVer = "新しいバージョンがあります", downloadNow = "ダウンロード",
        alreadyLatest = "最新バージョンです", updateFail = "確認に失敗しました",
        author = "作者情報",
        version = "バージョン", langSelect = "言語選択", darkModeSetting = "ダークモード設定",
        updateNow = "今すぐ更新", updateLater = "後で", autoCheckUpdate = "自動更新チェック",
        genderMale = "男性", genderFemale = "女性", genderOther = "その他",
        genderOccupied = "この性別は既に使用されています", genderSelect = "性別を選択",
        close = "閉じる", copyCode = "コードをコピー", codeCopied = "コードをコピーしました",
        networkError = "ネットワークエラー", queryFailed = "クエリに失敗しました", fetchFailed = "取得に失敗しました",
        unknownSender = "不明な送信者",
        cancel = "キャンセル", rawData = "生データ:",
        authorHomepage = "作者ホームページ", projectRepo = "プロジェクトリポジトリ",
        themeStyle = "テーマ", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "ボトムバー", barStyleFloat = "フローティング", barStyleGlass = "Liquid Glass", glassHint = "バーを長押ししてドラッグでタブを切り替え",
        monet = "モネの色抽出", monetDesc = "画像から1色を取り出し、Material 3 の配色一式を生成します",
        monetPickImage = "画像を選択", monetPresets = "プリセット", monetStyle = "配色スタイル", monetContrast = "コントラスト",
        monetContrastDefault = "標準", monetContrastHigh = "高", monetClose = "動的配色をオフ",
        monetExtracting = "色を抽出中…", monetFailed = "色を抽出できませんでした。別の画像をお試しください",
        themeSettings = "テーマ設定", themeFollowSystem = "システムに従う", themeLight = "ライト", themeDark = "ダーク",
        monetEnable = "Monet カラーを有効化", monetAccent = "アクセントカラー",
        monetAccentDefault = "デフォルト", monetAccentCustom = "カスタム",
        barBlur = "ぼかし", barBlurDesc = "上部バーと下部バーのぼかしを有効にします",
        barFloatDesc = "Apple 風のフローティングバーを使用します", barGlassDesc = "フローティングバーにリキッドグラス効果を適用します",
        mailService = "メールサービス",
        mailServiceDesc = "一部のサイトは PearAPI ドメインの認証コードを黙って遮断します。届かない場合は予備ソースに切り替えて新しいアドレスを生成してください",
        mailboxExpired = "メールボックスの有効期限が切れました。新しいものを生成してください",
        serviceStatus = "サービス状態", statusTesting = "テスト中…", statusOk = "正常",
        statusDown = "接続不可", testNow = "今すぐテスト",
        newMail = "新しいメールを受信しました",
        expiresInFmt = "%s後に期限切れ",
        autoRefresh = "自動更新"
    )
    "ko" -> Strings(
        title = "임시 메일",
        generate = "생성", generating = "생성 중...",
        yourEmail = "임시 메일 주소", receivedCount = "메시지",
        copy = "복사", copied = "복사됨",
        refresh = "새로고침", noNewMail = "새 메일 없음", parseError = "파싱 오류, 원본 데이터 표시",
        inbox = "받은편지함", history = "기록", settings = "설정",
        historyTitle = "메일 기록", status = "상태", active = "사용 중", expired = "만료됨",
        check = "확인", useNow = "사용",
        back = "뒤로", languageLabel = "언어", darkMode = "다크 모드",
        about = "정보", aboutDesc = "PearAPI 기반 경량 임시 메일 앱",
        checkUpdate = "업데이트 확인", updating = "확인 중...",
        newVer = "새 버전 사용 가능", downloadNow = "다운로드",
        alreadyLatest = "최신 버전입니다", updateFail = "확인 실패",
        author = "작성자",
        version = "버전", langSelect = "언어 선택", darkModeSetting = "다크 모드 설정",
        updateNow = "지금 업데이트", updateLater = "나중에", autoCheckUpdate = "자동 업데이트 확인",
        genderMale = "남성", genderFemale = "여성", genderOther = "기타",
        genderOccupied = "이 성별은 이미 사용 중입니다", genderSelect = "성별 선택",
        close = "닫기", copyCode = "인증 코드 복사", codeCopied = "코드가 복사되었습니다",
        networkError = "네트워크 오류", queryFailed = "조회 실패", fetchFailed = "가져오기 실패",
        unknownSender = "알 수 없는 발신자",
        cancel = "취소", rawData = "원본 데이터:",
        authorHomepage = "작성자 홈페이지", projectRepo = "프로젝트 저장소",
        themeStyle = "테마", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "하단 바", barStyleFloat = "플로팅", barStyleGlass = "Liquid Glass", glassHint = "하단 바를 길게 누른 뒤 드래그해 탭 전환",
        monet = "모네 색상 추출", monetDesc = "이미지에서 색 하나를 뽑아 Material 3 전체 배색을 생성합니다",
        monetPickImage = "이미지 선택", monetPresets = "프리셋", monetStyle = "배색 스타일", monetContrast = "대비",
        monetContrastDefault = "기본", monetContrastHigh = "높음", monetClose = "동적 색상 끄기",
        monetExtracting = "색상 추출 중…", monetFailed = "색상을 추출하지 못했습니다. 다른 이미지를 사용해 보세요",
        themeSettings = "테마 설정", themeFollowSystem = "시스템 따르기", themeLight = "라이트", themeDark = "다크",
        monetEnable = "Monet 색상 사용", monetAccent = "강조 색상",
        monetAccentDefault = "기본", monetAccentCustom = "사용자 지정",
        barBlur = "블러", barBlurDesc = "상단 및 하단 바에 블러 효과 사용",
        barFloatDesc = "Apple 스타일의 플로팅 하단 바 사용", barGlassDesc = "플로팅 하단 바에 리퀴드 글래스 효과 사용",
        mailService = "메일 서비스",
        mailServiceDesc = "일부 사이트는 PearAPI 도메인의 인증 코드를 조용히 차단합니다. 도착하지 않으면 백업 소스로 전환하고 새 주소를 생성하세요",
        mailboxExpired = "메일함이 만료되었습니다. 새로 생성해 주세요",
        serviceStatus = "서비스 상태", statusTesting = "테스트 중…", statusOk = "정상",
        statusDown = "접속 불가", testNow = "지금 테스트",
        newMail = "새 메일이 도착했습니다",
        expiresInFmt = "%s 후 만료",
        autoRefresh = "자동 새로고침"
    )
    "fr" -> Strings(
        title = "Temp Mail",
        generate = "Générer", generating = "Génération...",
        yourEmail = "Votre email temporaire", receivedCount = "Messages",
        copy = "Copier", copied = "Copié",
        refresh = "Actualiser", noNewMail = "Pas de nouveau mail", parseError = "Erreur d'analyse, affichage brut",
        inbox = "Boîte de réception", history = "Historique", settings = "Paramètres",
        historyTitle = "Historique des emails", status = "Statut", active = "Actif", expired = "Expiré",
        check = "Vérifier", useNow = "Utiliser",
        back = "Retour", languageLabel = "Langue", darkMode = "Mode sombre",
        about = "À propos", aboutDesc = "Une application légère de messagerie temporaire basée sur PearAPI",
        checkUpdate = "Vérifier les mises à jour", updating = "Vérification...",
        newVer = "Nouvelle version disponible", downloadNow = "Télécharger",
        alreadyLatest = "Déjà à jour", updateFail = "Échec de la vérification",
        author = "Auteur",
        version = "Version", langSelect = "Choisir la langue", darkModeSetting = "Réglage mode sombre",
        updateNow = "Mettre à jour", updateLater = "Plus tard", autoCheckUpdate = "Vérification automatique",
        genderMale = "Homme", genderFemale = "Femme", genderOther = "Autre",
        genderOccupied = "Ce genre est déjà pris", genderSelect = "Sélectionnez le genre",
        close = "Fermer", copyCode = "Copier le code", codeCopied = "Code copié",
        networkError = "Erreur réseau", queryFailed = "Échec de la requête", fetchFailed = "Échec de la récupération",
        unknownSender = "Expéditeur inconnu",
        cancel = "Annuler", rawData = "Données brutes :",
        authorHomepage = "Page de l'auteur", projectRepo = "Dépôt du projet",
        themeStyle = "Thème", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Barre inférieure", barStyleFloat = "Flottant", barStyleGlass = "Liquid Glass", glassHint = "Appuyez longuement sur la barre et faites glisser pour changer d'onglet",
        monet = "Couleurs Monet", monetDesc = "Extrayez une couleur d'une image pour générer toute la palette Material 3",
        monetPickImage = "Choisir une image", monetPresets = "Préréglages", monetStyle = "Style de couleur", monetContrast = "Contraste",
        monetContrastDefault = "Par défaut", monetContrastHigh = "Élevé", monetClose = "Désactiver les couleurs dynamiques",
        monetExtracting = "Extraction de la couleur…", monetFailed = "Impossible d'extraire une couleur, essayez une autre image",
        themeSettings = "Réglages du thème", themeFollowSystem = "Suivre le système", themeLight = "Clair", themeDark = "Sombre",
        monetEnable = "Activer les couleurs Monet", monetAccent = "Couleur d'accent",
        monetAccentDefault = "Par défaut", monetAccentCustom = "Personnalisée",
        barBlur = "Flou", barBlurDesc = "Active le flou des barres supérieure et inférieure",
        barFloatDesc = "Barre inférieure flottante de style Apple", barGlassDesc = "Effet verre liquide pour la barre flottante",
        mailService = "Service de messagerie",
        mailServiceDesc = "Certains sites bloquent silencieusement les codes PearAPI ; si rien n'arrive, passez à la source de secours et générez une nouvelle adresse",
        mailboxExpired = "Boîte mail expirée, veuillez en générer une nouvelle",
        serviceStatus = "État du service", statusTesting = "Test en cours…", statusOk = "Normal",
        statusDown = "Inaccessible", testNow = "Tester maintenant",
        newMail = "Nouveau message reçu",
        expiresInFmt = "Expire dans %s",
        autoRefresh = "Actualisation auto"
    )
    "de" -> Strings(
        title = "Temp Mail",
        generate = "Generieren", generating = "Generiere...",
        yourEmail = "Ihre temporäre E-Mail", receivedCount = "Nachrichten",
        copy = "Kopieren", copied = "Kopiert",
        refresh = "Aktualisieren", noNewMail = "Keine neuen E-Mails", parseError = "Parse-Fehler, Rohdaten anzeigen",
        inbox = "Posteingang", history = "Verlauf", settings = "Einstellungen",
        historyTitle = "E-Mail-Verlauf", status = "Status", active = "Aktiv", expired = "Abgelaufen",
        check = "Prüfen", useNow = "Nutzen",
        back = "Zurück", languageLabel = "Sprache", darkMode = "Dunkelmodus",
        about = "Über", aboutDesc = "Eine leichte temporäre E-Mail-App mit PearAPI",
        checkUpdate = "Update prüfen", updating = "Prüfe...",
        newVer = "Neue Version verfügbar", downloadNow = "Herunterladen",
        alreadyLatest = "Bereits aktuell", updateFail = "Prüfung fehlgeschlagen",
        author = "Autor",
        version = "Version", langSelect = "Sprache auswählen", darkModeSetting = "Dunkelmodus-Einstellung",
        updateNow = "Jetzt aktualisieren", updateLater = "Später", autoCheckUpdate = "Automatische Update-Prüfung",
        genderMale = "Männlich", genderFemale = "Weiblich", genderOther = "Andere",
        genderOccupied = "Dieses Geschlecht ist bereits vergeben", genderSelect = "Geschlecht auswählen",
        close = "Schließen", copyCode = "Code kopieren", codeCopied = "Code kopiert",
        networkError = "Netzwerkfehler", queryFailed = "Abfrage fehlgeschlagen", fetchFailed = "Abruf fehlgeschlagen",
        unknownSender = "Unbekannter Absender",
        cancel = "Abbrechen", rawData = "Rohdaten:",
        authorHomepage = "Autorenseite", projectRepo = "Projekt-Repository",
        themeStyle = "Design", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Navigationsleiste", barStyleFloat = "Schwebend", barStyleGlass = "Liquid Glass", glassHint = "Leiste lange drücken und ziehen, um Tabs zu wechseln",
        monet = "Monet-Farben", monetDesc = "Eine Farbe aus einem Bild ziehen und daraus ein komplettes Material-3-Farbschema erzeugen",
        monetPickImage = "Bild auswählen", monetPresets = "Voreinstellungen", monetStyle = "Farbstil", monetContrast = "Kontrast",
        monetContrastDefault = "Standard", monetContrastHigh = "Hoch", monetClose = "Dynamische Farben ausschalten",
        monetExtracting = "Farbe wird extrahiert…", monetFailed = "Farbe konnte nicht extrahiert werden, bitte anderes Bild wählen",
        themeSettings = "Themeneinstellungen", themeFollowSystem = "System folgen", themeLight = "Hell", themeDark = "Dunkel",
        monetEnable = "Monet-Farben aktivieren", monetAccent = "Akzentfarbe",
        monetAccentDefault = "Standard", monetAccentCustom = "Benutzerdefiniert",
        barBlur = "Weichzeichnen", barBlurDesc = "Obere und untere Leiste weichzeichnen",
        barFloatDesc = "Schwebende Leiste im Apple-Stil", barGlassDesc = "Flüssigglas-Effekt für die schwebende Leiste",
        mailService = "E-Mail-Dienst",
        mailServiceDesc = "Manche Seiten blockieren stumm PearAPI-Domains; wenn Codes nicht ankommen, zur Backup-Quelle wechseln und neue Adresse erstellen",
        mailboxExpired = "Postfach abgelaufen, bitte ein neues erstellen",
        serviceStatus = "Dienststatus", statusTesting = "Wird getestet…", statusOk = "Normal",
        statusDown = "Nicht erreichbar", testNow = "Jetzt testen",
        newMail = "Neue E-Mail erhalten",
        expiresInFmt = "Läuft ab in %s",
        autoRefresh = "Auto-Aktualisierung"
    )
    "es" -> Strings(
        title = "Correo Temporal",
        generate = "Generar", generating = "Generando...",
        yourEmail = "Tu correo temporal", receivedCount = "Mensajes",
        copy = "Copiar", copied = "Copiado",
        refresh = "Actualizar", noNewMail = "Sin nuevos correos", parseError = "Error de análisis, mostrando datos brutos",
        inbox = "Bandeja de entrada", history = "Historial", settings = "Ajustes",
        historyTitle = "Historial de correos", status = "Estado", active = "Activo", expired = "Expirado",
        check = "Verificar", useNow = "Usar",
        back = "Atrás", languageLabel = "Idioma", darkMode = "Modo oscuro",
        about = "Acerca de", aboutDesc = "Una app ligera de correo temporal basada en PearAPI",
        checkUpdate = "Buscar actualización", updating = "Buscando...",
        newVer = "Nueva versión disponible", downloadNow = "Descargar",
        alreadyLatest = "Ya está actualizado", updateFail = "Error al buscar",
        author = "Autor",
        version = "Versión", langSelect = "Seleccionar idioma", darkModeSetting = "Configurar modo oscuro",
        updateNow = "Actualizar ahora", updateLater = "Después", autoCheckUpdate = "Comprobación automática",
        genderMale = "Masculino", genderFemale = "Femenino", genderOther = "Otro",
        genderOccupied = "Este género ya está ocupado", genderSelect = "Seleccionar género",
        close = "Cerrar", copyCode = "Copiar código", codeCopied = "Código copiado",
        networkError = "Error de red", queryFailed = "Consulta fallida", fetchFailed = "Error al obtener",
        unknownSender = "Remitente desconocido",
        cancel = "Cancelar", rawData = "Datos sin procesar:",
        authorHomepage = "Página del autor", projectRepo = "Repositorio del proyecto",
        themeStyle = "Tema", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Barra inferior", barStyleFloat = "Flotante", barStyleGlass = "Liquid Glass", glassHint = "Mantén pulsada la barra y arrastra para cambiar de pestaña",
        monet = "Colores de Monet", monetDesc = "Extrae un color de una imagen y genera toda la paleta Material 3",
        monetPickImage = "Elegir imagen", monetPresets = "Ajustes preestablecidos", monetStyle = "Estilo de color", monetContrast = "Contraste",
        monetContrastDefault = "Predeterminado", monetContrastHigh = "Alto", monetClose = "Desactivar colores dinámicos",
        monetExtracting = "Extrayendo el color…", monetFailed = "No se pudo extraer el color, prueba con otra imagen",
        themeSettings = "Ajustes de tema", themeFollowSystem = "Seguir el sistema", themeLight = "Claro", themeDark = "Oscuro",
        monetEnable = "Activar colores Monet", monetAccent = "Color de acento",
        monetAccentDefault = "Predeterminado", monetAccentCustom = "Personalizado",
        barBlur = "Desenfoque", barBlurDesc = "Desenfoca las barras superior e inferior",
        barFloatDesc = "Barra inferior flotante estilo Apple", barGlassDesc = "Efecto de cristal líquido en la barra flotante",
        mailService = "Servicio de correo",
        mailServiceDesc = "Algunos sitios bloquean en silencio los códigos de PearAPI; si no llegan, cambia a la fuente alternativa y genera un nuevo correo",
        mailboxExpired = "Buzón expirado, genera uno nuevo",
        serviceStatus = "Estado del servicio", statusTesting = "Probando…", statusOk = "Normal",
        statusDown = "Inaccesible", testNow = "Probar ahora",
        newMail = "Nuevo correo recibido",
        expiresInFmt = "Caduca en %s",
        autoRefresh = "Actualización automática"
    )
    "pt" -> Strings(
        title = "Email Temporário",
        generate = "Gerar", generating = "Gerando...",
        yourEmail = "Seu email temporário", receivedCount = "Mensagens",
        copy = "Copiar", copied = "Copiado",
        refresh = "Atualizar", noNewMail = "Nenhum novo email", parseError = "Erro de análise, exibindo dados brutos",
        inbox = "Caixa de entrada", history = "Histórico", settings = "Configurações",
        historyTitle = "Histórico de emails", status = "Status", active = "Ativo", expired = "Expirado",
        check = "Verificar", useNow = "Usar",
        back = "Voltar", languageLabel = "Idioma", darkMode = "Modo escuro",
        about = "Sobre", aboutDesc = "Um app leve de email temporário com PearAPI",
        checkUpdate = "Verificar atualização", updating = "Verificando...",
        newVer = "Nova versão disponível", downloadNow = "Baixar",
        alreadyLatest = "Já está atualizado", updateFail = "Falha na verificação",
        author = "Autor",
        version = "Versão", langSelect = "Selecionar idioma", darkModeSetting = "Configuração modo escuro",
        updateNow = "Atualizar agora", updateLater = "Depois", autoCheckUpdate = "Verificação automática",
        genderMale = "Masculino", genderFemale = "Feminino", genderOther = "Outro",
        genderOccupied = "Este gênero já está ocupado", genderSelect = "Selecionar gênero",
        close = "Fechar", copyCode = "Copiar código", codeCopied = "Código copiado",
        networkError = "Erro de rede", queryFailed = "Falha na consulta", fetchFailed = "Falha ao obter",
        unknownSender = "Remetente desconhecido",
        cancel = "Cancelar", rawData = "Dados brutos:",
        authorHomepage = "Página do autor", projectRepo = "Repositório do projeto",
        themeStyle = "Tema", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Barra inferior", barStyleFloat = "Flutuante", barStyleGlass = "Liquid Glass", glassHint = "Pressione e arraste a barra para trocar de aba",
        monet = "Cores de Monet", monetDesc = "Extraia uma cor de uma imagem e gere toda a paleta Material 3",
        monetPickImage = "Escolher imagem", monetPresets = "Predefinições", monetStyle = "Estilo de cor", monetContrast = "Contraste",
        monetContrastDefault = "Padrão", monetContrastHigh = "Alto", monetClose = "Desativar cores dinâmicas",
        monetExtracting = "Extraindo a cor…", monetFailed = "Não foi possível extrair a cor, tente outra imagem",
        themeSettings = "Ajustes de tema", themeFollowSystem = "Seguir o sistema", themeLight = "Claro", themeDark = "Escuro",
        monetEnable = "Ativar cores Monet", monetAccent = "Cor de destaque",
        monetAccentDefault = "Padrão", monetAccentCustom = "Personalizada",
        barBlur = "Desfoque", barBlurDesc = "Desfoca as barras superior e inferior",
        barFloatDesc = "Barra inferior flutuante estilo Apple", barGlassDesc = "Efeito de vidro líquido na barra flutuante",
        mailService = "Serviço de e-mail",
        mailServiceDesc = "Alguns sites bloqueiam silenciosamente os códigos do PearAPI; se não chegarem, mude para a fonte alternativa e gere um novo e-mail",
        mailboxExpired = "Caixa de correio expirada, gere uma nova",
        serviceStatus = "Estado do serviço", statusTesting = "Testando…", statusOk = "Normal",
        statusDown = "Inacessível", testNow = "Testar agora",
        newMail = "Novo e-mail recebido",
        expiresInFmt = "Expira em %s",
        autoRefresh = "Atualização automática"
    )
    "ru" -> Strings(
        title = "Временная почта",
        generate = "Создать", generating = "Создание...",
        yourEmail = "Ваш временный email", receivedCount = "Сообщения",
        copy = "Копировать", copied = "Скопировано",
        refresh = "Обновить", noNewMail = "Нет новых писем", parseError = "Ошибка парсинга, показаны сырые данные",
        inbox = "Входящие", history = "История", settings = "Настройки",
        historyTitle = "История писем", status = "Статус", active = "Активен", expired = "Истёк",
        check = "Проверить", useNow = "Использовать",
        back = "Назад", languageLabel = "Язык", darkMode = "Тёмная тема",
        about = "О приложении", aboutDesc = "Лёгкое приложение для временной почты на базе PearAPI",
        checkUpdate = "Проверить обновления", updating = "Проверка...",
        newVer = "Доступна новая версия", downloadNow = "Скачать",
        alreadyLatest = "Уже обновлено", updateFail = "Ошибка проверки",
        author = "Автор",
        version = "Версия", langSelect = "Выбор языка", darkModeSetting = "Настройка тёмной темы",
        updateNow = "Обновить сейчас", updateLater = "Позже", autoCheckUpdate = "Автопроверка обновлений",
        genderMale = "Мужской", genderFemale = "Женский", genderOther = "Другое",
        genderOccupied = "Этот пол уже занят", genderSelect = "Выберите пол",
        close = "Закрыть", copyCode = "Копировать код", codeCopied = "Код скопирован",
        networkError = "Ошибка сети", queryFailed = "Запрос не удался", fetchFailed = "Не удалось получить",
        unknownSender = "Неизвестный отправитель",
        cancel = "Отмена", rawData = "Исходные данные:",
        authorHomepage = "Страница автора", projectRepo = "Репозиторий проекта",
        themeStyle = "Тема", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Нижняя панель", barStyleFloat = "Плавающая", barStyleGlass = "Liquid Glass", glassHint = "Зажмите панель и потяните, чтобы переключить вкладку",
        monet = "Цвета Моне", monetDesc = "Извлеките цвет из изображения и получите полную палитру Material 3",
        monetPickImage = "Выбрать изображение", monetPresets = "Пресеты", monetStyle = "Стиль цвета", monetContrast = "Контраст",
        monetContrastDefault = "Обычный", monetContrastHigh = "Высокий", monetClose = "Отключить динамические цвета",
        monetExtracting = "Извлечение цвета…", monetFailed = "Не удалось извлечь цвет, попробуйте другое изображение",
        themeSettings = "Настройки темы", themeFollowSystem = "Как в системе", themeLight = "Светлая", themeDark = "Тёмная",
        monetEnable = "Включить цвета Monet", monetAccent = "Акцентный цвет",
        monetAccentDefault = "По умолчанию", monetAccentCustom = "Свой",
        barBlur = "Размытие", barBlurDesc = "Размывать верхнюю и нижнюю панели",
        barFloatDesc = "Плавающая панель в стиле Apple", barGlassDesc = "Эффект жидкого стекла для плавающей панели",
        mailService = "Почтовый сервис",
        mailServiceDesc = "Некоторые сайты молча блокируют коды PearAPI; если они не приходят, переключитесь на резервный источник и создайте новый адрес",
        mailboxExpired = "Почтовый ящик истёк, создайте новый",
        serviceStatus = "Состояние сервиса", statusTesting = "Проверка…", statusOk = "Норма",
        statusDown = "Недоступен", testNow = "Проверить сейчас",
        newMail = "Получено новое письмо",
        expiresInFmt = "Истекает через %s",
        autoRefresh = "Автообновление"
    )
    "it" -> Strings(
        title = "Email Temporanea",
        generate = "Genera", generating = "Generazione...",
        yourEmail = "La tua email temporanea", receivedCount = "Messaggi",
        copy = "Copia", copied = "Copiato",
        refresh = "Aggiorna", noNewMail = "Nessuna nuova email", parseError = "Errore di analisi, mostrati dati grezzi",
        inbox = "Posta in arrivo", history = "Cronologia", settings = "Impostazioni",
        historyTitle = "Cronologia email", status = "Stato", active = "Attivo", expired = "Scaduto",
        check = "Controlla", useNow = "Usa",
        back = "Indietro", languageLabel = "Lingua", darkMode = "Modalità scura",
        about = "Informazioni", aboutDesc = "Un'app leggera di email temporanea basata su PearAPI",
        checkUpdate = "Controlla aggiornamenti", updating = "Controllo...",
        newVer = "Nuova versione disponibile", downloadNow = "Scarica",
        alreadyLatest = "Già aggiornato", updateFail = "Controllo fallito",
        author = "Autore",
        version = "Versione", langSelect = "Seleziona lingua", darkModeSetting = "Impostazione modalità scura",
        updateNow = "Aggiorna ora", updateLater = "Dopo", autoCheckUpdate = "Controllo automatico",
        genderMale = "Maschio", genderFemale = "Femmina", genderOther = "Altro",
        genderOccupied = "Questo genere è già occupato", genderSelect = "Seleziona genere",
        close = "Chiudi", copyCode = "Copia codice", codeCopied = "Codice copiato",
        networkError = "Errore di rete", queryFailed = "Query non riuscita", fetchFailed = "Recupero non riuscito",
        unknownSender = "Mittente sconosciuto",
        cancel = "Annulla", rawData = "Dati grezzi:",
        authorHomepage = "Pagina dell'autore", projectRepo = "Repository del progetto",
        themeStyle = "Tema", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Barra inferiore", barStyleFloat = "Fluttuante", barStyleGlass = "Liquid Glass", glassHint = "Tieni premuta la barra e trascina per cambiare scheda",
        monet = "Colori di Monet", monetDesc = "Estrai un colore da un'immagine e genera l'intera palette Material 3",
        monetPickImage = "Scegli immagine", monetPresets = "Preset", monetStyle = "Stile colore", monetContrast = "Contrasto",
        monetContrastDefault = "Predefinito", monetContrastHigh = "Alto", monetClose = "Disattiva colori dinamici",
        monetExtracting = "Estrazione del colore…", monetFailed = "Impossibile estrarre il colore, prova un'altra immagine",
        themeSettings = "Impostazioni tema", themeFollowSystem = "Segui il sistema", themeLight = "Chiaro", themeDark = "Scuro",
        monetEnable = "Attiva colori Monet", monetAccent = "Colore d'accento",
        monetAccentDefault = "Predefinito", monetAccentCustom = "Personalizzato",
        barBlur = "Sfocatura", barBlurDesc = "Sfoca le barre superiore e inferiore",
        barFloatDesc = "Barra inferiore fluttuante in stile Apple", barGlassDesc = "Effetto vetro liquido per la barra fluttuante",
        mailService = "Servizio email",
        mailServiceDesc = "Alcuni siti bloccano silenziosamente i codici PearAPI; se non arrivano, passa alla fonte di backup e genera una nuova email",
        mailboxExpired = "Casella email scaduta, generarne una nuova",
        serviceStatus = "Stato del servizio", statusTesting = "Test in corso…", statusOk = "Normale",
        statusDown = "Irraggiungibile", testNow = "Testa ora",
        newMail = "Nuova email ricevuta",
        expiresInFmt = "Scade tra %s",
        autoRefresh = "Aggiornamento automatico"
    )
    "ar" -> Strings(
        title = "بريد مؤقت",
        generate = "إنشاء", generating = "جارٍ الإنشاء...",
        yourEmail = "بريدك المؤقت", receivedCount = "الرسائل",
        copy = "نسخ", copied = "تم النسخ",
        refresh = "تحديث", noNewMail = "لا توجد رسائل جديدة", parseError = "خطأ في التحليل، عرض البيانات الخام",
        inbox = "صندوق الوارد", history = "السجل", settings = "الإعدادات",
        historyTitle = "سجل البريد", status = "الحالة", active = "نشط", expired = "منتهي",
        check = "تحقق", useNow = "استخدم",
        back = "رجوع", languageLabel = "اللغة", darkMode = "الوضع الداكن",
        about = "حول", aboutDesc = "تطبيق بريد مؤقت خفيف يعمل بواسطة PearAPI",
        checkUpdate = "التحقق من التحديث", updating = "جارٍ التحقق...",
        newVer = "إصدار جديد متاح", downloadNow = "تحميل",
        alreadyLatest = "الإصدار الأحدث", updateFail = "فشل التحقق",
        author = "المؤلف",
        version = "الإصدار", langSelect = "اختر اللغة", darkModeSetting = "إعدادات الوضع الداكن",
        updateNow = "تحديث الآن", updateLater = "لاحقاً", autoCheckUpdate = "التحقق التلقائي من التحديث",
        genderMale = "ذكر", genderFemale = "أنثى", genderOther = "آخر",
        genderOccupied = "هذا الجنس محجوز بالفعل", genderSelect = "اختر الجنس",
        close = "إغلاق", copyCode = "نسخ الرمز", codeCopied = "تم نسخ الرمز",
        networkError = "خطأ في الشبكة", queryFailed = "فشل الاستعلام", fetchFailed = "فشل الجلب",
        unknownSender = "مرسل غير معروف",
        cancel = "إلغاء", rawData = "البيانات الخام:",
        authorHomepage = "صفحة المؤلف", projectRepo = "مستودع المشروع",
        themeStyle = "السمة", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "الشريط السفلي", barStyleFloat = "عائم", barStyleGlass = "Liquid Glass", glassHint = "اضغط مطولًا على الشريط واسحب لتبديل التبويب",
        monet = "ألوان مونيه", monetDesc = "استخرج لونًا من صورة وأنشئ لوحة Material 3 كاملة",
        monetPickImage = "اختيار صورة", monetPresets = "إعدادات جاهزة", monetStyle = "نمط الألوان", monetContrast = "التباين",
        monetContrastDefault = "افتراضي", monetContrastHigh = "عالٍ", monetClose = "إيقاف الألوان الديناميكية",
        monetExtracting = "جارٍ استخراج اللون…", monetFailed = "تعذّر استخراج اللون، جرّب صورة أخرى",
        themeSettings = "إعدادات المظهر", themeFollowSystem = "حسب النظام", themeLight = "فاتح", themeDark = "داكن",
        monetEnable = "تفعيل ألوان Monet", monetAccent = "لون التمييز",
        monetAccentDefault = "افتراضي", monetAccentCustom = "مخصّص",
        barBlur = "تمويه", barBlurDesc = "تفعيل تمويه الشريطين العلوي والسفلي",
        barFloatDesc = "شريط سفلي عائم بأسلوب Apple", barGlassDesc = "تأثير الزجاج السائل للشريط العائم",
        mailService = "خدمة البريد",
        mailServiceDesc = "تحظر بعض المواقع أكواد PearAPI بصمت؛ إذا لم تصل، بدّل إلى المصدر الاحتياطي وأنشئ بريدًا جديدًا",
        mailboxExpired = "انتهت صلاحية صندوق البريد، يرجى إنشاء واحد جديد",
        serviceStatus = "حالة الخدمة", statusTesting = "جارٍ الاختبار…", statusOk = "طبيعي",
        statusDown = "غير متاح", testNow = "اختبر الآن",
        newMail = "وصل بريد جديد",
        expiresInFmt = "ينتهي بعد %s",
        autoRefresh = "تحديث تلقائي"
    )
    "hi" -> Strings(
        title = "अस्थायी मेल",
        generate = "जनरेट करें", generating = "जनरेट हो रहा है...",
        yourEmail = "आपका अस्थायी ईमेल", receivedCount = "संदेश",
        copy = "कॉपी करें", copied = "कॉपी हो गया",
        refresh = "रिफ्रेश", noNewMail = "कोई नया मेल नहीं", parseError = "पार्स त्रुटि, कच्चा डेटा दिखाया गया",
        inbox = "इनबॉक्स", history = "इतिहास", settings = "सेटिंग्स",
        historyTitle = "ईमेल इतिहास", status = "स्थिति", active = "सक्रिय", expired = "समाप्त",
        check = "जाँच करें", useNow = "अब उपयोग करें",
        back = "वापस", languageLabel = "भाषा", darkMode = "डार्क मोड",
        about = "बारे में", aboutDesc = "PearAPI द्वारा संचालित एक हल्का अस्थायी मेल ऐप",
        checkUpdate = "अपडेट जाँचें", updating = "जाँच हो रही है...",
        newVer = "नया संस्करण उपलब्ध", downloadNow = "डाउनलोड करें",
        alreadyLatest = "पहले से नवीनतम", updateFail = "जाँच विफल",
        author = "लेखक",
        version = "संस्करण", langSelect = "भाषा चुनें", darkModeSetting = "डार्क मोड सेटिंग",
        updateNow = "अभी अपडेट करें", updateLater = "बाद में", autoCheckUpdate = "स्वचालित अपडेट जाँच",
        genderMale = "पुरुष", genderFemale = "महिला", genderOther = "अन्य",
        genderOccupied = "यह लिंग पहले से लिया हुआ है", genderSelect = "लिंग चुनें",
        close = "बंद करें", copyCode = "कोड कॉपी करें", codeCopied = "कोड कॉपी हो गया",
        networkError = "नेटवर्क त्रुटि", queryFailed = "क्वेरी विफल", fetchFailed = "प्राप्त करना विफल",
        unknownSender = "अज्ञात प्रेषक",
        cancel = "रद्द करें", rawData = "कच्चा डेटा:",
        authorHomepage = "लेखक का पेज", projectRepo = "प्रोजेक्ट रिपॉज़िटरी",
        themeStyle = "थीम", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "निचला बार", barStyleFloat = "फ़्लोटिंग", barStyleGlass = "Liquid Glass", glassHint = "बार को देर तक दबाकर खींचें और टैब बदलें",
        monet = "मोने रंग", monetDesc = "किसी छवि से एक रंग निकालें और पूरी Material 3 रंग-योजना बनाएँ",
        monetPickImage = "छवि चुनें", monetPresets = "प्रीसेट", monetStyle = "रंग शैली", monetContrast = "कंट्रास्ट",
        monetContrastDefault = "डिफ़ॉल्ट", monetContrastHigh = "उच्च", monetClose = "डायनामिक रंग बंद करें",
        monetExtracting = "रंग निकाला जा रहा है…", monetFailed = "रंग नहीं निकाला जा सका, कोई दूसरी छवि आज़माएँ",
        themeSettings = "थीम सेटिंग", themeFollowSystem = "सिस्टम के अनुसार", themeLight = "हल्का", themeDark = "गहरा",
        monetEnable = "Monet रंग चालू करें", monetAccent = "एक्सेंट रंग",
        monetAccentDefault = "डिफ़ॉल्ट", monetAccentCustom = "कस्टम",
        barBlur = "धुंधलापन", barBlurDesc = "ऊपरी और निचले बार को धुंधला करें",
        barFloatDesc = "Apple शैली का फ़्लोटिंग बॉटम बार", barGlassDesc = "फ़्लोटिंग बार के लिए लिक्विड ग्लास प्रभाव",
        mailService = "मेल सेवा",
        mailServiceDesc = "कुछ साइटें PearAPI डोमेन के कोड चुपचाप ब्लॉक करती हैं; न आने पर बैकअप स्रोत पर स्विच करें और नया पता बनाएँ",
        mailboxExpired = "मेलबॉक्स की समय सीमा समाप्त हो गई है, कृपया नया बनाएँ",
        serviceStatus = "सेवा स्थिति", statusTesting = "जाँच हो रही है…", statusOk = "सामान्य",
        statusDown = "पहुँच योग्य नहीं", testNow = "अभी जाँचें",
        newMail = "नया मेल प्राप्त हुआ",
        expiresInFmt = "%s में समाप्त",
        autoRefresh = "ऑटो रिफ्रेश"
    )
    "vi" -> Strings(
        title = "Mail Tạm Thời",
        generate = "Tạo", generating = "Đang tạo...",
        yourEmail = "Email tạm thời của bạn", receivedCount = "Tin nhắn",
        copy = "Sao chép", copied = "Đã sao chép",
        refresh = "Làm mới", noNewMail = "Không có thư mới", parseError = "Lỗi phân tích, hiển thị dữ liệu thô",
        inbox = "Hộp thư đến", history = "Lịch sử", settings = "Cài đặt",
        historyTitle = "Lịch sử email", status = "Trạng thái", active = "Đang dùng", expired = "Hết hạn",
        check = "Kiểm tra", useNow = "Dùng ngay",
        back = "Quay lại", languageLabel = "Ngôn ngữ", darkMode = "Chế độ tối",
        about = "Giới thiệu", aboutDesc = "Ứng dụng mail tạm thời nhẹ nhàng dựa trên PearAPI",
        checkUpdate = "Kiểm tra cập nhật", updating = "Đang kiểm tra...",
        newVer = "Phiên bản mới có sẵn", downloadNow = "Tải xuống",
        alreadyLatest = "Đã là phiên bản mới nhất", updateFail = "Kiểm tra thất bại",
        author = "Tác giả",
        version = "Phiên bản", langSelect = "Chọn ngôn ngữ", darkModeSetting = "Cài đặt chế độ tối",
        updateNow = "Cập nhật ngay", updateLater = "Để sau", autoCheckUpdate = "Tự động kiểm tra cập nhật",
        genderMale = "Nam", genderFemale = "Nữ", genderOther = "Khác",
        genderOccupied = "Giới tính này đã được sử dụng", genderSelect = "Chọn giới tính",
        close = "Đóng", copyCode = "Sao chép mã", codeCopied = "Đã sao chép mã",
        networkError = "Lỗi mạng", queryFailed = "Truy vấn thất bại", fetchFailed = "Lấy thất bại",
        unknownSender = "Người gửi không xác định",
        cancel = "Hủy", rawData = "Dữ liệu thô:",
        authorHomepage = "Trang tác giả", projectRepo = "Kho dự án",
        themeStyle = "Chủ đề", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Thanh dưới", barStyleFloat = "Nổi", barStyleGlass = "Liquid Glass", glassHint = "Nhấn giữ thanh và kéo để chuyển tab",
        monet = "Màu Monet", monetDesc = "Lấy một màu từ ảnh để tạo toàn bộ bảng màu Material 3",
        monetPickImage = "Chọn ảnh", monetPresets = "Cài sẵn", monetStyle = "Kiểu màu", monetContrast = "Độ tương phản",
        monetContrastDefault = "Mặc định", monetContrastHigh = "Cao", monetClose = "Tắt màu động",
        monetExtracting = "Đang trích xuất màu…", monetFailed = "Không trích xuất được màu, hãy thử ảnh khác",
        themeSettings = "Cài đặt chủ đề", themeFollowSystem = "Theo hệ thống", themeLight = "Sáng", themeDark = "Tối",
        monetEnable = "Bật màu Monet", monetAccent = "Màu nhấn",
        monetAccentDefault = "Mặc định", monetAccentCustom = "Tùy chỉnh",
        barBlur = "Làm mờ", barBlurDesc = "Làm mờ thanh trên và thanh dưới",
        barFloatDesc = "Thanh dưới nổi kiểu Apple", barGlassDesc = "Hiệu ứng kính lỏng cho thanh nổi",
        mailService = "Dịch vụ mail",
        mailServiceDesc = "Một số trang web âm thầm chặn mã PearAPI; nếu không nhận được, hãy chuyển sang nguồn dự phòng và tạo địa chỉ mới",
        mailboxExpired = "Hộp thư đã hết hạn, vui lòng tạo hộp thư mới",
        serviceStatus = "Trạng thái dịch vụ", statusTesting = "Đang kiểm tra…", statusOk = "Bình thường",
        statusDown = "Không truy cập được", testNow = "Kiểm tra ngay",
        newMail = "Đã nhận thư mới",
        expiresInFmt = "Hết hạn sau %s",
        autoRefresh = "Tự động làm mới"
    )
    "th" -> Strings(
        title = "อีเมลชั่วคราว",
        generate = "สร้าง", generating = "กำลังสร้าง...",
        yourEmail = "อีเมลชั่วคราวของคุณ", receivedCount = "ข้อความ",
        copy = "คัดลอก", copied = "คัดลอกแล้ว",
        refresh = "รีเฟรช", noNewMail = "ไม่มีอีเมลใหม่", parseError = "ข้อผิดพลาดในการแยกวิเคราะห์ แสดงข้อมูลดิบ",
        inbox = "กล่องจดหมาย", history = "ประวัติ", settings = "การตั้งค่า",
        historyTitle = "ประวัติอีเมล", status = "สถานะ", active = "ใช้งานอยู่", expired = "หมดอายุ",
        check = "ตรวจสอบ", useNow = "ใช้เลย",
        back = "กลับ", languageLabel = "ภาษา", darkMode = "โหมดมืด",
        about = "เกี่ยวกับ", aboutDesc = "แอปอีเมลชั่วคราวน้ำหนักเบาที่ใช้ PearAPI",
        checkUpdate = "ตรวจสอบอัปเดต", updating = "กำลังตรวจสอบ...",
        newVer = "มีเวอร์ชันใหม่", downloadNow = "ดาวน์โหลด",
        alreadyLatest = "เป็นเวอร์ชันล่าสุดแล้ว", updateFail = "ตรวจสอบล้มเหลว",
        author = "ผู้เขียน",
        version = "เวอร์ชัน", langSelect = "เลือกภาษา", darkModeSetting = "การตั้งค่าโหมดมืด",
        updateNow = "อัปเดตตอนนี้", updateLater = "ทีหลัง", autoCheckUpdate = "ตรวจสอบอัปเดตอัตโนมัติ",
        genderMale = "ชาย", genderFemale = "หญิง", genderOther = "อื่น ๆ",
        genderOccupied = "เพศนี้ถูกใช้แล้ว", genderSelect = "เลือกเพศ",
        close = "ปิด", copyCode = "คัดลอกรหัส", codeCopied = "คัดลอกรหัสแล้ว",
        networkError = "ข้อผิดพลาดเครือข่าย", queryFailed = "การสอบถามล้มเหลว", fetchFailed = "ดึงข้อมูลล้มเหลว",
        unknownSender = "ผู้ส่งที่ไม่รู้จัก",
        cancel = "ยกเลิก", rawData = "ข้อมูลดิบ:",
        authorHomepage = "หน้าผู้เขียน", projectRepo = "ที่เก็บโปรเจ็กต์",
        themeStyle = "ธีม", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "แถบล่าง", barStyleFloat = "ลอย", barStyleGlass = "Liquid Glass", glassHint = "กดแถบค้างแล้วลากเพื่อสลับแท็บ",
        monet = "สีแบบโมเนต์", monetDesc = "ดึงสีหนึ่งจากรูปภาพเพื่อสร้างชุดสี Material 3 ทั้งหมด",
        monetPickImage = "เลือกรูปภาพ", monetPresets = "ค่าที่ตั้งไว้", monetStyle = "สไตล์สี", monetContrast = "คอนทราสต์",
        monetContrastDefault = "ค่าเริ่มต้น", monetContrastHigh = "สูง", monetClose = "ปิดสีไดนามิก",
        monetExtracting = "กำลังดึงสี…", monetFailed = "ดึงสีไม่สำเร็จ ลองใช้รูปอื่น",
        themeSettings = "ตั้งค่าธีม", themeFollowSystem = "ตามระบบ", themeLight = "สว่าง", themeDark = "มืด",
        monetEnable = "เปิดใช้สี Monet", monetAccent = "สีเน้น",
        monetAccentDefault = "ค่าเริ่มต้น", monetAccentCustom = "กำหนดเอง",
        barBlur = "เบลอ", barBlurDesc = "เปิดเบลอแถบด้านบนและด้านล่าง",
        barFloatDesc = "แถบล่างแบบลอยสไตล์ Apple", barGlassDesc = "เอฟเฟกต์กระจกเหลวสำหรับแถบลอย",
        mailService = "บริการเมล",
        mailServiceDesc = "บางเว็บไซต์บล็อกรหัสจากโดเมน PearAPI โดยเงียบ ๆ หากไม่ได้รับ ให้สลับไปแหล่งสำรองและสร้างที่อยู่ใหม่",
        mailboxExpired = "กล่องจดหมายหมดอายุแล้ว โปรดสร้างใหม่",
        serviceStatus = "สถานะบริการ", statusTesting = "กำลังทดสอบ…", statusOk = "ปกติ",
        statusDown = "เข้าถึงไม่ได้", testNow = "ทดสอบเลย",
        newMail = "ได้รับอีเมลใหม่",
        expiresInFmt = "หมดอายุใน %s",
        autoRefresh = "รีเฟรชอัตโนมัติ"
    )
    "id" -> Strings(
        title = "Email Sementara",
        generate = "Buat", generating = "Membuat...",
        yourEmail = "Email sementara Anda", receivedCount = "Pesan",
        copy = "Salin", copied = "Disalin",
        refresh = "Muat ulang", noNewMail = "Tidak ada email baru", parseError = "Kesalahan parsing, menampilkan data mentah",
        inbox = "Kotak Masuk", history = "Riwayat", settings = "Pengaturan",
        historyTitle = "Riwayat Email", status = "Status", active = "Aktif", expired = "Kedaluwarsa",
        check = "Periksa", useNow = "Gunakan",
        back = "Kembali", languageLabel = "Bahasa", darkMode = "Mode Gelap",
        about = "Tentang", aboutDesc = "Aplikasi email sementara ringan berbasis PearAPI",
        checkUpdate = "Periksa Pembaruan", updating = "Memeriksa...",
        newVer = "Versi baru tersedia", downloadNow = "Unduh",
        alreadyLatest = "Sudah versi terbaru", updateFail = "Pemeriksaan gagal",
        author = "Penulis",
        version = "Versi", langSelect = "Pilih Bahasa", darkModeSetting = "Pengaturan Mode Gelap",
        updateNow = "Perbarui Sekarang", updateLater = "Nanti", autoCheckUpdate = "Periksa Pembaruan Otomatis",
        genderMale = "Pria", genderFemale = "Wanita", genderOther = "Lainnya",
        genderOccupied = "Jenis kelamin ini sudah digunakan", genderSelect = "Pilih jenis kelamin",
        close = "Tutup", copyCode = "Salin Kode", codeCopied = "Kode disalin",
        networkError = "Kesalahan jaringan", queryFailed = "Kueri gagal", fetchFailed = "Gagal mengambil",
        unknownSender = "Pengirim tidak dikenal",
        cancel = "Batal", rawData = "Data mentah:",
        authorHomepage = "Halaman Penulis", projectRepo = "Repositori Proyek",
        themeStyle = "Tema", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "Bilah bawah", barStyleFloat = "Mengambang", barStyleGlass = "Liquid Glass", glassHint = "Tekan lama bilah lalu geser untuk berpindah tab",
        monet = "Warna Monet", monetDesc = "Ambil satu warna dari gambar untuk membuat skema Material 3 lengkap",
        monetPickImage = "Pilih gambar", monetPresets = "Preset", monetStyle = "Gaya warna", monetContrast = "Kontras",
        monetContrastDefault = "Bawaan", monetContrastHigh = "Tinggi", monetClose = "Matikan warna dinamis",
        monetExtracting = "Mengekstrak warna…", monetFailed = "Warna gagal diekstrak, coba gambar lain",
        themeSettings = "Pengaturan Tema", themeFollowSystem = "Ikuti sistem", themeLight = "Terang", themeDark = "Gelap",
        monetEnable = "Aktifkan warna Monet", monetAccent = "Warna aksen",
        monetAccentDefault = "Bawaan", monetAccentCustom = "Kustom",
        barBlur = "Blur", barBlurDesc = "Blur bilah atas dan bilah bawah",
        barFloatDesc = "Bilah bawah mengambang gaya Apple", barGlassDesc = "Efek kaca cair untuk bilah mengambang",
        mailService = "Layanan email",
        mailServiceDesc = "Beberapa situs diam-diam memblokir kode PearAPI; jika tidak diterima, beralih ke sumber cadangan dan buat alamat baru",
        mailboxExpired = "Kotak mail sudah kedaluwarsa, silakan buat baru",
        serviceStatus = "Status layanan", statusTesting = "Menguji…", statusOk = "Normal",
        statusDown = "Tidak dapat diakses", testNow = "Uji sekarang",
        newMail = "Email baru diterima",
        expiresInFmt = "Kedaluwarsa dalam %s",
        autoRefresh = "Refresh otomatis"
    )
    else -> Strings(
        title = "临时邮箱",
        generate = "生成新邮箱", generating = "生成中...",
        yourEmail = "你的临时邮箱", receivedCount = "收件数",
        copy = "复制", copied = "已复制",
        refresh = "刷新", noNewMail = "暂无新邮件", parseError = "解析异常，查看原始数据",
        inbox = "收件箱", history = "历史", settings = "设置",
        historyTitle = "历史邮箱", status = "状态", active = "使用中", expired = "已过期",
        check = "检查", useNow = "使用",
        back = "返回", languageLabel = "语言", darkMode = "深色模式",
        about = "关于", aboutDesc = "基于 PearAPI 的轻量级临时邮箱应用" ,
        checkUpdate = "检查更新", updating = "检查中...",
        newVer = "新版本可用", downloadNow = "下载更新",
        alreadyLatest = "已是最新版本", updateFail = "检查更新失败",
        author = "作者信息",
        version = "版本", langSelect = "选择语言", darkModeSetting = "深色模式设置",
        updateNow = "在线更新", updateLater = "暂不更新", autoCheckUpdate = "启动时自动检查更新",
        genderMale = "男", genderFemale = "女", genderOther = "其他",
        genderOccupied = "该性别已被占用", genderSelect = "选择性别",
        close = "关闭", copyCode = "复制验证码", codeCopied = "验证码已复制",
        networkError = "网络错误", queryFailed = "查询失败", fetchFailed = "获取失败",
        unknownSender = "未知发件人",
        cancel = "取消", rawData = "原始数据:",
        authorHomepage = "作者主页", projectRepo = "项目仓库",
        themeStyle = "主题风格", themeDefault = "Material3", themeHyperOS = "Miuix",
        barStyle = "底栏风格", barStyleFloat = "悬浮底栏", barStyleGlass = "Liquid Glass", glassHint = "长按底栏可左右拖动切换标签",
        monet = "莫奈取色", monetDesc = "从图片里取一个颜色，生成整套 Material 3 配色",
        monetPickImage = "选择图片", monetPresets = "预设", monetStyle = "配色风格", monetContrast = "对比度",
        monetContrastDefault = "默认", monetContrastHigh = "高", monetClose = "关闭动态配色",
        monetExtracting = "正在提取主色…", monetFailed = "取色失败，请换一张图片",
        themeSettings = "主题设置", themeFollowSystem = "跟随系统", themeLight = "浅色", themeDark = "深色",
        monetEnable = "启用 Monet 颜色", monetAccent = "强调色",
        monetAccentDefault = "默认", monetAccentCustom = "自定义",
        barBlur = "模糊", barBlurDesc = "启用顶栏和底栏的模糊效果",
        barFloatDesc = "使用 Apple 风格的悬浮底栏", barGlassDesc = "启用悬浮底栏的液态玻璃效果",
        mailService = "邮箱服务",
        mailServiceDesc = "部分网站会静默拦截 PearAPI 域名的验证码，收不到时可切换到备用源并重新生成邮箱",
        mailboxExpired = "邮箱已过期，请生成新邮箱",
        serviceStatus = "服务状态", statusTesting = "检测中…", statusOk = "正常",
        statusDown = "无法访问", testNow = "立即检测",
        newMail = "收到新邮件",
        expiresInFmt = "%s后过期",
        autoRefresh = "自动刷新"
    )
}

/** Tab 文案（根布局底栏与主题设置页预览示意图共用）。 */
private fun tabLabelOf(tab: Tab, s: Strings): String = when (tab) {
    Tab.Inbox -> s.inbox
    Tab.History -> s.history
    Tab.Settings -> s.settings
}

enum class Tab(val icon: ImageVector) {
    Inbox(Icons.Default.Email),
    History(Icons.Default.DateRange),
    Settings(Icons.Default.Settings)
}

// 底栏形态，仅在 Miuix 主题下可选。持久化使用稳定字符串 key；
// fromKey 对未知值一律回退 Float（现有悬浮底栏），禁止直接 valueOf。
// 三个取值覆盖了"悬浮 / 液态玻璃"两个开关的全部有效组合，无效组合（如悬浮关闭但液态玻璃开启）
// 在类型层面就不存在：
//   Float      = 悬浮开 + 液态玻璃关 → 普通悬浮底栏
//   LiquidGlass= 悬浮开 + 液态玻璃开 → 悬浮液态玻璃底栏
//   Edge       = 悬浮关              → 贴边底栏（液态玻璃不可用）
enum class BarStyle(val key: String) {
    Float("float"),
    LiquidGlass("liquid_glass"),
    Edge("edge");

    companion object {
        fun fromKey(key: String?): BarStyle = entries.find { it.key == key } ?: Float
    }
}

// 邮箱服务源：PearAPI 为默认；instanttempemail.com（ITE）为备用源——
// 其域名（fpklm.com）不在常见一次性邮箱黑名单内，部分网站（如 qoder）会静默丢弃
// 发往 PearAPI 域名（catchmail.io / uberip.com 等）的验证码，却能正常投递到 ITE 域名。
// fromKey 对未知值一律回退 PearAPI，禁止直接 valueOf。
enum class MailProvider(val key: String) {
    PearApi("pearapi"),
    InstantTempEmail("ite");

    companion object {
        fun fromKey(key: String?): MailProvider = entries.find { it.key == key } ?: PearApi
    }
}

data class EmailItem(
    val from: String,
    val subject: String,
    val time: String,
    val body: String,
    val htmlBody: String = "",
    val timestamp: Long = 0L
)

data class HistoryEmail(
    val email: String,
    val isActive: Boolean,
    // instanttempemail.com 邮箱的查询令牌（收件箱钥匙）；PearAPI 邮箱无状态，恒为空串
    val token: String = "",
    // 邮箱过期时刻（epoch 毫秒，0=未知）：ITE 来自服务端 expires；PearAPI 为生成时刻+10 分钟。
    // 历史条目带上它，切回历史邮箱时倒计时才不会丢
    val expiresAt: Long = 0L
)

data class AppState(
    val email: String = "",
    val count: Int = 0,
    val rawMessages: List<String> = emptyList(),
    val items: List<EmailItem> = emptyList(),
    val isLoading: Boolean = false,
    val history: List<HistoryEmail> = emptyList(),
    val currentTab: Tab = Tab.Inbox,
    // 明暗三态（跟随系统 / 浅色 / 深色）：跟随系统只在内部解析成布尔，不改变系统本身设置
    val themeMode: ThemeMode = ThemeMode.Light,
    val language: String = "zh",
    val autoCheckUpdate: Boolean = true,
    val themeStyle: ThemeStyle = ThemeStyle.Material3,
    val barStyle: BarStyle = BarStyle.Float,
    // 模糊总开关：关闭后液态玻璃底栏退化为不带模糊的普通底栏（低版本仍可正常显示）
    val glassBlurEnabled: Boolean = true,
    // 动态配色（默认关闭）：seed = NoDynamicSeed 时两套主题的配色都与定制前完全一致
    val dynamicSeed: Int = NoDynamicSeed,
    val dynamicStyle: DynamicStyle = DynamicStyle.TonalSpot,
    val dynamicContrast: Float = 0f,
    // 邮箱服务源：仅决定"生成新邮箱"按钮调用哪个服务（持久化设置）。
    // 当前邮箱的刷新不用它判断——按 iteToken 是否非空自动分流，切换服务不会打断现有邮箱的收件
    val mailProvider: MailProvider = MailProvider.PearApi,
    // instanttempemail.com 当前邮箱的查询令牌；非空 ⇒ 当前邮箱属于 ITE，刷新走 ITE 接口
    val iteToken: String = "",
    // 当前邮箱过期时刻（epoch 毫秒，0=未知，不显示倒计时）。
    // ITE 由服务端 expires 提供（每次轮询回填校准）；PearAPI 接口只给时长"10 minutes"，
    // 故取生成时刻+10 分钟估算，与实际行为一致
    val mailboxExpiresAt: Long = 0L
)

/** 明暗三态读取：优先新键 themeMode，旧版本只有布尔 isDarkMode，做一次性兼容读取。 */
private fun readThemeMode(prefs: android.content.SharedPreferences): ThemeMode {
    prefs.getString("themeMode", null)?.let { return ThemeMode.fromKey(it) }
    return if (prefs.getBoolean("isDarkMode", false)) ThemeMode.Dark else ThemeMode.Light
}

private val disclaimerText = """
临时邮箱服务免责声明

感谢您使用本临时邮箱服务（以下简称"本服务"）。本服务是一款完全开源的软件项目，旨在为用户提供一次性、匿名的电子邮箱地址，用于注册验证、临时通信等场景。

在使用本服务前，请您仔细阅读并理解本免责声明。一旦您开始使用本服务，即视为您已阅读、理解并同意接受本免责声明的全部条款。

1. 开源性质与"按现状"提供
本服务以"按现状"（AS IS）和"按可用性"（AS AVAILABLE）原则提供。开发者及贡献者不对本服务的完整性、可靠性、准确性、适用性、无中断性或无错误性作任何明示或暗示的担保。

2. 邮箱性质与数据安全
本服务提供的邮箱地址是临时存在的，不保证邮箱的长期可用性。邮件可能会在未通知的情况下被自动删除。请勿使用本服务传输任何敏感、机密、涉及个人隐私或受法律保护的信息。

3. 用户行为与禁止条款
禁止发送垃圾邮件、恶意软件、病毒或钓鱼链接；禁止利用本服务进行欺诈、侵权、诽谤或违反相关法律法规的行为。

4. 第三方链接与内容
本服务可能包含指向第三方网站或资源的链接。开发者不对第三方网站的内容、准确性、安全性或隐私政策承担任何责任。

5. 服务中断与技术故障
本服务可能因维护、升级、硬件故障、网络攻击或不可抗力因素导致服务中断。开发者不保证服务100%可用性。

6. 免费服务限制
本服务为无偿提供的公益或演示性质服务。开发者保留在任何时间修改、暂停、终止服务的权利。

7. 适用法律与管辖
因使用本服务产生的任何争议，应提交至开发者所在地有管辖权的法院解决。

8. 条款修改
开发者保留随时修改本免责声明的权利。修改后的声明将在发布即生效。
""".trimIndent()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 默认的 auto() 会在 3 键导航（API 29+ 由系统绘制 scrim）与 API 26–28（直接使用
        // DefaultLightScrim = #E6FFFFFF）上把导航栏刷成近白色，在悬浮底栏下方形成纯白长条。
        // 显式传透明 scrim：light() 的 nightMode ≠ AUTO，可同时关闭系统强制对比。
        // 导航栏图标明暗仍由下方 Compose 逻辑按应用内深色开关控制。
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("app", MODE_PRIVATE) }
            var state by rememberSaveable(stateSaver = AppStateSaver) {
                mutableStateOf(AppState(
                    language = prefs.getString("language", "zh") ?: "zh",
                    themeMode = readThemeMode(prefs),
                    autoCheckUpdate = prefs.getBoolean("autoCheckUpdate", true),
                    themeStyle = ThemeStyle.fromKey(prefs.getString("themeStyle", ThemeStyle.Material3.key)),
                    barStyle = BarStyle.fromKey(prefs.getString("barStyle", BarStyle.Float.key)),
                    glassBlurEnabled = prefs.getBoolean("glassBlurEnabled", true),
                    dynamicSeed = prefs.getInt("dynamicSeed", NoDynamicSeed),
                    dynamicStyle = DynamicStyle.fromKey(prefs.getString("dynamicStyle", DynamicStyle.TonalSpot.key)),
                    dynamicContrast = prefs.getFloat("dynamicContrast", 0f),
                    mailProvider = MailProvider.fromKey(prefs.getString("mailProvider", MailProvider.PearApi.key))
                ))
            }
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val s = strings(state.language)

            // 设置页当前子页放在根布局持有（而不是设置页内部）：
            // 底栏形态切换会让 GlassShell 走不同的布局分支（Scaffold ↔ 玻璃 Box+Scaffold），
            // 分支切换会改变该子树的组合 key，状态若留在分支内部就会被重置——
            // 表现就是"一改液态玻璃/模糊就闪回设置主页"。放在分支之外即可保持当前子页。
            val settingsPageState = rememberSaveable(stateSaver = SettingsPageSaver) {
                mutableStateOf(SettingsPage.Main)
            }

            // 明暗三态解析成实际要用的布尔：跟随系统时与系统夜间模式实时同步，
            // 浅色/深色为显式覆盖。整棵 UI 树只认这个 darkTheme，不再各自读系统设置。
            val systemDark = isSystemInDarkTheme()
            val darkTheme = state.themeMode.isDark(systemDark)

            // 状态栏/导航栏图标颜色需跟随应用实际明暗（跟随系统时会随系统切换一起变）
            val view = LocalView.current
            LaunchedEffect(darkTheme) {
                val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }

            var showDisclaimer by remember { mutableStateOf(!prefs.getBoolean("disclaimer_accepted", false)) }
            var selectedGender by remember { mutableStateOf("") }

            var showUpdateDialog by remember { mutableStateOf(false) }
            var updateUrl by remember { mutableStateOf("") }
            var updateTag by remember { mutableStateOf("") }
            var updateBody by remember { mutableStateOf("") }
            var updateSha by remember { mutableStateOf("") }
            var showDownloadProgress by remember { mutableStateOf(false) }
            var downloadProgress by remember { mutableStateOf(0) }
            var poem by remember { mutableStateOf<PoemLine?>(null) }

            val client = remember {
                OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
            }

            // 从"未知来源"授权页返回后自动续跑下载（仍未授权则 downloadInstall 内再次跳转）
            var retryDownload by remember { mutableStateOf(false) }
            val installPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                retryDownload = true
            }

            var downloadCall by remember { mutableStateOf<Call?>(null) }

            fun downloadInstall(url: String, expectedSha256: String) {
                if (expectedSha256.isBlank()) {
                    // 缺少校验信息时拒绝下载，防止更新链路被篡改
                    scope.launch { snackbar.showSnackbar(s.updateFail) }
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!context.packageManager.canRequestPackageInstalls()) {
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        installPermissionLauncher.launch(intent)
                        return
                    }
                }
                showDownloadProgress = true
                downloadProgress = 0
                scope.launch(Dispatchers.IO) {
                    var file: File? = null
                    try {
                        val base = context.getExternalFilesDir(null)
                        if (base == null) {
                            withContext(Dispatchers.Main) {
                                showDownloadProgress = false
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        val dir = File(base, "updates")
                        dir.mkdirs()
                        val f = File(dir, "app-release.apk")
                        file = f
                        val dl = Request.Builder().url(url).get().build()
                        val call = client.newCall(dl)
                        downloadCall = call
                        val resp = call.execute()
                        val total = resp.body?.contentLength() ?: -1L
                        val source = resp.body?.byteStream() ?: return@launch
                        val md = MessageDigest.getInstance("SHA-256")
                        f.outputStream().use { out ->
                            val buf = ByteArray(8192)
                            var read: Int
                            var sofar = 0L
                            while (source.read(buf).also { read = it } != -1) {
                                md.update(buf, 0, read)
                                out.write(buf, 0, read)
                                sofar += read
                                if (total > 0) {
                                    val pct = (sofar * 100 / total).toInt()
                                    withContext(Dispatchers.Main) { downloadProgress = pct }
                                }
                            }
                        }
                        resp.close()
                        val actual = md.digest().joinToString("") { "%02x".format(it) }
                        if (!actual.equals(expectedSha256, ignoreCase = true)) {
                            f.delete()
                            withContext(Dispatchers.Main) {
                                showDownloadProgress = false
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        withContext(Dispatchers.Main) { showDownloadProgress = false }
                        val uri = FileProvider.getUriForFile(context,
                            "${context.packageName}.fileprovider", f)
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/vnd.android.package-archive")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // 用户主动取消不提示错误；失败/取消都删除不完整的安装包
                        try { file?.delete() } catch (_: Exception) { }
                        withContext(Dispatchers.Main) {
                            showDownloadProgress = false
                            if (downloadCall?.isCanceled() != true) {
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                        }
                    } finally {
                        downloadCall = null
                    }
                }
            }

            // 授权返回后由标志位驱动重试下载
            LaunchedEffect(retryDownload) {
                if (retryDownload) {
                    retryDownload = false
                    downloadInstall(updateUrl, updateSha)
                }
            }

            fun checkUpdate(isManual: Boolean = false) {
                if (isManual) scope.launch { snackbar.showSnackbar(s.updating) }
                scope.launch(Dispatchers.IO) {
                    try {
                        val r = Request.Builder()
                            .url("https://api.github.com/repos/wzhdgithub/tempmail/releases/latest")
                            .header("Accept", "application/vnd.github.v3+json")
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        val tag = j.optString("tag_name", "").removePrefix("v").trim()
                        if (tag.isBlank()) {
                            // 非 Release 响应（限流 403 / 拦截页 / API 变更），不能误报为"已最新"
                            if (isManual) withContext(Dispatchers.Main) {
                                scope.launch { snackbar.showSnackbar(s.updateFail) }
                            }
                            return@launch
                        }
                        val cur = BuildConfig.VERSION_NAME
                        if (versionCompare(tag, cur) <= 0) {
                            if (isManual) withContext(Dispatchers.Main) {
                                scope.launch { snackbar.showSnackbar(s.alreadyLatest) }
                            }
                            return@launch
                        }
                        val assets = j.optJSONArray("assets") ?: return@launch
                        val asset = assets.getJSONObject(0)
                        val url = asset.optString("browser_download_url", "")
                        if (url.isBlank()) return@launch
                        val releaseNotes = j.optString("body", "")
                        // 优先使用 GitHub Release asset 的 digest 字段，其次从发版说明中提取 64 位十六进制哈希
                        var sha = asset.optString("digest", "").removePrefix("sha256:").trim()
                        if (!sha.matches(Regex("^[0-9a-fA-F]{64}$"))) {
                            sha = Regex("\\b[0-9a-fA-F]{64}\\b").find(releaseNotes)?.value ?: ""
                        }
                        withContext(Dispatchers.Main) {
                            updateTag = tag
                            updateUrl = url
                            updateSha = sha
                            updateBody = releaseNotes
                            showUpdateDialog = true
                        }
                    } catch (e: Exception) {
                        if (isManual) withContext(Dispatchers.Main) {
                            scope.launch { snackbar.showSnackbar(s.updateFail) }
                        }
                    }
                }
            }

            // silent=true 供自动轮询使用：不置 isLoading（避免生成按钮周期性闪禁用）、不弹错误提示
            fun doRefresh(e: String, onDone: (count: Int, raw: String) -> Unit, silent: Boolean = false) {
                if (!silent) state = state.copy(isLoading = true)
                scope.launch(Dispatchers.IO) {
                    try {
                        val apiUrl = "https://api.pearapi.ai/api/email/".toHttpUrl().newBuilder()
                            .addQueryParameter("type", "receive")
                            .addQueryParameter("email", e)
                            .build()
                        val r = Request.Builder()
                            .url(apiUrl)
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        if (j.optString("code") == "200") {
                            val raw = j.optString("receivedata", "")
                            val cnt = j.optString("count", "0").toIntOrNull() ?: 0
                            withContext(Dispatchers.Main) { onDone(cnt, raw) }
                        } else {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(j.optString("msg", s.queryFailed)) }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            state = state.copy(isLoading = false)
                            if (!silent) scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            }

            // instanttempemail.com 收件箱查询：GET /api/inbox/{token}
            // 200 → {address, emails:[...], expires}；404 → 邮箱已过期（7 天有效期）
            fun doRefreshIte(token: String, onDone: (count: Int, raw: String) -> Unit, silent: Boolean = false) {
                if (!silent) state = state.copy(isLoading = true)
                scope.launch(Dispatchers.IO) {
                    try {
                        val apiUrl = "https://instanttempemail.com/api/inbox/".toHttpUrl().newBuilder()
                            .addPathSegment(token)
                            .build()
                        val r = Request.Builder()
                            .url(apiUrl)
                            .get().build()
                        val resp = client.newCall(r).execute()
                        val body = resp.body?.string() ?: ""
                        if (resp.code == 404) {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(s.mailboxExpired) }
                            }
                        } else if (resp.isSuccessful) {
                            val cnt = try {
                                JSONObject(body).optJSONArray("emails")?.length() ?: 0
                            } catch (_: Exception) { 0 }
                            withContext(Dispatchers.Main) { onDone(cnt, body) }
                        } else {
                            withContext(Dispatchers.Main) {
                                state = state.copy(isLoading = false)
                                if (!silent) scope.launch { snackbar.showSnackbar(s.queryFailed) }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            state = state.copy(isLoading = false)
                            if (!silent) scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            }

            // 收件箱刷新统一入口：手动按钮与自动轮询共用同一套解析/合并/提示逻辑。
            // silent=true（自动轮询）：不打扰用户——无"暂无邮件"提示，仅在新邮件到达时提醒
            fun performInboxRefresh(silent: Boolean) {
                val emailAtRefresh = state.email
                val tokenAtRefresh = state.iteToken
                // 当前邮箱归属按 iteToken 是否非空判断：非空 ⇒ ITE 邮箱（token 即收件箱钥匙）
                val useIte = tokenAtRefresh.isNotBlank()
                fun deliver(cnt: Int, raw: String) {
                    val newItems = if (raw.isBlank()) emptyList()
                        else if (useIte) parseIteEmails(raw, s.unknownSender)
                        else parseEmails(raw, s.unknownSender)
                    // ITE 每次轮询都返回服务端 expires：回填校准本地过期时刻（以服务端为准）
                    val serverExpires = if (useIte && raw.isNotBlank()) {
                        try { parseEmailTime(JSONObject(raw).optString("expires", "")) } catch (_: Exception) { 0L }
                    } else 0L
                    var mergedSize = -1
                    var prevSize = -1
                    // 基于写入时的最新状态更新，避免覆盖刷新期间切换的设置项
                    val cur0 = state
                    state = if (cur0.email != emailAtRefresh) {
                        cur0.copy(isLoading = false)
                    } else {
                        val merged = mergeEmailItems(cur0.items, newItems)
                        mergedSize = merged.size
                        prevSize = cur0.items.size
                        Log.d("MAIL_DEBUG", "API返回count=$cnt 解析后=${newItems.size} 已有=${cur0.items.size} 合并后=${merged.size}")
                        // rawMessages 只在本次确实解析到邮件时追加：
                        // ITE 空收件箱返回完整 JSON（非空串），若按 raw 非空判断，
                        // 每次刷新都会往"原始数据"区塞一条相同内容
                        val newRaws = if (newItems.isEmpty()) cur0.rawMessages
                            else (cur0.rawMessages + raw).takeLast(5)
                        cur0.copy(
                            count = cnt, rawMessages = newRaws,
                            items = merged, isLoading = false,
                            mailboxExpiresAt = if (serverExpires > 0) serverExpires else cur0.mailboxExpiresAt
                        )
                    }
                    val added = if (mergedSize >= 0 && prevSize >= 0) mergedSize - prevSize else 0
                    scope.launch {
                        if (added > 0) {
                            // 新邮件到达：手动/自动都提醒（自动轮询存在的主要意义）
                            snackbar.showSnackbar(if (added == 1) s.newMail else "${s.newMail} ×$added")
                        } else if (!silent) {
                            // 用解析结果而非 raw 判空：ITE 空收件箱的 raw 是完整 JSON（非空串）
                            if (newItems.isEmpty()) snackbar.showSnackbar(s.noNewMail)
                            else if (mergedSize == 0) snackbar.showSnackbar(s.parseError)
                        }
                    }
                }
                if (useIte) doRefreshIte(tokenAtRefresh, { cnt, raw -> deliver(cnt, raw) }, silent)
                else doRefresh(emailAtRefresh, { cnt, raw -> deliver(cnt, raw) }, silent)
            }

            // 前台感知：后台时暂停轮询，省电省流量（ON_RESUME 恢复）
            var isAppInForeground by remember { mutableStateOf(true) }
            DisposableEffect(this@MainActivity) {
                val observer = LifecycleEventObserver { _, event ->
                    isAppInForeground = event == Lifecycle.Event.ON_RESUME
                }
                this@MainActivity.lifecycle.addObserver(observer)
                onDispose { this@MainActivity.lifecycle.removeObserver(observer) }
            }

            // 自动轮询倒计时（秒）：收件箱刷新按钮旁显示，让用户直观看到下次刷新时机
            var pollCountdownSec by remember { mutableStateOf(0) }

            // 收件箱自动轮询：前台 + 非免责页 + 停留在收件箱 + 已有邮箱时，每 10 秒静默刷新一次；
            // 请求进行中（isLoading）跳过该轮，避免与手动刷新叠加。
            // 本地 fun 的 state 读取走 rememberSaveable 委托，循环内拿到的始终是最新值。
            // key 用 s 而非 Unit：协程捕获的是首次组合的函数实例，切换语言后需重启才能用上新文案
            LaunchedEffect(s) {
                while (true) {
                    // 秒级倒数驱动收件箱的 "9s" 显示；倒数到 0 触发一轮静默刷新
                    for (t in 10 downTo 1) {
                        pollCountdownSec = t
                        delay(1000)
                    }
                    pollCountdownSec = 0
                    if (isAppInForeground && !showDisclaimer &&
                        state.currentTab == Tab.Inbox &&
                        state.email.isNotBlank() && !state.isLoading) {
                        performInboxRefresh(silent = true)
                    }
                }
            }

            LaunchedEffect(Unit) {
                if (state.autoCheckUpdate) checkUpdate()
            }

            LaunchedEffect(Unit) {
                poem = withContext(Dispatchers.IO) { fetchRandomPoemLine(client) }
            }

            // 清理上次更新遗留的安装包
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    try {
                        File(context.getExternalFilesDir(null), "updates/app-release.apk").delete()
                    } catch (_: Exception) { }
                }
            }

            TempMailTheme(
                darkTheme = darkTheme,
                themeStyle = state.themeStyle,
                // 动态配色：未启用时传 null，两套主题的配色都与定制前完全一致
                dynamicSeed = state.dynamicSeed.takeIf { it != NoDynamicSeed },
                dynamicStyle = state.dynamicStyle,
                dynamicContrast = state.dynamicContrast
            ) {
                // 两套主题共用同一份页面内容，仅外层布局与底栏形态不同
                val tabLabel: (Tab) -> String = { tab -> tabLabelOf(tab, s) }
                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局。
                // glassOverlap 为页面滚动内容末尾需预留的底栏高度，避免最后一项被底栏遮挡。
                val glassActive = state.themeStyle == ThemeStyle.HyperOS &&
                    state.barStyle == BarStyle.LiquidGlass &&
                    state.glassBlurEnabled &&
                    !showDisclaimer && isGlassBlurSupported()
                val glassOverlap = if (glassActive) GlassBarSpace else 0.dp
                // 一次性提示：首次启用液态玻璃底栏时告知"长按可拖动切换"
                LaunchedEffect(glassActive) {
                    if (glassActive && !prefs.getBoolean("glass_hint_shown", false)) {
                        prefs.edit().putBoolean("glass_hint_shown", true).apply()
                        snackbar.showSnackbar(s.glassHint)
                    }
                }
                val disclaimerPage: @Composable (Modifier) -> Unit = { pageModifier ->
                    Column(
                        pageModifier.verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(24.dp))
                        Text("免责声明", style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(16.dp))
                        Text(disclaimerText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(24.dp))
                        Text(s.genderSelect, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text("你真的选对了吗?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        val genders = listOf("male" to s.genderMale, "female" to s.genderFemale, "other" to s.genderOther)
                        genders.forEach { (value, label) ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable { selectedGender = value }
                                    .clip(MaterialTheme.shapes.small)
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedGender == value, onClick = { selectedGender = value })
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        ThemedButton(
                            onClick = {
                                if (selectedGender.isEmpty()) {
                                    scope.launch { snackbar.showSnackbar(s.genderSelect) }
                                } else if (selectedGender == "other") {
                                    prefs.edit().putBoolean("disclaimer_accepted", true).apply()
                                    showDisclaimer = false
                                } else {
                                    scope.launch { snackbar.showSnackbar(s.genderOccupied) }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.large,
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("同意并进入")
                        }
                        Spacer(Modifier.height(32.dp))
                    }
                }
                val tabPages: @Composable (PaddingValues) -> Unit = { contentPadding ->
                    AnimatedContent(
                        targetState = state.currentTab,
                        transitionSpec = {
                            val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                            (slideInHorizontally { it * direction } + fadeIn(tween(250)))
                                .togetherWith(slideOutHorizontally { it * -direction } + fadeOut(tween(150)))
                        },
                        label = "tabContent"
                    ) { tab ->
                        when (tab) {
                            Tab.Inbox -> InboxTab(contentPadding, glassOverlap, state, snackbar, scope, context, s, client, poem, darkTheme, pollCountdownSec, { performInboxRefresh(silent = false) }) { updater ->
                                state = updater(state)
                            }
                            Tab.History -> HistoryTab(contentPadding, glassOverlap, state, s) { email ->
                                // 恢复历史邮箱时需同时恢复其查询令牌（ITE 邮箱的收件箱钥匙）与过期时刻
                                val histEntry = state.history.find { it.email == email }
                                val newHistory = if (state.email.isNotBlank() && state.email != email)
                                    state.history + HistoryEmail(state.email, false, state.iteToken, state.mailboxExpiresAt)
                                else state.history
                                val filteredHistory = newHistory.filter { it.email != email }
                                state = state.copy(
                                    email = email,
                                    count = 0,
                                    rawMessages = emptyList(),
                                    items = emptyList(),
                                    history = filteredHistory,
                                    iteToken = histEntry?.token ?: "",
                                    mailboxExpiresAt = histEntry?.expiresAt ?: 0L
                                )
                            }
                            Tab.Settings -> SettingsTab(contentPadding, glassOverlap, state, s, snackbar, scope, client, settingsPageState, onCheckUpdate = { manual -> checkUpdate(manual) }) { newState ->
                                if (newState.language != state.language) {
                                    prefs.edit().putString("language", newState.language).apply()
                                }
                                if (newState.themeMode != state.themeMode) {
                                    prefs.edit().putString("themeMode", newState.themeMode.key).apply()
                                }
                                if (newState.autoCheckUpdate != state.autoCheckUpdate) {
                                    prefs.edit().putBoolean("autoCheckUpdate", newState.autoCheckUpdate).apply()
                                }
                                if (newState.themeStyle != state.themeStyle) {
                                    prefs.edit().putString("themeStyle", newState.themeStyle.key).apply()
                                }
                                if (newState.barStyle != state.barStyle) {
                                    prefs.edit().putString("barStyle", newState.barStyle.key).apply()
                                }
                                if (newState.glassBlurEnabled != state.glassBlurEnabled) {
                                    prefs.edit().putBoolean("glassBlurEnabled", newState.glassBlurEnabled).apply()
                                }
                                if (newState.dynamicSeed != state.dynamicSeed) {
                                    prefs.edit().putInt("dynamicSeed", newState.dynamicSeed).apply()
                                }
                                if (newState.dynamicStyle != state.dynamicStyle) {
                                    prefs.edit().putString("dynamicStyle", newState.dynamicStyle.key).apply()
                                }
                                if (newState.dynamicContrast != state.dynamicContrast) {
                                    prefs.edit().putFloat("dynamicContrast", newState.dynamicContrast).apply()
                                }
                                if (newState.mailProvider != state.mailProvider) {
                                    prefs.edit().putString("mailProvider", newState.mailProvider.key).apply()
                                }
                                state = newState
                            }
                        }
                    }
                }

                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局
                // 外层 Box：让"主题变更过渡层"成为 GlassShell 的兄弟节点，才能盖住包括底栏在内的整屏
                Box(Modifier.fillMaxSize()) {
                    GlassShell(
                        glass = glassActive,
                        darkTheme = darkTheme,
                        items = Tab.entries.map { GlassBarItem(it.icon, tabLabel(it)) },
                        selectedIndex = Tab.entries.indexOf(state.currentTab),
                        onSelect = { state = state.copy(currentTab = Tab.entries[it]) },
                        snackbarHost = { SnackbarHost(snackbar) },
                        fallbackBar = {
                            if (!showDisclaimer) {
                                if (state.themeStyle == ThemeStyle.Material3) {
                                    // Material3：原始默认样式（与底栏定制前完全一致）
                                    NavigationBar {
                                        Tab.entries.forEach { tab ->
                                            NavigationBarItem(
                                                selected = state.currentTab == tab,
                                                onClick = { state = state.copy(currentTab = tab) },
                                                icon = { Icon(tab.icon, tabLabel(tab)) },
                                                label = { Text(tabLabel(tab)) }
                                            )
                                        }
                                    }
                                } else {
                                    // Miuix：底栏形态可选（Material3 主题不提供，保持原样）。
                                    // 用 Crossfade 包一层：切换形态时淡入淡出过渡，避免生硬跳变
                                    Crossfade(
                                        targetState = state.barStyle,
                                        animationSpec = tween(THEME_ANIM_MS),
                                        label = "barStyle"
                                    ) { barStyle ->
                                        when (barStyle) {
                                            BarStyle.LiquidGlass -> LiquidGlassBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                            BarStyle.Float -> MiuixFloatingBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                            BarStyle.Edge -> MiuixEdgeBottomBar(
                                                current = state.currentTab,
                                                label = tabLabel,
                                                onSelect = { state = state.copy(currentTab = it) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { p ->
                        if (showDisclaimer) {
                            disclaimerPage(
                                Modifier.fillMaxSize().statusBarsPadding().padding(p).padding(horizontal = 24.dp)
                            )
                        } else {
                            tabPages(p)
                        }
                    }
                    // 配色过渡由主题层的逐 token 动画负责（见 TempMailTheme / animateColorScheme），
                    // 这里不再需要"旧底色遮罩"——那种做法会在切换后先闪一帧新配色再盖上遮罩
                }
                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = { showUpdateDialog = false },
                        title = { Text("${s.newVer}: v$updateTag") },
                        text = {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                if (updateBody.isNotBlank()) {
                                    Text(updateBody, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        },
                        confirmButton = {
                            ThemedTextButton(onClick = {
                                showUpdateDialog = false
                                downloadInstall(updateUrl, updateSha)
                            }) { Text(s.updateNow) }
                        },
                        dismissButton = {
                            ThemedTextButton(onClick = { showUpdateDialog = false }) { Text(s.updateLater) }
                        }
                    )
                }
                if (showDownloadProgress) {
                    AlertDialog(
                        onDismissRequest = {
                            downloadCall?.cancel()
                            showDownloadProgress = false
                        },
                        title = { Text(s.updating) },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                ThemedLinearProgress(progress = { downloadProgress / 100f }, modifier = Modifier.fillMaxWidth())
                                Spacer(Modifier.height(8.dp))
                                Text("${downloadProgress}%")
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            ThemedTextButton(onClick = {
                                downloadCall?.cancel()
                                showDownloadProgress = false
                            }) { Text(s.cancel) }
                        }
                    )
                }
            }
        }
    }
}

// ==================== Miuix 主题底栏（三种形态可选） ====================

/**
 * Miuix 悬浮底栏：既有样式，视觉与行为保持与定制前一致。
 */
@Composable
private fun MiuixFloatingBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    // 指示器必须用不透明色：M3 绘制时以 .copy(alpha = animationProgress)
    // 覆盖该色的 alpha（选中稳定后为 1f），传入带透明度的颜色会被静默还原成实心色。
    // 故按 12% 比例预先合成到容器色上，亮/暗模式均自动匹配底色。
    // 容器色走主题桥接：开启莫奈后取带色调的 Miuix 容器色，不再是固定白色
    val barColor = themedBarContainerColor()
    val indicatorColor = MaterialTheme.colorScheme.primary
        .copy(alpha = 0.12f)
        .compositeOver(barColor)
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp)
            .padding(bottom = 12.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(28.dp),
                clip = true
            ),
        containerColor = barColor,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0.dp),
        content = {
            Tab.entries.forEach { tab ->
                val selected = current == tab
                NavigationBarItem(
                    selected = selected,
                    onClick = { onSelect(tab) },
                    icon = {
                        Icon(
                            tab.icon,
                            label(tab),
                            tint = if (selected) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Text(
                            label(tab),
                            color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = indicatorColor
                    )
                )
            }
        }
    )
}

/**
 * 贴边底栏：悬浮关闭时的形态（对应参考图7）。
 * 与悬浮底栏同一套色板与选中态规则，但**不浮起**——铺满整宽、不留横向边距、不投影，
 * 靠顶部一条细分隔线与内容分层，底栏背景一直延伸到屏幕底边。
 */
@Composable
private fun MiuixEdgeBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    val barColor = themedBarContainerColor()
    Column(
        Modifier
            .fillMaxWidth()
            .background(barColor)
    ) {
        ThemedDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Tab.entries.forEach { tab ->
                val selected = current == tab
                val contentColor = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onSelect(tab) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                ) {
                    Icon(tab.icon, label(tab), tint = contentColor,
                        modifier = Modifier.size(24.dp))
                    Text(label(tab), style = MaterialTheme.typography.labelSmall,
                        color = contentColor)
                }
            }
        }
    }
}

/**
 * 液态玻璃底栏（伪玻璃回退实现，用于 API 33 以下或模糊不可用的设备）：
 * 视觉与交互规格参考 skill-liquid-glass
 * （玻璃本体 + 高光描边 + 外层柔和阴影 + 滑动指示器 + 按压缩放回弹）。
 *
 * 注：真实模糊/折射由 ui/glass/GlassBottomBar.kt 在 API 33+ 提供；
 * 本组件仅以渐变 + 描边 + 阴影模拟玻璃质感，保证低版本观感一致性与稳定性。
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun LiquidGlassBottomBar(
    current: Tab,
    label: (Tab) -> String,
    onSelect: (Tab) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val tabs = Tab.entries
    val glassShape = RoundedCornerShape(28.dp)
    // 玻璃层次：不透明基底之上叠一层极淡的纵向明暗（顶部受光、底部压暗），形成厚度。
    // 基底必须不透明 —— 否则 10dp 阴影会从半透明本体下方透出，把底栏越往底部压得越灰。
    val bodyBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = 0.10f),
            Color.Transparent,
            Color.Black.copy(alpha = 0.05f)
        )
    )
    // 高光描边：左上来光最亮，过渡到极淡的主色收边
    val edgeBrush = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = 0.70f),
            Color.White.copy(alpha = 0.10f),
            scheme.primary.copy(alpha = 0.22f)
        )
    )
    // 不透明基底色：走主题桥接，开启莫奈后为带色调的 Miuix 容器色（未开启时与原来的 surface 同值）
    val baseColor = themedBarContainerColor()
    // 指示器由本组件自行绘制（不经过 M3 NavigationBar），故可直接使用半透明色，
    // 预合成到基底色上以保证在深浅两种背景下都有足够存在感
    val indicatorColor = scheme.primary.copy(alpha = 0.16f)
        .compositeOver(baseColor)
    Box(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp)
    ) {
        // 阴影层：不透明底色承载投影，使阴影只出现在圆角轮廓之外
        Box(
            Modifier
                .matchParentSize()
                .shadow(elevation = 10.dp, shape = glassShape, clip = true)
                .background(baseColor, glassShape)
        )
        BoxWithConstraints(
            Modifier
                .matchParentSize()
                .background(bodyBrush, glassShape)
                .border(1.dp, edgeBrush, glassShape)
        ) {
            val itemWidth = maxWidth / tabs.size
            val indicatorWidth = minOf(64.dp, itemWidth - 8.dp)
            val index = tabs.indexOf(current).coerceAtLeast(0)
            val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
            val slot = if (rtl) tabs.size - 1 - index else index
            val indicatorX by animateDpAsState(
                targetValue = itemWidth * slot + (itemWidth - indicatorWidth) / 2,
                animationSpec = spring(dampingRatio = 0.78f, stiffness = Spring.StiffnessMediumLow),
                label = "glassIndicatorX"
            )
            // 滑动指示器：绘制在图标之下
            Box(
                Modifier
                    .offset(x = indicatorX, y = 8.dp)
                    .size(indicatorWidth, 32.dp)
                    .background(indicatorColor, RoundedCornerShape(percent = 50))
            )
            // 顶部受光已由 bodyBrush 的渐变承担，此处不再叠加内高光
            Row(
                Modifier.fillMaxSize().selectableGroup(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    GlassTabItem(
                        modifier = Modifier.weight(1f),
                        tab = tab,
                        selected = current == tab,
                        label = label(tab),
                        onClick = { onSelect(tab) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassTabItem(
    modifier: Modifier,
    tab: Tab,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // 按压回弹：按下收缩、松开弹回，模拟玻璃被按压的液体反馈
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = 0.42f, stiffness = Spring.StiffnessMedium),
        label = "glassPressScale"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            tab.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (selected) scheme.primary else scheme.onSurfaceVariant
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) scheme.primary else scheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InboxTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    context: Context,
    s: Strings,
    client: OkHttpClient,
    poem: PoemLine?,
    // 当前明暗主题：邮件正文 WebView 的深色适配需要（算法暗化/forceDark）
    darkTheme: Boolean,
    // 自动轮询倒计时（秒，0=正在刷新或不显示）：让用户直观看到下次刷新时机
    pollCountdownSec: Int,
    onManualRefresh: () -> Unit,
    onState: ((AppState) -> AppState) -> Unit
) {
    var showBodyDialog by remember { mutableStateOf(false) }
    var dialogBody by remember { mutableStateOf("") }
    var dialogHtml by remember { mutableStateOf("") }
    var showPoem by remember { mutableStateOf(false) }

    // 倒计时时钟：仅已知过期时刻时每秒跳动（无过期信息则不空转）
    var nowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.mailboxExpiresAt) {
        if (state.mailboxExpiresAt <= 0L) return@LaunchedEffect
        while (true) {
            nowMs = System.currentTimeMillis()
            delay(1000)
        }
    }

    LaunchedEffect(poem != null) {
        if (poem != null) showPoem = true
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp)
            .statusBarsPadding().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showPoem) Spacer(Modifier.height(24.dp))
        AnimatedVisibility(
            visible = showPoem,
            enter = fadeIn(tween(600)) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = tween(600)
            )
        ) {
            poem?.let { pm ->
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(12.dp))
                    Text(pm.line,
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("——《${pm.title}》${if (pm.author.isNotBlank()) " ${pm.author}" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        Icon(Icons.Default.Email, contentDescription = s.title, Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Text(s.title, style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(28.dp))

        ThemedButton(
            onClick = {
                onState { it.copy(isLoading = true) }
                scope.launch(Dispatchers.IO) {
                    try {
                        if (state.mailProvider == MailProvider.InstantTempEmail) {
                            // instanttempemail.com：POST /api/create → {address, expires, token}
                            // 邮箱 7 天有效；token 是收件箱查询钥匙，必须随邮箱一起保存
                            val r = Request.Builder()
                                .url("https://instanttempemail.com/api/create")
                                .post("".toRequestBody(null))
                                .build()
                            val body = client.newCall(r).execute().body?.string() ?: ""
                            val j = JSONObject(body)
                            val newEmail = j.optString("address", "")
                            val newToken = j.optString("token", "")
                            if (newEmail.isNotBlank() && newToken.isNotBlank()) {
                                // expires 为 ISO8601 UTC（含微秒），parseEmailTime 已兼容该格式
                                val newExpiresAt = parseEmailTime(j.optString("expires", ""))
                                withContext(Dispatchers.Main) {
                                    // 基于写入时的最新状态合并历史，避免覆盖请求期间的其他状态变更
                                    onState { cur ->
                                        val newHistory = if (cur.email.isNotBlank())
                                            cur.history + HistoryEmail(cur.email, false, cur.iteToken, cur.mailboxExpiresAt)
                                        else cur.history
                                        cur.copy(
                                            email = newEmail, count = 0,
                                            rawMessages = emptyList(), items = emptyList(),
                                            history = newHistory, isLoading = false,
                                            iteToken = newToken,
                                            mailboxExpiresAt = newExpiresAt
                                        )
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onState { it.copy(isLoading = false) }
                                    scope.launch { snackbar.showSnackbar(s.fetchFailed) }
                                }
                            }
                        } else {
                            val r = Request.Builder()
                                .url("https://api.pearapi.ai/api/email/?type=get")
                                .get().build()
                            val body = client.newCall(r).execute().body?.string() ?: ""
                            val j = JSONObject(body)
                            if (j.optString("code") == "200") {
                                val newEmail = j.optString("email", "")
                                // PearAPI 只返回时长（"time":"10 minutes"），无绝对时间戳，
                                // 以生成时刻+10 分钟作为过期时刻（与实测行为一致）
                                val newExpiresAt = System.currentTimeMillis() + 10 * 60_000L
                                withContext(Dispatchers.Main) {
                                    // 基于写入时的最新状态合并历史，避免覆盖请求期间的其他状态变更
                                    onState { cur ->
                                        val newHistory = if (cur.email.isNotBlank())
                                            cur.history + HistoryEmail(cur.email, false, cur.iteToken, cur.mailboxExpiresAt)
                                        else cur.history
                                        cur.copy(
                                            email = newEmail, count = 0,
                                            rawMessages = emptyList(), items = emptyList(),
                                            history = newHistory, isLoading = false,
                                            iteToken = "",
                                            mailboxExpiresAt = newExpiresAt
                                        )
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    onState { it.copy(isLoading = false) }
                                    scope.launch { snackbar.showSnackbar(j.optString("msg", s.fetchFailed)) }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            onState { it.copy(isLoading = false) }
                            scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
                    }
                }
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = themedCornerShape(14.dp, 20.dp)
        ) { Text(if (state.isLoading) s.generating else s.generate) }

        if (state.email.isNotBlank()) {
            Spacer(Modifier.height(20.dp))
            // 自动轮询倒计时胶囊：从下方邮箱卡片的「复制/刷新」行移出，独立放在生成按钮与
            // 邮箱卡片之间的空白区（页面中轴居中，wrap 内容宽度适配小屏）；倒数到 0 触发一次
            // 静默刷新，轮询逻辑不变。轮询周期内 0 态不足一帧即被重置，故保持条件渲染不加动效
            if (pollCountdownSec > 0) {
                ThemedPollCountdown(seconds = pollCountdownSec, label = s.autoRefresh)
                Spacer(Modifier.height(16.dp))
            }
            ThemedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(20.dp)) {
                    Text(s.yourEmail, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("${s.receivedCount}: ${state.items.size}", style = MaterialTheme.typography.labelSmall)
                    // 邮箱过期倒计时：只在已知过期时刻（>0）时显示，统一为 "...后过期" 文案
                    if (state.mailboxExpiresAt > 0L) {
                        val remaining = state.mailboxExpiresAt - nowMs
                        val danger = remaining in 1..5 * 60_000L
                        Text(
                            if (remaining <= 0L) s.mailboxExpired
                            else String.format(s.expiresInFmt, formatDurationWords(remaining)),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (remaining <= 0L || danger) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.email,
                            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f))
                        ThemedTextButton(onClick = {
                            (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                                .setPrimaryClip(ClipData.newPlainText("email", state.email))
                            scope.launch { snackbar.showSnackbar(s.copied) }
                        }) { Text(s.copy) }
                        FilledTonalButton(
                            onClick = onManualRefresh,
                            shape = MaterialTheme.shapes.medium
                        ) { Text(s.refresh) }
                    }
                }
            }
        }

        if (state.items.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(s.inbox, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            val sorted = remember(state.items) {
                state.items.sortedWith(
                    compareByDescending<EmailItem> { it.timestamp }.thenByDescending { it.time }
                )
            }
            sorted.forEach { item ->
                ThemedCard(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .clickable {
                            dialogBody = if (item.body.isNotBlank()) item.body
                                else if (item.htmlBody.isNotBlank()) stripHtml(item.htmlBody)
                                else ""
                            dialogHtml = item.htmlBody
                            showBodyDialog = true
                        },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(item.from, style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(item.subject, style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.height(4.dp))
                        Text(item.time, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.body.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            ThemedDivider()
                            Spacer(Modifier.height(8.dp))
                            Text(item.body,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        if (state.rawMessages.isNotEmpty() && state.items.isEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(s.rawData, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            state.rawMessages.reversed().forEach { raw ->
                ThemedCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(raw, Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        // 玻璃底栏：内容可滚到浮起的底栏下方，末尾预留底栏高度使最后一项仍可完整滚出
        // 必须在滚动 Column 内部才有效；放在 Column 外只是父布局里的游离元素，最后一项会被底栏遮住
        if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
    }

    if (showBodyDialog) {
        val code = extractVerificationCode(dialogBody)
        AlertDialog(
            onDismissRequest = { showBodyDialog = false },
            title = { Text(s.inbox) },
            text = {
                Column {
                    // 对话框关闭时销毁 WebView，防止每打开一封邮件就泄漏一个原生实例
                    val webViewHolder = remember { arrayOfNulls<WebView>(1) }
                    DisposableEffect(Unit) {
                        onDispose {
                            webViewHolder[0]?.let { wv ->
                                wv.stopLoading()
                                wv.loadUrl("about:blank")
                                wv.destroy()
                            }
                            webViewHolder[0] = null
                        }
                    }
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = false
                                // 直接加载远程图片（产品决策）：验证码邮件的 logo/图形验证码
                                // 需要图片才能识别，优先可用性
                                settings.loadsImagesAutomatically = true
                                // 深色适配：亮色 HTML 在深色主题下会有白底黑字的割裂感。
                                // API 33+ 用算法暗化（系统 WebView 官方替代方案）；
                                // 低版本退回 forceDark（targetSdk 33+ 时仅在 33 以下设备生效，恰好互补）
                                if (darkTheme) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        settings.isAlgorithmicDarkeningAllowed = true
                                    } else {
                                        @Suppress("DEPRECATION")
                                        settings.forceDark = WebSettings.FORCE_DARK_ON
                                    }
                                }
                                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                webChromeClient = object : WebChromeClient() {
                                    override fun onCreateWindow(
                                        view: WebView?,
                                        isDialog: Boolean,
                                        isUserGesture: Boolean,
                                        resultMsg: Message?
                                    ): Boolean {
                                        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false
                                        val newWebView = WebView(view?.context ?: ctx).apply {
                                            settings.javaScriptEnabled = true
                                            settings.allowFileAccess = false
                                            webViewClient = object : WebViewClient() {
                                                override fun shouldOverrideUrlLoading(
                                                    v: WebView?,
                                                    r: WebResourceRequest?
                                                ): Boolean {
                                                    openMailLink(v, r?.url)
                                                    v?.destroy()
                                                    return true
                                                }

                                                @Deprecated("Deprecated in Java")
                                                override fun shouldOverrideUrlLoading(v: WebView?, url: String?): Boolean {
                                                    openMailLink(v, url?.let { Uri.parse(it) })
                                                    v?.destroy()
                                                    return true
                                                }
                                            }
                                        }
                                        transport.webView = newWebView
                                        resultMsg.sendToTarget()
                                        return true
                                    }
                                }
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        Log.d("MAIL_LINK", "override request: ${request?.url}")
                                        openMailLink(view, request?.url)
                                        return true
                                    }

                                    @Deprecated("Deprecated in Java")
                                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                                        Log.d("MAIL_LINK", "override url: $url")
                                        openMailLink(view, url?.let { Uri.parse(it) })
                                        return true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        try {
                                            view?.evaluateJavascript(mailLinkJs, null)
                                        } catch (_: Exception) {
                                        }
                                    }
                                }
                                if (dialogHtml.isNotBlank()) {
                                    loadDataWithBaseURL("https://example.com", dialogHtml, "text/html", "UTF-8", null)
                                } else if (looksLikeHtml(dialogBody)) {
                                    loadDataWithBaseURL("https://example.com", dialogBody, "text/html", "UTF-8", null)
                                } else {
                                    // 纯文本不再用 text/plain 加载（WebView 默认黑字，深色主题下不可读），
                                    // 包一层带主题色 <pre> 的 HTML，明暗主题下都可读
                                    val textColor = if (darkTheme) "#E6E1E5" else "#1C1B1F"
                                    val esc = Html.escapeHtml(dialogBody)
                                    loadDataWithBaseURL("https://example.com",
                                        "<html><body style=\"margin:0;padding:4px\">" +
                                            "<pre style=\"white-space:pre-wrap;word-wrap:break-word;" +
                                            "font-family:monospace;font-size:14px;color:$textColor\">$esc</pre>" +
                                            "</body></html>",
                                        "text/html", "UTF-8", null)
                                }
                            }.also { webViewHolder[0] = it }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 200.dp, max = 400.dp)
                    )
                }
            },
            confirmButton = {
                ThemedTextButton(onClick = { showBodyDialog = false }) { Text(s.close) }
            },
            dismissButton = code?.let { c ->
                {
                    ThemedTextButton(onClick = {
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                            .setPrimaryClip(ClipData.newPlainText("verification_code", c))
                        scope.launch { snackbar.showSnackbar("${s.codeCopied}: $c") }
                    }) { Text(s.copyCode) }
                }
            }
        )
    }
}

@Composable
private fun HistoryTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    s: Strings,
    onUseEmail: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(24.dp))
        Text(s.historyTitle, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        val allEmails = remember(state.email, state.iteToken, state.history) {
            val list = mutableListOf<HistoryEmail>()
            if (state.email.isNotBlank()) list.add(HistoryEmail(state.email, true, state.iteToken, state.mailboxExpiresAt))
            list.addAll(state.history.reversed())
            list
        }

        if (allEmails.isEmpty()) {
            Text(s.noNewMail, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            allEmails.forEach { h ->
                ThemedCard(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        .then(if (!h.isActive) Modifier.clickable { onUseEmail(h.email) } else Modifier),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(h.email,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.weight(1f))
                            if (h.isActive) {
                                Surface(
                                    shape = themedCornerShape(6.dp, 12.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        s.active,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            } else {
                                Surface(
                                    shape = themedCornerShape(6.dp, 12.dp),
                                    color = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        s.expired,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(s.useNow,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
        // 玻璃底栏：内容可滚到浮起的底栏下方，末尾预留底栏高度使最后一项仍可完整显示
        if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
    }
}

private enum class SettingsPage { Main, Language, DarkMode, Theme, Monet, ThemeSettings, About, Author, MailProvider }

/** 用序号持久化子页（配置变更 / 进程重建后回到原页面，越界时回退设置主页）。 */
private val SettingsPageSaver: Saver<SettingsPage, Int> = Saver(
    save = { it.ordinal },
    restore = { SettingsPage.entries.getOrElse(it) { SettingsPage.Main } }
)

/** 明暗三态的显示文案（设置主页的值、深色模式子页、主题设置页的分段控件共用）。 */
private fun themeModeLabel(mode: ThemeMode, s: Strings): String = when (mode) {
    ThemeMode.System -> s.themeFollowSystem
    ThemeMode.Light -> s.themeLight
    ThemeMode.Dark -> s.themeDark
}

/** 邮箱服务健康检测结果。testing=true 表示探测进行中；ok 三态：null=未检测。 */
private data class ServiceStatus(val testing: Boolean = false, val ok: Boolean? = null, val latencyMs: Long = 0)

/**
 * 邮箱服务健康探测：向服务的只读端点发一次 GET，返回（是否在线, 耗时毫秒）。
 * - PearAPI：用 receive 查询一个不存在的邮箱（只读，不会分配新邮箱），预期 200；
 * - ITE：查询不存在的 token，预期 404（"邮箱不存在"恰说明 API 本身在正常应答）。
 * 2xx~4xx 均视为服务在线；仅 5xx 与网络异常（超时/DNS 失败）判为不可用。
 */
private suspend fun checkMailServiceHealth(client: OkHttpClient, provider: MailProvider): Pair<Boolean, Long> =
    withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val url = when (provider) {
                MailProvider.PearApi -> "https://api.pearapi.ai/api/email/".toHttpUrl().newBuilder()
                    .addQueryParameter("type", "receive")
                    .addQueryParameter("email", "healthcheck@healthcheck.invalid")
                    .build()
                MailProvider.InstantTempEmail ->
                    "https://instanttempemail.com/api/inbox/00000000-0000-0000-0000-000000000000".toHttpUrl()
            }
            client.newCall(Request.Builder().url(url).get().build()).execute().use { resp ->
                Pair(resp.code in 200..499, System.currentTimeMillis() - start)
            }
        } catch (_: Exception) {
            Pair(false, System.currentTimeMillis() - start)
        }
    }

@Composable
private fun SettingsTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    client: OkHttpClient,
    pageState: MutableState<SettingsPage>,
    onCheckUpdate: (Boolean) -> Unit,
    onState: (AppState) -> Unit
) {
    // 子页状态由根布局持有（见 setContent 中的说明）：底栏布局分支切换时不会被重置
    var page by pageState
    val ctx = LocalContext.current
    BackHandler(page != SettingsPage.Main) { page = SettingsPage.Main }

    Column(Modifier.fillMaxSize().padding(p).padding(horizontal = 24.dp).statusBarsPadding()) {
        Box(Modifier.weight(1f), propagateMinConstraints = true) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(24.dp))

                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        val direction = if (forward) 1 else -1
                        (slideInHorizontally { it * direction } + fadeIn(tween(250)))
                            .togetherWith(slideOutHorizontally { it * -direction } + fadeOut(tween(150)))
                    },
                    label = "settingsPage"
                ) { currentPage ->
                    // AnimatedContent 的内容作用域不会垂直堆叠同级元素：包一层 Column，
                    // 否则页面内"返回按钮 + 标题 + 卡片"会互相覆盖（标题与返回按钮被卡片盖住）
                    Column {
                    when (currentPage) {
                        SettingsPage.Main -> {
                            Text(s.settings, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(24.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    SettingsItem(
                                        label = s.languageLabel,
                                        value = allLanguages.find { it.code == state.language }?.label ?: "中文",
                                        onClick = { page = SettingsPage.Language }
                                    )
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.mailService,
                                        value = if (state.mailProvider == MailProvider.InstantTempEmail) "instanttempemail.com" else "PearAPI",
                                        onClick = { page = SettingsPage.MailProvider }
                                    )
                                    // 明暗模式与底栏选项：HyperOS 主题下合并进「主题设置」，
                                    // Material3 主题下沿用原有的独立入口，两者不重复出现
                                    if (state.themeStyle == ThemeStyle.HyperOS) {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.themeSettings,
                                            value = themeModeLabel(state.themeMode, s),
                                            onClick = { page = SettingsPage.ThemeSettings }
                                        )
                                    } else {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.darkMode,
                                            value = themeModeLabel(state.themeMode, s),
                                            onClick = { page = SettingsPage.DarkMode }
                                        )
                                    }
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.themeStyle,
                                        value = if (state.themeStyle == ThemeStyle.HyperOS) s.themeHyperOS else s.themeDefault,
                                        onClick = { page = SettingsPage.Theme }
                                    )
                                    // 动态配色入口只在 Material3 主题下保留：
                                    // HyperOS 主题的取色入口在「主题设置」页内，避免两处重复
                                    if (state.themeStyle == ThemeStyle.Material3) {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.monet,
                                            value = if (state.dynamicSeed == NoDynamicSeed) "OFF"
                                            else "#%06X".format(state.dynamicSeed and 0xFFFFFF),
                                            onClick = { page = SettingsPage.Monet }
                                        )
                                    }
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                label = s.about,
                                onClick = { page = SettingsPage.About }
                            )
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                label = s.checkUpdate,
                                onClick = { onCheckUpdate(true) }
                            )
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            Row(
                                Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(s.autoCheckUpdate, style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f))
                                ThemedSwitch(
                                    checked = state.autoCheckUpdate,
                                    onCheckedChange = { onState(state.copy(autoCheckUpdate = it)) }
                                )
                            }
                            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            SettingsItem(
                                label = s.author,
                                onClick = { page = SettingsPage.Author }
                            )
                                }
                            }
                        }

                        SettingsPage.Language -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.langSelect, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    allLanguages.forEachIndexed { i, lang ->
                                        if (i > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(lang.label, state.language == lang.code,
                                            onClick = { onState(state.copy(language = lang.code)); page = SettingsPage.Main })
                                    }
                                }
                            }
                        }

                        SettingsPage.MailProvider -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.mailService, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(12.dp))
                            Text(s.mailServiceDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(20.dp))

                            // 服务状态检测：进入本页自动探测一次，之后可随时手动重测。
                            // 状态用 remember 持有——离开子页即释放，回来重新探测，天然拿到最新状态
                            val statuses = remember { mutableStateMapOf<MailProvider, ServiceStatus>() }
                            fun runCheck() {
                                MailProvider.entries.forEach { statuses[it] = ServiceStatus(testing = true) }
                                // 两个服务并行探测，各自完成后独立更新自己的行
                                MailProvider.entries.forEach { p ->
                                    scope.launch {
                                        val (ok, ms) = checkMailServiceHealth(client, p)
                                        statuses[p] = ServiceStatus(ok = ok, latencyMs = ms)
                                    }
                                }
                            }
                            LaunchedEffect(Unit) { runCheck() }
                            val checking = statuses.values.any { it.testing }

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(s.serviceStatus, style = MaterialTheme.typography.titleSmall)
                                        Spacer(Modifier.weight(1f))
                                        ThemedTextButton(onClick = { runCheck() }, enabled = !checking) {
                                            Text(s.testNow)
                                        }
                                    }
                                    MailProvider.entries.forEachIndexed { i, p ->
                                        if (i > 0) ThemedDivider()
                                        val st = statuses[p]
                                        // 状态点：灰=检测中/未检测，绿=正常，红=不可用
                                        val dotColor = when {
                                            st == null || st.testing -> MaterialTheme.colorScheme.onSurfaceVariant
                                            st.ok == true -> Color(0xFF34C759)
                                            else -> Color(0xFFFF3B30)
                                        }
                                        val statusText = when {
                                            st == null || st.testing -> s.statusTesting
                                            st.ok == true -> "${s.statusOk} · ${st.latencyMs} ms"
                                            else -> s.statusDown
                                        }
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                if (p == MailProvider.InstantTempEmail) "instanttempemail.com" else "PearAPI",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Text(statusText,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = if (st?.ok == false) MaterialTheme.colorScheme.error
                                                else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    MailProvider.entries.forEachIndexed { i, p ->
                                        if (i > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(
                                            if (p == MailProvider.InstantTempEmail) "instanttempemail.com" else "PearAPI",
                                            state.mailProvider == p,
                                            onClick = { onState(state.copy(mailProvider = p)); page = SettingsPage.Main }
                                        )
                                    }
                                }
                            }
                        }

                        SettingsPage.DarkMode -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.darkModeSetting, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            // 三态：跟随系统 / 浅色 / 深色（与 Miuix 主题设置页保持一致）
                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    ThemeMode.entries.forEachIndexed { index, mode ->
                                        if (index > 0) ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        LanguageOption(themeModeLabel(mode, s), state.themeMode == mode) {
                                            onState(state.copy(themeMode = mode))
                                        }
                                    }
                                }
                            }
                        }

                        SettingsPage.Theme -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.themeStyle, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    LanguageOption(s.themeDefault, state.themeStyle == ThemeStyle.Material3,
                                        onClick = { onState(state.copy(themeStyle = ThemeStyle.Material3)); page = SettingsPage.Main })
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    LanguageOption(s.themeHyperOS, state.themeStyle == ThemeStyle.HyperOS,
                                        onClick = { onState(state.copy(themeStyle = ThemeStyle.HyperOS)); page = SettingsPage.Main })
                                }
                            }
                        }

                        SettingsPage.ThemeSettings -> ThemeSettingsPage(
                            state = state,
                            s = s,
                            snackbar = snackbar,
                            scope = scope,
                            onState = onState,
                            onBack = { page = SettingsPage.Main }
                        )

                        SettingsPage.Monet -> MonetPage(
                            state = state,
                            s = s,
                            snackbar = snackbar,
                            scope = scope,
                            onState = onState,
                            onBack = { page = SettingsPage.Main }
                        )

                        SettingsPage.About -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.about, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))
                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column(Modifier.padding(20.dp)) {
                                    Text(s.aboutDesc, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                SettingsPage.Author -> {
                    ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(s.author, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(20.dp))
                    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                        Column(Modifier.padding(20.dp)) {
                            Text("GitHub", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text(s.authorHomepage,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.Underline,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.clickable {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/wzhdgithub")))
                                })
                            Spacer(Modifier.height(4.dp))
                            Text(s.projectRepo,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.Underline,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.clickable {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/wzhdgithub/tempmail")))
                                })
                            Spacer(Modifier.height(4.dp))
                            Text("Blog",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    textDecoration = TextDecoration.Underline,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.clickable {
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://wzhblog6.pwapi.cn/")))
                                })
                        }
                    }
                }
                    }
                    }
                }
                // 玻璃底栏：滚动内容末尾预留底栏高度，使其可完整滚出（底栏浮在其上）。
                // 必须在滚动 Column 内部才有效：放在 Column 外只是 Box 里的游离元素，
                // 最后一项会被底栏遮住、点不到（与 InboxTab 曾经的错位相同）
                if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
            }
        }
        if (page == SettingsPage.Main) {
            Text("${s.version} ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp + bottomOverlap))
        }
    }
}

/**
 * Miuix 风格「主题设置」子页（排版参照参考图）：
 *   居中标题 + 手机预览示意图 → 明暗三选一 → 卡片1（莫奈开关 / 强调色）→ 卡片2（底栏选项）。
 *
 * 仅在 HyperOS 主题下可达（入口在设置主页），页面元素全部经 ui/theme/MiuixComponents.kt 的
 * Themed* 桥接，HyperOS 下渲染为真正的 Miuix 组件；配色与 Material3 版莫奈页共用同一份
 * dynamicSeed / dynamicStyle，因此两套主题的取色结果始终一致。
 *
 * 生效方式：**实时生效**。本页任何改动都立刻写回全局状态，预览区域与当前界面（含底栏）
 * 同步更新，改完不需要返回主界面即可看到效果；页面本身留在原地，方便连续调整。
 * 预览区域与真实界面同源取色（themedSurfaceColors），并用动画平滑过渡（颜色 / 尺寸 / 底栏形态）。
 */
@Composable
private fun ThemeSettingsPage(
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    onState: (AppState) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var extracting by remember { mutableStateOf(false) }
    var accentMenuOpen by remember { mutableStateOf(false) }

    val monetOn = state.dynamicSeed != NoDynamicSeed
    val floatingOn = state.barStyle != BarStyle.Edge

    // 相册取色：与 Material3 版莫奈页共用同一条链路（SeedExtractor 内部切 IO 线程）
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            extracting = true
            val seed = extractSeedFromUri(ctx, uri)
            extracting = false
            if (seed != null) onState(state.copy(dynamicSeed = seed))
            else snackbar.showSnackbar(s.monetFailed)
        }
    }

    // 标题栏：左返回 + 居中大标题（改动已实时生效，返回只是离开本页）
    Box(Modifier.fillMaxWidth()) {
        ThemedIconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
        }
        Text(s.themeSettings, style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center))
    }
    Spacer(Modifier.height(20.dp))

    // 预览区域：与真实界面同源取色，实时反映明暗 / 种子色 / 底栏形态
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        ThemePreviewMock(
            barStyle = state.barStyle,
            label = { tab -> tabLabelOf(tab, s) }
        )
    }
    Spacer(Modifier.height(28.dp))

    // 明暗三选一：跟随系统 / 浅色 / 深色
    ThemedSegmentedTabs(
        tabs = ThemeMode.entries.map { themeModeLabel(it, s) },
        selectedIndex = ThemeMode.entries.indexOf(state.themeMode),
        onSelect = { onState(state.copy(themeMode = ThemeMode.entries[it])) }
    )
    Spacer(Modifier.height(16.dp))

    // 卡片1：莫奈取色
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            ThemedListRow(
                title = s.monetEnable,
                icon = { ImageFrameIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = monetOn,
                        onCheckedChange = { on ->
                            // 开启时先落到「默认」强调色（应用主色蓝），随后可再选预设或图片
                            onState(state.copy(dynamicSeed = if (on) DefaultMonetSeed else NoDynamicSeed))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.monetAccent,
                icon = { Icon(Icons.Default.Edit, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp)) },
                onClick = { accentMenuOpen = true },
                trailing = {
                    ThemedDropdownValue(
                        text = if (monetOn && state.dynamicSeed != DefaultMonetSeed)
                            s.monetAccentCustom else s.monetAccentDefault,
                        onClick = { accentMenuOpen = true }
                    )
                }
            )
            // 零高度锚点：紧贴该行下方，弹出菜单以它为基准向下展开
            Box(Modifier.fillMaxWidth().height(0.dp)) {
                AccentDropdown(
                    expanded = accentMenuOpen,
                    selectedSeed = state.dynamicSeed,
                    extracting = extracting,
                    s = s,
                    onDismiss = { accentMenuOpen = false },
                    onPick = { seed -> onState(state.copy(dynamicSeed = seed)) },
                    onPickImage = {
                        accentMenuOpen = false
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // 卡片2：底栏相关
    // 状态机（BarStyle，三项枚举让"无效组合"从类型上就不存在）：
    //   悬浮关 → 贴边底栏（图7 的贴边样式，液态玻璃不可用）
    //   悬浮开 + 液态玻璃关 → 普通悬浮底栏
    //   悬浮开 + 液态玻璃开 → 悬浮液态玻璃底栏
    ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column {
            ThemedListRow(
                title = s.barStyleFloat,
                summary = s.barFloatDesc,
                icon = { FloatingBarIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = floatingOn,
                        onCheckedChange = { on ->
                            // 关掉悬浮时液态玻璃一并关闭（贴边底栏没有玻璃形态）
                            onState(state.copy(barStyle = if (on) BarStyle.Float else BarStyle.Edge))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.barStyleGlass,
                summary = s.barGlassDesc,
                icon = { GlassDropIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    ThemedSwitch(
                        checked = state.barStyle == BarStyle.LiquidGlass,
                        enabled = floatingOn,
                        onCheckedChange = { on ->
                            onState(state.copy(barStyle = if (on) BarStyle.LiquidGlass else BarStyle.Float))
                        }
                    )
                }
            )
            ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ThemedListRow(
                title = s.barBlur,
                summary = s.barBlurDesc,
                icon = { BlurDotsIcon(MaterialTheme.colorScheme.onSurface) },
                trailing = {
                    // 模糊只作用于液态玻璃底栏：未选中玻璃时该项不可用，避免出现"无效组合"
                    ThemedSwitch(
                        checked = state.glassBlurEnabled,
                        enabled = state.barStyle == BarStyle.LiquidGlass,
                        onCheckedChange = { onState(state.copy(glassBlurEnabled = it)) }
                    )
                }
            )
        }
    }
    Spacer(Modifier.height(24.dp))
}

/** 图片取色图标：圆角画框 + 太阳 + 山形（Material 图标集中没有 image，这里按参考图手绘）。 */
@Composable
private fun ImageFrameIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val stroke = size.width * 0.085f
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(size.width * 0.2f),
            style = Stroke(width = stroke)
        )
        drawCircle(color = tint, radius = size.width * 0.075f,
            center = Offset(size.width * 0.33f, size.height * 0.35f))
        val hill = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.76f)
            lineTo(size.width * 0.43f, size.height * 0.50f)
            lineTo(size.width * 0.60f, size.height * 0.68f)
            lineTo(size.width * 0.70f, size.height * 0.58f)
            lineTo(size.width * 0.80f, size.height * 0.76f)
            close()
        }
        drawPath(hill, tint)
    }
}

/**
 * 手机预览示意图（实时预览）：完整反映当前主题的各处变化——
 *   - 配色：页面底色 / 卡片 / 强调容器 / 强调色 / 文字色，全部取自 themedSurfaceColors()
 *     （HyperOS 下就是 Miuix 组件真正在用的那套颜色），并用 260ms 颜色动画平滑过渡；
 *   - 底栏形态：悬浮（内缩 + 圆角 + 抬离底边）↔ 贴边（满宽 + 直角 + 紧贴底边）之间平滑位移/形变；
 *   - 尺寸与圆角：机身与色块的圆角取自当前主题的 shapes token，因此换主题风格时形状随之变化；
 *   - 字体：底栏项文字用当前主题的 labelSmall 渲染，字体样式变化同样可见。
 */
@Composable
private fun ThemePreviewMock(
    barStyle: BarStyle,
    label: (Tab) -> String,
    modifier: Modifier = Modifier
) {
    val colors = themedSurfaceColors()
    val shapeSpec = tween<Color>(THEME_ANIM_MS)
    val sizeSpec = tween<Dp>(THEME_ANIM_MS)

    // 颜色：全部带动画，明暗/种子色切换时平滑渐变
    val background by animateColorAsState(colors.background, shapeSpec, label = "previewBackground")
    val card by animateColorAsState(colors.card, shapeSpec, label = "previewCard")
    val accent by animateColorAsState(colors.primary, shapeSpec, label = "previewAccent")
    val accentContainer by animateColorAsState(colors.primaryContainer, shapeSpec, label = "previewAccentContainer")
    val onSurface by animateColorAsState(colors.onSurface, shapeSpec, label = "previewOnSurface")
    val outline by animateColorAsState(colors.outlineVariant, shapeSpec, label = "previewOutline")

    // 形状：跟随主题的 shapes token（Material3 / Miuix 的圆角层级不同）
    val shapes = MaterialTheme.shapes
    val bodyRadius by animateDpAsState(cornerRadiusOf(shapes.extraLarge), sizeSpec, label = "previewBodyRadius")
    val blockRadius by animateDpAsState(cornerRadiusOf(shapes.small), sizeSpec, label = "previewBlockRadius")

    // 底栏形态：悬浮 = 内缩 + 圆角 + 抬离底边；贴边 = 满宽 + 直角 + 紧贴底边
    val floating = barStyle != BarStyle.Edge
    val barInset by animateDpAsState(if (floating) 8.dp else 0.dp, sizeSpec, label = "previewBarInset")
    val barCorner by animateDpAsState(if (floating) blockRadius else 0.dp, sizeSpec, label = "previewBarCorner")
    val barLift by animateDpAsState(if (floating) 6.dp else 0.dp, sizeSpec, label = "previewBarLift")
    val barHeight by animateDpAsState(if (floating) 30.dp else 34.dp, sizeSpec, label = "previewBarHeight")

    Box(
        modifier = modifier
            .width(132.dp)
            .height(206.dp)
            .clip(RoundedCornerShape(bodyRadius))
            .background(background)
            .border(1.5.dp, outline, RoundedCornerShape(bodyRadius))
    ) {
        // 内容区（含底部为底栏预留的高度，避免与底栏重叠）
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 10.dp, end = 10.dp)
                .padding(bottom = barHeight + barLift + 6.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(30.dp)
                    .clip(RoundedCornerShape(blockRadius)).background(accentContainer))
                Box(Modifier.weight(1f).height(30.dp)
                    .clip(RoundedCornerShape(blockRadius)).background(card))
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().weight(1f)
                .clip(RoundedCornerShape(blockRadius)).background(card))
        }

        // 底栏：与真实底栏同构（4 个项 + 首项强调色），形态随 BarStyle 平滑变化
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(start = barInset, end = barInset, bottom = barLift)
                .fillMaxWidth()
                .clip(RoundedCornerShape(barCorner))
                .background(card)
        ) {
            // 贴边形态用一条顶部分割线与内容分层（悬浮形态下分割线随圆角淡出）
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer { alpha = if (floating) 0f else 1f }
                    .background(outline)
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .padding(horizontal = if (floating) 6.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tab.entries.forEachIndexed { index, tab ->
                    val selected = index == 0
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (selected) accent else onSurface)
                        )
                        Text(
                            text = label(tab),
                            // 沿用主题 labelSmall 的字体族与字重，只把字号缩到示意用的尺寸
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 7.sp,
                                lineHeight = 8.sp
                            ),
                            color = if (selected) accent else onSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/** 模糊图标：半调网点（呼应参考图里的模糊图标）。 */
@Composable
private fun BlurDotsIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val step = size.width / 4f
        repeat(4) { row ->
            repeat(4) { col ->
                // 右下角逐渐变淡变小，形成"虚化"观感
                val fade = 1f - (row + col) / 7f
                drawCircle(
                    color = tint.copy(alpha = 0.35f + 0.65f * fade),
                    radius = step * 0.17f * (0.6f + 0.4f * fade),
                    center = Offset(step * (col + 0.5f), step * (row + 0.5f))
                )
            }
        }
    }
}

/** 悬浮底栏图标：圆角矩形机身 + 底部的实心条。 */
@Composable
private fun FloatingBarIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val stroke = size.width * 0.085f
        val radius = size.width * 0.2f
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(radius),
            style = Stroke(width = stroke)
        )
        val barHeight = size.height * 0.22f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.24f, size.height * 0.62f),
            size = Size(size.width * 0.52f, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f)
        )
    }
}

/** 液态玻璃图标：水滴。 */
@Composable
private fun GlassDropIcon(tint: Color) {
    Canvas(Modifier.size(24.dp)) {
        val r = size.width * 0.29f
        val center = Offset(size.width / 2f, size.height * 0.64f)
        drawCircle(color = tint, radius = r, center = center)
        val path = Path().apply {
            moveTo(size.width / 2f, size.height * 0.06f)
            lineTo(center.x + r * 0.82f, center.y - r * 0.5f)
            lineTo(center.x - r * 0.82f, center.y - r * 0.5f)
            close()
        }
        drawPath(path, tint)
    }
}

/**
 * 「强调色」下拉菜单：默认（应用主色蓝）/ 预设色带 / 从图片取色。
 * 用 Popup 锚定在强调色行下方，带淡入 + 轻微缩放的过渡（与底栏放大镜的出现动效一致）。
 */
@Composable
private fun AccentDropdown(
    expanded: Boolean,
    selectedSeed: Int,
    extracting: Boolean,
    s: Strings,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit,
    onPickImage: () -> Unit
) {
    if (!expanded) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(180)) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, 6),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        ThemedCard(
            modifier = Modifier
                .width(268.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp))
                .graphicsLayer {
                    alpha = progress.value
                    val scale = 0.94f + 0.06f * progress.value
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(1f, 0f)
                },
            shape = MaterialTheme.shapes.large
        ) {
            Column {
                AccentMenuRow(
                    label = s.monetAccentDefault,
                    selected = selectedSeed == DefaultMonetSeed,
                    swatch = Color(DefaultMonetSeed),
                    onClick = { onPick(DefaultMonetSeed); onDismiss() }
                )
                ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Text(
                    s.monetPresets,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp)
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MonetPresets.forEach { preset ->
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(preset))
                                .border(
                                    width = if (selectedSeed == preset) 3.dp else 1.dp,
                                    color = if (selectedSeed == preset) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { onPick(preset); onDismiss() }
                        )
                    }
                }
                ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                AccentMenuRow(
                    label = if (extracting) s.monetExtracting else s.monetPickImage,
                    selected = selectedSeed != DefaultMonetSeed && selectedSeed !in MonetPresets
                        && selectedSeed != NoDynamicSeed,
                    icon = { Icon(Icons.Default.Create, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) },
                    onClick = onPickImage
                )
            }
        }
    }
}

/** 下拉菜单中的一行：可选色点 + 文案（选中时显示对勾）。 */
@Composable
private fun AccentMenuRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatch: Color? = null,
    icon: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (swatch != null) {
            Box(Modifier.size(22.dp).clip(CircleShape).background(swatch))
            Spacer(Modifier.width(14.dp))
        } else if (icon != null) {
            icon()
            Spacer(Modifier.width(14.dp))
        }
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}

/**
 * 莫奈取色子页：选择图片取色 / 预设色 / 配色风格 / 对比度 / 关闭。
 * 仅在 Material3 主题下可达（Miuix 主题的取色入口在「主题设置」页内）。
 */
@Composable
private fun MonetPage(
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
private fun ColorDot(color: Color, selected: Boolean, onClick: (() -> Unit)? = null) {
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

@Composable
private fun SettingsItem(label: String, value: String? = null, onClick: () -> Unit) {
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
private fun LanguageOption(label: String, selected: Boolean, onClick: () -> Unit) {
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

private fun parseEmails(raw: String, defaultFrom: String = ""): List<EmailItem> {
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

private fun parseEmailObject(obj: JSONObject): EmailItem {
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

private fun looksLikeHtml(text: String): Boolean {
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

private val mailLinkJs = """
    (function() {
        document.addEventListener('click', function(e) {
            var el = e.target;
            while (el && el !== document) {
                if (el.tagName === 'A' && el.href) {
                    e.preventDefault();
                    window.location.href = el.href;
                    return;
                }
                el = el.parentNode;
            }
        }, true);
    })();
""".trimIndent()

private fun openMailLink(view: WebView?, uri: Uri?) {
    val u = uri ?: return
    val scheme = u.scheme?.lowercase()
    if (scheme != "http" && scheme != "https" && scheme != "mailto" && scheme != "tel") return
    Log.d("MAIL_LINK", "open: $u")
    try {
        val ctx = view?.context ?: return
        ctx.startActivity(Intent(Intent.ACTION_VIEW, u))
    } catch (e: Exception) {
        Log.d("MAIL_LINK", "open failed: $e")
    }
}

private data class PoemLine(val line: String, val title: String, val author: String)

private val poemApiUrls = listOf(
    "https://poetry.palemoky.com/api/v1/poems/random",
    "https://poetry.palemoky.com/api/poems/random"
)

private fun fetchRandomPoemLine(client: OkHttpClient): PoemLine? {
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

private fun parsePoemResponse(body: String): PoemLine? {
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

private fun versionCompare(a: String, b: String): Int {
    val aParts = a.split(".").map { it.toIntOrNull() ?: 0 }
    val bParts = b.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLen = maxOf(aParts.size, bParts.size)
    for (i in 0 until maxLen) {
        val av = aParts.getOrElse(i) { 0 }
        val bv = bParts.getOrElse(i) { 0 }
        if (av != bv) return av - bv
    }
    return 0
}

private fun stripHtml(html: String): String {
    return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
        .toString().trim()
}

private fun emailKey(item: EmailItem): String {
    return "${item.from}|${item.subject}|${item.time}"
}

private fun mergeEmailItems(oldList: List<EmailItem>, newList: List<EmailItem>): List<EmailItem> {
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

private fun extractVerificationCode(text: String): String? {
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
private fun parseEmailTime(time: String): Long {
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
private fun formatIteTime(iso: String): String {
    val ts = parseEmailTime(iso)
    if (ts == 0L) return iso
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(java.util.Date(ts))
}

// 剩余毫秒 → 人类可读时长（取相邻两级单位，单位缩写全球通用）："6d 23h" / "5h 23m" / "23m 45s" / "45s"
private fun formatDurationWords(remMs: Long): String {
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
private fun parseIteEmails(body: String, defaultFrom: String): List<EmailItem> {
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
private fun appStateToJson(state: AppState): String {
    val j = JSONObject()
    j.put("email", state.email)
    j.put("count", state.count)
    j.put("language", state.language)
    j.put("themeMode", state.themeMode.key)
    j.put("autoCheckUpdate", state.autoCheckUpdate)
    j.put("themeStyle", state.themeStyle.key)
    j.put("barStyle", state.barStyle.key)
    j.put("glassBlurEnabled", state.glassBlurEnabled)
    j.put("dynamicSeed", state.dynamicSeed)
    j.put("dynamicStyle", state.dynamicStyle.key)
    j.put("dynamicContrast", state.dynamicContrast.toDouble())
    j.put("tab", state.currentTab.ordinal)
    j.put("mailProvider", state.mailProvider.key)
    j.put("iteToken", state.iteToken)
    j.put("mailboxExpiresAt", state.mailboxExpiresAt)
    // items 限制条数、正文截断、且不保存 htmlBody（完整 HTML 动辄数十 KB），
    // 防止写入 Bundle 越过 Binder 事务上限导致 TransactionTooLargeException
    val items = JSONArray()
    state.items.takeLast(20).forEach { item ->
        items.put(JSONObject().apply {
            put("from", item.from)
            put("subject", item.subject)
            put("time", item.time)
            put("body", item.body.take(2000))
            put("ts", item.timestamp)
        })
    }
    j.put("items", items)
    val raws = JSONArray()
    state.rawMessages.takeLast(5).forEach { raws.put(it) }
    j.put("raws", raws)
    val hist = JSONArray()
    state.history.forEach { h ->
        hist.put(JSONObject().apply {
            put("email", h.email)
            put("active", h.isActive)
            put("token", h.token)
            put("expiresAt", h.expiresAt)
        })
    }
    j.put("history", hist)
    return j.toString()
}

private fun appStateFromJson(json: String): AppState? {
    return try {
        val j = JSONObject(json)
        val items = mutableListOf<EmailItem>()
        j.optJSONArray("items")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                items.add(
                    EmailItem(
                        from = o.optString("from", ""),
                        subject = o.optString("subject", ""),
                        time = o.optString("time", ""),
                        body = o.optString("body", ""),
                        htmlBody = o.optString("htmlBody", ""),
                        timestamp = o.optLong("ts", 0L)
                    )
                )
            }
        }
        val raws = mutableListOf<String>()
        j.optJSONArray("raws")?.let { arr ->
            for (i in 0 until arr.length()) raws.add(arr.optString(i))
        }
        val hist = mutableListOf<HistoryEmail>()
        j.optJSONArray("history")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.optJSONObject(i) ?: continue
                hist.add(HistoryEmail(o.optString("email", ""), o.optBoolean("active", false), o.optString("token", ""), o.optLong("expiresAt", 0L)))
            }
        }
        AppState(
            email = j.optString("email", ""),
            count = j.optInt("count", 0),
            rawMessages = raws,
            items = items,
            isLoading = false,
            history = hist,
            currentTab = Tab.entries.getOrElse(j.optInt("tab", 0)) { Tab.Inbox },
            themeMode = ThemeMode.fromKey(j.optString("themeMode", ThemeMode.Light.key)),
            language = j.optString("language", "zh"),
            autoCheckUpdate = j.optBoolean("autoCheckUpdate", true),
            themeStyle = ThemeStyle.fromKey(j.optString("themeStyle", ThemeStyle.Material3.key)),
            barStyle = BarStyle.fromKey(j.optString("barStyle", BarStyle.Float.key)),
            glassBlurEnabled = j.optBoolean("glassBlurEnabled", true),
            dynamicSeed = j.optInt("dynamicSeed", NoDynamicSeed),
            dynamicStyle = DynamicStyle.fromKey(j.optString("dynamicStyle", DynamicStyle.TonalSpot.key)),
            dynamicContrast = j.optDouble("dynamicContrast", 0.0).toFloat(),
            mailProvider = MailProvider.fromKey(j.optString("mailProvider", MailProvider.PearApi.key)),
            iteToken = j.optString("iteToken", ""),
            mailboxExpiresAt = j.optLong("mailboxExpiresAt", 0L)
        )
    } catch (e: Exception) {
        null
    }
}

private val AppStateSaver: Saver<AppState, String> = Saver(
    save = { appStateToJson(it.copy(isLoading = false)) },
    restore = { appStateFromJson(it) }
)
