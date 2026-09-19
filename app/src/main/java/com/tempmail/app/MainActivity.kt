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
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.tempmail.app.ui.glass.GlassBarItem
import com.tempmail.app.ui.glass.GlassBarSpace
import com.tempmail.app.ui.glass.GlassShell
import com.tempmail.app.ui.glass.isGlassBlurSupported
import com.tempmail.app.ui.theme.TempMailTheme
import com.tempmail.app.ui.theme.ThemeStyle
import com.tempmail.app.ui.theme.ThemedButton
import com.tempmail.app.ui.theme.ThemedCard
import com.tempmail.app.ui.theme.ThemedDivider
import com.tempmail.app.ui.theme.ThemedIconButton
import com.tempmail.app.ui.theme.ThemedLinearProgress
import com.tempmail.app.ui.theme.ThemedSwitch
import com.tempmail.app.ui.theme.ThemedTextButton
import com.tempmail.app.ui.theme.themedCornerShape
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import kotlin.random.Random
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
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
    val glassHint: String
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
        barStyle = "ボトムバー", barStyleFloat = "フローティング", barStyleGlass = "Liquid Glass", glassHint = "バーを長押ししてドラッグでタブを切り替え"
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
        barStyle = "하단 바", barStyleFloat = "플로팅", barStyleGlass = "Liquid Glass", glassHint = "하단 바를 길게 누른 뒤 드래그해 탭 전환"
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
        barStyle = "Barre inférieure", barStyleFloat = "Flottant", barStyleGlass = "Liquid Glass", glassHint = "Appuyez longuement sur la barre et faites glisser pour changer d'onglet"
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
        barStyle = "Navigationsleiste", barStyleFloat = "Schwebend", barStyleGlass = "Liquid Glass", glassHint = "Leiste lange drücken und ziehen, um Tabs zu wechseln"
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
        barStyle = "Barra inferior", barStyleFloat = "Flotante", barStyleGlass = "Liquid Glass", glassHint = "Mantén pulsada la barra y arrastra para cambiar de pestaña"
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
        barStyle = "Barra inferior", barStyleFloat = "Flutuante", barStyleGlass = "Liquid Glass", glassHint = "Pressione e arraste a barra para trocar de aba"
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
        barStyle = "Нижняя панель", barStyleFloat = "Плавающая", barStyleGlass = "Liquid Glass", glassHint = "Зажмите панель и потяните, чтобы переключить вкладку"
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
        barStyle = "Barra inferiore", barStyleFloat = "Fluttuante", barStyleGlass = "Liquid Glass", glassHint = "Tieni premuta la barra e trascina per cambiare scheda"
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
        barStyle = "الشريط السفلي", barStyleFloat = "عائم", barStyleGlass = "Liquid Glass", glassHint = "اضغط مطولًا على الشريط واسحب لتبديل التبويب"
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
        barStyle = "निचला बार", barStyleFloat = "फ़्लोटिंग", barStyleGlass = "Liquid Glass", glassHint = "बार को देर तक दबाकर खींचें और टैब बदलें"
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
        barStyle = "Thanh dưới", barStyleFloat = "Nổi", barStyleGlass = "Liquid Glass", glassHint = "Nhấn giữ thanh và kéo để chuyển tab"
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
        barStyle = "แถบล่าง", barStyleFloat = "ลอย", barStyleGlass = "Liquid Glass", glassHint = "กดแถบค้างแล้วลากเพื่อสลับแท็บ"
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
        barStyle = "Bilah bawah", barStyleFloat = "Mengambang", barStyleGlass = "Liquid Glass", glassHint = "Tekan lama bilah lalu geser untuk berpindah tab"
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
        barStyle = "底栏风格", barStyleFloat = "悬浮", barStyleGlass = "Liquid Glass", glassHint = "长按底栏可左右拖动切换标签"
    )
}

enum class Tab(val icon: ImageVector) {
    Inbox(Icons.Default.Email),
    History(Icons.Default.DateRange),
    Settings(Icons.Default.Settings)
}

// 底栏风格，仅在 Miuix 主题下可选。持久化使用稳定字符串 key；
// fromKey 对未知值一律回退 Float（现有悬浮底栏），禁止直接 valueOf
enum class BarStyle(val key: String) {
    Float("float"),
    LiquidGlass("liquid_glass");

    companion object {
        fun fromKey(key: String?): BarStyle = entries.find { it.key == key } ?: Float
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
    val isActive: Boolean
)

data class AppState(
    val email: String = "",
    val count: Int = 0,
    val rawMessages: List<String> = emptyList(),
    val items: List<EmailItem> = emptyList(),
    val isLoading: Boolean = false,
    val history: List<HistoryEmail> = emptyList(),
    val currentTab: Tab = Tab.Inbox,
    val isDarkMode: Boolean = false,
    val language: String = "zh",
    val autoCheckUpdate: Boolean = true,
    val themeStyle: ThemeStyle = ThemeStyle.Material3,
    val barStyle: BarStyle = BarStyle.Float
)

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
                    isDarkMode = prefs.getBoolean("isDarkMode", false),
                    autoCheckUpdate = prefs.getBoolean("autoCheckUpdate", true),
                    themeStyle = ThemeStyle.fromKey(prefs.getString("themeStyle", ThemeStyle.Material3.key)),
                    barStyle = BarStyle.fromKey(prefs.getString("barStyle", BarStyle.Float.key))
                ))
            }
            val snackbar = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            val s = strings(state.language)

            // 应用内深色开关与系统夜间模式相互独立，状态栏/导航栏图标颜色需跟随应用主题
            val view = LocalView.current
            LaunchedEffect(state.isDarkMode) {
                val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !state.isDarkMode
                    isAppearanceLightNavigationBars = !state.isDarkMode
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

            fun doRefresh(e: String, onDone: (count: Int, raw: String) -> Unit) {
                state = state.copy(isLoading = true)
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
                                scope.launch { snackbar.showSnackbar(j.optString("msg", s.queryFailed)) }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            state = state.copy(isLoading = false)
                            scope.launch { snackbar.showSnackbar(e.message ?: s.networkError) }
                        }
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

            TempMailTheme(darkTheme = state.isDarkMode, themeStyle = state.themeStyle) {
                // 两套主题共用同一份页面内容，仅外层布局与底栏形态不同
                val tabLabel: (Tab) -> String = { tab ->
                    when (tab) {
                        Tab.Inbox -> s.inbox
                        Tab.History -> s.history
                        Tab.Settings -> s.settings
                    }
                }
                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局。
                // glassOverlap 为页面滚动内容末尾需预留的底栏高度，避免最后一项被底栏遮挡。
                val glassActive = state.themeStyle == ThemeStyle.HyperOS &&
                    state.barStyle == BarStyle.LiquidGlass &&
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
                            Tab.Inbox -> InboxTab(contentPadding, glassOverlap, state, snackbar, scope, context, s, client, poem, ::doRefresh) { updater ->
                                state = updater(state)
                            }
                            Tab.History -> HistoryTab(contentPadding, glassOverlap, state, s) { email ->
                                val newHistory = if (state.email.isNotBlank() && state.email != email)
                                    state.history + HistoryEmail(state.email, false)
                                else state.history
                                val filteredHistory = newHistory.filter { it.email != email }
                                state = state.copy(
                                    email = email,
                                    count = 0,
                                    rawMessages = emptyList(),
                                    items = emptyList(),
                                    history = filteredHistory
                                )
                            }
                            Tab.Settings -> SettingsTab(contentPadding, glassOverlap, state, s, snackbar, scope, client, onCheckUpdate = { manual -> checkUpdate(manual) }) { newState ->
                                if (newState.language != state.language) {
                                    prefs.edit().putString("language", newState.language).apply()
                                }
                                if (newState.isDarkMode != state.isDarkMode) {
                                    prefs.edit().putBoolean("isDarkMode", newState.isDarkMode).apply()
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
                                state = newState
                            }
                        }
                    }
                }

                // 真·液态玻璃：HyperOS 主题 + 液态玻璃底栏 + API 33+（RuntimeShader）时启用，
                // 内容铺满整屏并挂 backdrop 图层，底栏浮于其上做真实模糊/折射；否则走原有布局
                GlassShell(
                    glass = glassActive,
                    darkTheme = state.isDarkMode,
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
                                // Miuix：底栏风格可选（Material3 主题不提供，保持原样）
                                when (state.barStyle) {
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

// ==================== Miuix 主题底栏（两种风格可选） ====================

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
    val barColor = MaterialTheme.colorScheme.surfaceVariant
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
    // 指示器由本组件自行绘制（不经过 M3 NavigationBar），故可直接使用半透明色，
    // 预合成到基底色上以保证在深浅两种背景下都有足够存在感
    val indicatorColor = scheme.primary.copy(alpha = 0.16f)
        .compositeOver(scheme.surface)
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
                .background(scheme.surface, glassShape)
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
    doRefresh: (String, (Int, String) -> Unit) -> Unit,
    onState: ((AppState) -> AppState) -> Unit
) {
    var showBodyDialog by remember { mutableStateOf(false) }
    var dialogBody by remember { mutableStateOf("") }
    var dialogHtml by remember { mutableStateOf("") }
    var showPoem by remember { mutableStateOf(false) }

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
                        val r = Request.Builder()
                            .url("https://api.pearapi.ai/api/email/?type=get")
                            .get().build()
                        val body = client.newCall(r).execute().body?.string() ?: ""
                        val j = JSONObject(body)
                        if (j.optString("code") == "200") {
                            val newEmail = j.optString("email", "")
                            withContext(Dispatchers.Main) {
                                // 基于写入时的最新状态合并历史，避免覆盖请求期间的其他状态变更
                                onState { cur ->
                                    val newHistory = if (cur.email.isNotBlank())
                                        cur.history + HistoryEmail(cur.email, false)
                                    else cur.history
                                    cur.copy(
                                        email = newEmail, count = 0,
                                        rawMessages = emptyList(), items = emptyList(),
                                        history = newHistory, isLoading = false
                                    )
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onState { it.copy(isLoading = false) }
                                scope.launch { snackbar.showSnackbar(j.optString("msg", s.fetchFailed)) }
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
            ThemedCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(20.dp)) {
                    Text(s.yourEmail, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(12.dp))
                    Text("${s.receivedCount}: ${state.items.size}", style = MaterialTheme.typography.labelSmall)
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
                            onClick = {
                                val emailAtRefresh = state.email
                                doRefresh(emailAtRefresh) { cnt, raw ->
                                    val newItems = if (raw.isBlank()) emptyList()
                                        else parseEmails(raw, s.unknownSender)
                                    var mergedSize = -1
                                    // 基于写入时的最新状态更新，避免覆盖刷新期间切换的设置项
                                    onState { cur ->
                                        if (cur.email != emailAtRefresh) {
                                            cur.copy(isLoading = false)
                                        } else {
                                            val merged = mergeEmailItems(cur.items, newItems)
                                            mergedSize = merged.size
                                            Log.d("MAIL_DEBUG", "API返回count=$cnt 解析后=${newItems.size} 已有=${cur.items.size} 合并后=${merged.size}")
                                            // rawMessages 在内存中同样限长，避免长会话单调增长
                                            val newRaws = if (raw.isBlank()) cur.rawMessages
                                                else (cur.rawMessages + raw).takeLast(5)
                                            cur.copy(
                                                count = cnt, rawMessages = newRaws,
                                                items = merged, isLoading = false
                                            )
                                        }
                                    }
                                    scope.launch {
                                        if (raw.isBlank()) snackbar.showSnackbar(s.noNewMail)
                                        else if (mergedSize == 0) snackbar.showSnackbar(s.parseError)
                                    }
                                }
                            },
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
                                // 不自动加载远程图片，避免追踪像素泄露用户 IP 与"已读"状态
                                settings.loadsImagesAutomatically = false
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
                                    loadDataWithBaseURL("https://example.com", dialogBody, "text/plain", "UTF-8", null)
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
        // 玻璃底栏：内容可滚到浮起的底栏下方，末尾预留底栏高度使最后一项仍可完整显示
        if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
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

        val allEmails = remember(state.email, state.history) {
            val list = mutableListOf<HistoryEmail>()
            if (state.email.isNotBlank()) list.add(HistoryEmail(state.email, true))
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

private enum class SettingsPage { Main, Language, DarkMode, Theme, BarStyle, About, Author }

@Composable
private fun SettingsTab(
    p: PaddingValues,
    bottomOverlap: Dp,
    state: AppState,
    s: Strings,
    snackbar: SnackbarHostState,
    scope: CoroutineScope,
    client: OkHttpClient,
    onCheckUpdate: (Boolean) -> Unit,
    onState: (AppState) -> Unit
) {
    var page by remember { mutableStateOf(SettingsPage.Main) }
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
                                        label = s.darkMode,
                                        value = if (state.isDarkMode) "ON" else "OFF",
                                        onClick = { page = SettingsPage.DarkMode }
                                    )
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    SettingsItem(
                                        label = s.themeStyle,
                                        value = if (state.themeStyle == ThemeStyle.HyperOS) s.themeHyperOS else s.themeDefault,
                                        onClick = { page = SettingsPage.Theme }
                                    )
                                    // 底栏风格仅在 Miuix 主题下提供，Material3 主题保持原样
                                    if (state.themeStyle == ThemeStyle.HyperOS) {
                                        ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                        SettingsItem(
                                            label = s.barStyle,
                                            value = if (state.barStyle == BarStyle.LiquidGlass) s.barStyleGlass else s.barStyleFloat,
                                            onClick = { page = SettingsPage.BarStyle }
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

                        SettingsPage.DarkMode -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.darkModeSetting, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Row(
                                    Modifier.padding(horizontal = 20.dp, vertical = 16.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(s.darkMode, style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f))
                                    ThemedSwitch(
                                        checked = state.isDarkMode,
                                        onCheckedChange = { onState(state.copy(isDarkMode = it)) }
                                    )
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

                        SettingsPage.BarStyle -> {
                            ThemedIconButton(onClick = { page = SettingsPage.Main }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(s.barStyle, style = MaterialTheme.typography.headlineSmall)
                            Spacer(Modifier.height(20.dp))

                            ThemedCard(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                Column {
                                    LanguageOption(s.barStyleFloat, state.barStyle == BarStyle.Float,
                                        onClick = { onState(state.copy(barStyle = BarStyle.Float)); page = SettingsPage.Main })
                                    ThemedDivider(modifier = Modifier.padding(horizontal = 16.dp))
                                    LanguageOption(s.barStyleGlass, state.barStyle == BarStyle.LiquidGlass,
                                        onClick = { onState(state.copy(barStyle = BarStyle.LiquidGlass)); page = SettingsPage.Main })
                                }
                            }
                        }

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
                            Spacer(Modifier.height(12.dp))
                            ThemedDivider()
                            Spacer(Modifier.height(12.dp))
                            Text("Email", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("(yjhsbwssg@163.com)", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(12.dp))
                            Text("微信", style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("(yjhsbwssg)", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                    }
                }
            }
            // 玻璃底栏：滚动内容末尾预留底栏高度，使其可完整滚出（底栏浮在其上）
            if (bottomOverlap > 0.dp) Spacer(Modifier.height(bottomOverlap))
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
            val d = sdf.parse(t)
            if (d != null) return d.time
        } catch (_: Exception) {
        }
    }
    return 0L
}

// AppState 序列化为 JSON，用于 rememberSaveable 在配置更改/进程重建后恢复状态。
// isLoading 不持久化（请求协程已随组合销毁），rawMessages 只保留最近 5 条控制 Bundle 体积。
private fun appStateToJson(state: AppState): String {
    val j = JSONObject()
    j.put("email", state.email)
    j.put("count", state.count)
    j.put("language", state.language)
    j.put("isDarkMode", state.isDarkMode)
    j.put("autoCheckUpdate", state.autoCheckUpdate)
    j.put("themeStyle", state.themeStyle.key)
    j.put("barStyle", state.barStyle.key)
    j.put("tab", state.currentTab.ordinal)
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
                hist.add(HistoryEmail(o.optString("email", ""), o.optBoolean("active", false)))
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
            isDarkMode = j.optBoolean("isDarkMode", false),
            language = j.optString("language", "zh"),
            autoCheckUpdate = j.optBoolean("autoCheckUpdate", true),
            themeStyle = ThemeStyle.fromKey(j.optString("themeStyle", ThemeStyle.Material3.key)),
            barStyle = BarStyle.fromKey(j.optString("barStyle", BarStyle.Float.key))
        )
    } catch (e: Exception) {
        null
    }
}

private val AppStateSaver: Saver<AppState, String> = Saver(
    save = { appStateToJson(it.copy(isLoading = false)) },
    restore = { appStateFromJson(it) }
)
