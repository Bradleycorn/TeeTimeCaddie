@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version libs.versions.kotlin
}

kotlin {
    android {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    ios()
    iosSimulatorArm64()

    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.experimental.ExperimentalObjCRefinement")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.crashkios)
                implementation(libs.firebase.mpp.config)
                implementation(libs.firebase.mpp.crashlytics)
                implementation(libs.firebase.mpp.performance)
                implementation(libs.kermit.core)
                implementation(libs.kotlinx.serialization.core)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-properties:1.5.0-RC")
                implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.21")
                implementation(project(":core:extensions"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting
        val androidUnitTest by getting

        val iosMain by getting
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosTest by getting
        val iosSimulatorArm64Test by getting {
            dependsOn(iosTest)
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