pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.android.test") version "8.9.1"
        id("androidx.baselineprofile") version "1.4.1"
        id("com.google.gms.google-services") version "4.4.4"
        id("com.google.dagger.hilt.android") version "2.57.2"
        id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Newsynk"
include(":benchmark")
