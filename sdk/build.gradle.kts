@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
    alias(libs.plugins.skie)
}

kotlin {
    androidTarget {
        // Every other SDK module pins this. :sdk did not, so it compiled to Java 21 bytecode while
        // the Android app expects 17 — invisible until a :sdk type (SessionManager) first appeared
        // in KSP-generated Java and javac had to read the class file.
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

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
            export(project(":sdk:features:players"))
            export(project(":sdk:features:teetimes"))
            export(libs.mokoresources.api)
            export(libs.kotlinx.datetime)
            export(libs.nsexceptionkt)
        }
    }

    sourceSets {

        appleMain.dependencies {
            api(libs.nsexceptionkt)
        }

        commonMain.dependencies {
            api(project(":sdk:core:analytics"))
            api(project(":sdk:core:extensions"))
            api(project(":sdk:core:models"))
            api(project(":sdk:features:auth"))
            api(project(":sdk:features:players"))
            api(project(":sdk:features:teetimes"))
            implementation(project(":sdk:core:storage"))
            implementation(libs.multiplatform.settings)
            implementation(libs.firebase.mpp.auth)
            implementation(libs.firebase.mpp.firestore)
            api(libs.kotlinx.datetime)
        }

        commonTest.dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
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
    testOptions {
        unitTests {
            // SessionManager takes an EventManager, whose Kermit logger reaches android.os.Process
            // on construction. These tests exercise session logic, not Android behaviour, so let
            // the unmocked framework calls return defaults rather than throwing.
            isReturnDefaultValues = true
        }
    }
}

// iOS test binaries cannot be linked in this project.
//
// GitLive's Firebase modules bake `-framework FirebaseCore` (and friends) into their linker
// options, but those frameworks only exist inside the iOS app's SPM checkout — so `ld` fails with
// "framework 'FirebaseCore' not found" for any module that depends on them, directly or
// transitively. This predates TTC-67: :sdk:features:teetimes has always had it.
//
// The tests in these modules are platform-agnostic Kotlin with no expect/actual, so running them
// on the JVM gives equivalent coverage. Disabling the iOS test tasks is what keeps `./gradlew
// build` meaningful — otherwise it fails for a reason unrelated to the code under test, and a real
// failure would be indistinguishable from this one.
//
// Any module that gains tests and depends on GitLive Firebase will need the same block. The fix is
// to stop depending on GitLive in shared code, at which point all of this can go.
tasks.matching {
    (it.name.startsWith("link") && it.name.contains("TestIos")) ||
        it.name.matches(Regex("ios[A-Za-z0-9]*Test"))
}.configureEach {
    enabled = false
}
