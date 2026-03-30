# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the proguardFiles
# setting in build.gradle.kts.

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**

# ── Firebase ──────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# ── Retrofit + OkHttp ────────────────────────────────────────────────────────
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# ── Gson (JSON models) ────────────────────────────────────────────────────────
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
# Preserve all data model classes used for JSON deserialization
-keep class com.abpvt.newsapp.data.model.** { *; }

# ── Coil (image loading) ──────────────────────────────────────────────────────
-keep class coil.** { *; }
-dontwarn coil.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── App-specific ─────────────────────────────────────────────────────────────
# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }
# Keep Firestore data model fields
-keepclassmembers class com.abpvt.newsapp.data.** {
    public <init>();
    public <fields>;
}