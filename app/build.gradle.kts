import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// 签名凭据从根目录 keystore.properties（不纳入版本控制）读取，避免明文密码泄露。
// 也可通过环境变量 KEYSTORE_PASSWORD / KEY_PASSWORD 提供（CI 场景）。
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystorePropertiesFile.inputStream().use { keystoreProperties.load(it) }
}
val releaseStorePassword = keystoreProperties.getProperty("storePassword")
    ?: System.getenv("KEYSTORE_PASSWORD")
val releaseKeyPassword = keystoreProperties.getProperty("keyPassword")
    ?: System.getenv("KEY_PASSWORD")
val hasReleaseKeystore = !releaseStorePassword.isNullOrBlank() && !releaseKeyPassword.isNullOrBlank()

android {
    namespace = "com.tempmail.app"
    // Miuix 0.9.3 要求 compileSdk >= 37（AGP 8.13 推荐上限 36，已在
    // gradle.properties 用 android.suppressUnsupportedCompileSdk=37 放行）
    compileSdk = 37

    defaultConfig {
        applicationId = "com.tempmail.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 9
        versionName = "1.9.1"
    }

    if (hasReleaseKeystore) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile", "keystore.jks"))
                storePassword = releaseStorePassword
                keyAlias = keystoreProperties.getProperty("keyAlias", "tempmail")
                keyPassword = releaseKeyPassword
            }
        }
    }

    adbOptions {
        installOptions("-t")
    }

    buildTypes {
        debug {
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseKeystore) signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Compose BOM 2026.05.01 -> ui/foundation/runtime 1.11.2（与 Miuix 所需的
    // JetBrains Compose 1.11.1 映射的 androidx 版本精确一致）、material3 1.4.0
    val composeBom = platform("androidx.compose:compose-bom:2026.05.01")
    implementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    // material3 1.4.0 起不再传递依赖 material-icons-core，需显式声明
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.foundation:foundation")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Miuix（KernelSU 同款 UI 框架）：miuix-ui 提供组件，miuix-blur 提供
    // RuntimeShader 液态玻璃模糊（实际模糊效果仅 API 33+，低版本自动降级）
    val miuixVersion = "0.9.3"
    implementation("top.yukonga.miuix.kmp:miuix-ui-android:$miuixVersion")
    implementation("top.yukonga.miuix.kmp:miuix-blur-android:$miuixVersion")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
