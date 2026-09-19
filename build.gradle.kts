plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    // Kotlin 2.0+ 起 Compose 编译器随 Kotlin 版本发布，需单独应用该插件
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
