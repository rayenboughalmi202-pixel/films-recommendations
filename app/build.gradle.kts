import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Optional build-time TMDB key: put  tmdb.api.key=YOUR_KEY  in local.properties
// (never commit local.properties). The user can also paste the key inside the
// app itself (Settings button), which is the recommended way for a ready APK.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val tmdbKeyFromLocal: String = localProps.getProperty("tmdb.api.key", "")

android {
    namespace = "com.yami.films"
    // Google Play (since 31 Aug 2026): new apps must target API 36 (Android 16).
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yami.films"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbKeyFromLocal\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
