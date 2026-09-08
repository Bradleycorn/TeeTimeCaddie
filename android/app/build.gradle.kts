@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application")
    kotlin("android")
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.androidx.compose.compiler)
    alias(libs.plugins.hilt)
}

android {
    namespace = "net.bradball.teetimecaddie.android"
    compileSdk = 36
    defaultConfig {
        applicationId = "net.bradball.teetimecaddie.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            manifestPlaceholders["analyticsCollectionEnabled"] = "false"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
            manifestPlaceholders["performanceCollectionEnabled"] = "false"
            manifestPlaceholders["logPerformanceCollection"] = "false"
        }

        getByName("release") {
            isMinifyEnabled = false
            manifestPlaceholders["analyticsCollectionEnabled"] = "true"
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
            manifestPlaceholders["performanceCollectionEnabled"] = "true"
            manifestPlaceholders["logPerformanceCollection"] = "false"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi"
        )
    }

}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":sdk"))

    // CORE
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.splashscreen)

    // KOTLIN
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.core)

    // ANDROIDX - COMPOSE
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.accompanist)
    debugImplementation(libs.bundles.android.debug)

    // ANDROIDX - LIFECYCLE
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.lifecycle.process)

    // FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.bundles.firebase)

    // HILT
    implementation(libs.hilt.android)
    ksp(libs.hilt.ksp)

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
