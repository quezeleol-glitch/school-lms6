import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun prop(name: String, def: String = ""): String =
    (localProps.getProperty(name)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }
        ?: def).trim().replace("\"", "")

android {
    namespace = "id.man19.guru"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.man19.guru"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "GEMINI_API_KEY", "\"${prop("GEMINI_API_KEY")}\"")
        buildConfigField("String", "GEMINI_MODEL", "\"${prop("GEMINI_MODEL", "gemini-3.5-flash-lite")}\"")
        buildConfigField("String", "GURU_PIN", "\"${prop("GURU_PIN", "ROXB")}\"")
        buildConfigField("String", "START_URL", "\"https://man19.assessment.web.id/login\"")
    }

    buildFeatures { buildConfig = true; viewBinding = true }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Biar APK release bisa langsung diinstall, ditandatangani pakai debug key.
            // Untuk rilis resmi (Play Store), ganti dengan keystore sendiri.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
