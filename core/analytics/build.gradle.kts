@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version libs.versions.kotlin
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
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
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.5.1")
            implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
            implementation(project(":core:extensions"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "net.bradball.teetimecaddie.core.analytics"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}