import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
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
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.firebase.mpp.auth)
            implementation(libs.kermit.core)
            api(libs.mokoresources.api)
            implementation(project(":sdk:core:extensions"))
            implementation(project(":sdk:core:analytics"))
            implementation(project(":sdk:core:models"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.mokoresources.test)
        }

        androidMain {
            kotlin.srcDir("build/generated/moko/androidMain/src")
        }
    }
}

android {
    namespace = "net.bradball.teetimecaddie.features.auth"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}
dependencies {
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
}

multiplatformResources {
    resourcesPackage.set("net.bradball.teetimecaddie.features.auth")
    resourcesClassName.set("AR")
    iosBaseLocalizationRegion = "en"
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
