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

            export(project(":sdk:core:analytics"))
            export(project(":sdk:core:extensions"))
            export(project(":sdk:core:models"))
            export(project(":sdk:features:auth"))
            export(project(":sdk:features:teetimes"))
            export(libs.mokoresources.api)
        }
    }

    sourceSets {

        commonMain.dependencies {
            api(project(":sdk:core:analytics"))
            api(project(":sdk:core:extensions"))
            api(project(":sdk:core:models"))
            api(project(":sdk:features:auth"))
            api(project(":sdk:features:teetimes"))
            implementation(project(":sdk:core:storage"))
            implementation(libs.multiplatform.settings)
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
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
