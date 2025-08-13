@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget { }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "TeeTimeCaddieKit"
            isStatic = true
            binaryOption("bundleId", "${project.group}.teetimecaddiekit")

            export(project(":core:analytics")) { transitiveExport = true }
            export(project(":core:extensions")) { transitiveExport = true }
            export(project(":core:models")) { transitiveExport = true }
            export(project(":features:auth")) { transitiveExport = true }
            export(project(":features:teetimes")) { transitiveExport = true }
            export(libs.mokoresources.api) { transitiveExport = true }
        }
    }

    sourceSets {

        commonMain.dependencies {
            api(project(":core:analytics"))
            api(project(":core:extensions"))
            api(project(":core:models"))
            api(project(":features:auth"))
            api(project(":features:teetimes"))
            implementation(project(":core:storage"))
            implementation(libs.crashkios)
            implementation(libs.multiplatform.settings)
            implementation(libs.firebase.mpp.auth)
            implementation(libs.firebase.mpp.firestore)
        }

        commonTest.dependencies {
                implementation(kotlin("test"))
        }

        androidMain {
            kotlin.srcDir("build/generated/moko/androidMain/src")
        }
    }
}

android {
    namespace = "net.bradball.teetimecaddie"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
