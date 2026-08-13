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
            implementation(project(":sdk:core:extensions"))
            implementation(project(":sdk:core:analytics"))
            implementation(project(":sdk:core:models"))
            implementation(project(":sdk:core:storage"))
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
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

multiplatformResources {
    resourcesPackage.set("net.bradball.teetimecaddie.features.auth")
    resourcesClassName.set("AR")
    iosBaseLocalizationRegion = "en"
}