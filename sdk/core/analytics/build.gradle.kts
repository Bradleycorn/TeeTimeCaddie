import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version libs.versions.kotlin
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalObjCRefinement")
            }
        }


        commonMain.dependencies {
            implementation(libs.crashkios)
            implementation(libs.firebase.mpp.config)
            implementation(libs.firebase.mpp.crashlytics)
            implementation(libs.firebase.mpp.performance)
            implementation(libs.kermit.core)
            implementation(libs.kotlinx.serialization.core)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.9.0")
            implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
            implementation(project(":sdk:core:extensions"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "net.bradball.teetimecaddie.core.analytics"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}