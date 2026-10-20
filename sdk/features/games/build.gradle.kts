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
            implementation(libs.kermit.core)
            api(libs.mokoresources.api)
            implementation(libs.kotlinx.datetime)
            implementation(project(":sdk:core:extensions"))
            implementation(project(":sdk:core:analytics"))
            implementation(project(":sdk:core:models"))
            implementation(project(":sdk:core:storage"))
            // GameDocument.createdAt is a GitLive server timestamp, and building a document means
            // the type has to resolve here too. Nothing in this module talks to Firestore itself.
            implementation(libs.firebase.mpp.firestore)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mokoresources.test)
        }

        androidMain {
            kotlin.srcDir("build/generated/moko/androidMain/src")
        }
    }
}

android {
    namespace = "net.bradball.teetimecaddie.features.games"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests {
            // The repository tests build an EventManager, whose Kermit logger reaches
            // android.os.Process on construction. They exercise game logic, not Android behaviour,
            // so let the unmocked framework calls return defaults rather than throwing.
            isReturnDefaultValues = true
        }
    }
}

multiplatformResources {
    resourcesPackage.set("net.bradball.teetimecaddie.features.games")
    resourcesClassName.set("GMR")
    iosBaseLocalizationRegion = "en"
}

// iOS test binaries cannot be linked in this project: GitLive's Firebase modules bake
// `-framework FirebaseCore` into their linker options, and those frameworks only exist inside the
// iOS app's SPM checkout. See the same block in sdk/build.gradle.kts for the full story. These
// tests are platform-agnostic, so the JVM run gives equivalent coverage.
tasks.matching {
    (it.name.startsWith("link") && it.name.contains("TestIos")) ||
        it.name.matches(Regex("ios[A-Za-z0-9]*Test"))
}.configureEach {
    enabled = false
}
