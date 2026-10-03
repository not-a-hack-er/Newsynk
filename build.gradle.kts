import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") apply false
    // These plugins generate the Crashlytics build ID and instrument network
    // performance. Including only their runtime SDKs makes Firebase crash the
    // process before MainActivity can start.
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    // Hilt
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("androidx.baselineprofile")
}

// Firebase configuration is intentionally kept out of version control. Applying
// the plugin conditionally keeps a fresh clone buildable before Firebase setup.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

// Load API key from local.properties
val localProperties = Properties().apply {
    val propertiesFile = sequenceOf(
        rootProject.file("local.properties"),
        project.file("local.properties")
    ).firstOrNull { it.exists() }
    if (propertiesFile != null) load(propertiesFile.inputStream())
}

val releaseStorePath = localProperties.getProperty("RELEASE_STORE_FILE")
val releaseStore = releaseStorePath?.let { file(it) }
val releaseSigningConfigured = releaseStore?.exists() == true &&
    !localProperties.getProperty("RELEASE_STORE_PASSWORD").isNullOrBlank() &&
    !localProperties.getProperty("RELEASE_KEY_ALIAS").isNullOrBlank() &&
    !localProperties.getProperty("RELEASE_KEY_PASSWORD").isNullOrBlank()
val backendBaseUrl = localProperties.getProperty("NEWSYNK_BACKEND_URL", "")
    .trim()
    .let { if (it.isBlank()) "https://example.invalid/" else "${it.trimEnd('/')}/" }

android {
    namespace = "com.abpvt.newsapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.abpvt.newsapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "2.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API keys from local.properties into BuildConfig
        buildConfigField(
            "String",
            "NEWS_API_KEY",
            "\"${localProperties.getProperty("NEWS_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "GNEWS_API_KEY",
            "\"${localProperties.getProperty("GNEWS_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "CURRENTS_API_KEY",
            "\"${localProperties.getProperty("CURRENTS_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "NEWSDATA_API_KEY",
            "\"${localProperties.getProperty("NEWSDATA_API_KEY", "")}\""
        )
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )
        buildConfigField("String", "NEWSYNK_BACKEND_URL", "\"$backendBaseUrl\"")
    }

    signingConfigs {
        if (releaseSigningConfigured) create("release") {
            storeFile = releaseStore
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            // Production news-provider credentials belong in the server proxy,
            // never in a reverse-engineerable APK.
            buildConfigField("String", "NEWS_API_KEY", "\"\"")
            buildConfigField("String", "GNEWS_API_KEY", "\"\"")
            buildConfigField("String", "CURRENTS_API_KEY", "\"\"")
            buildConfigField("String", "NEWSDATA_API_KEY", "\"\"")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true   // Required to access BuildConfig.NEWS_API_KEY
    }
    packaging {
        resources {
            // Exclude duplicate META-INF files that come from transitive deps (jspecify, okhttp, etc.)
            excludes += setOf(
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Material 3 extended icons (AutoMirrored, etc.)
    implementation("androidx.compose.material:material-icons-extended")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Navigation (Compose)
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // Chrome Custom Tabs
    implementation("androidx.browser:browser:1.9.0")

    // Hilt — dependency injection (2.57.2: last patch on AGP-8-compatible line; 2.58+ requires AGP 9)
    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-compiler:2.57.2")
    // hiltViewModel() for Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // WorkManager (daily digest scheduler)
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    // Hilt WorkManager integration — enables @HiltWorker
    implementation("androidx.hilt:hilt-work:1.2.0")

    // ViewModel + StateFlow
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // Room powers the offline-first feed and downloaded reading cache.
    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    baselineProfile(project(":benchmark"))

    // Retrofit (News API) — OkHttp logging-interceptor kept at 4.12.0 (stable, matches Retrofit 3's bundled OkHttp)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Firebase (BoM) — note: -ktx suffix removed as of BOM 33+, KTX merged into main artifacts
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-perf")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    debugImplementation("com.google.firebase:firebase-appcheck-debug")

    // Coil (Image Loading) — kept on 2.x to avoid Coil 3 breaking migration (new artifact group + imports)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Google Sign-In
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    implementation("com.google.android.gms:play-services-auth:21.5.1")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
