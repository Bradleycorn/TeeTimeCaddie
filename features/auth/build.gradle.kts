
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
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
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
            implementation(libs.firebase.mpp.auth)
            implementation(libs.kermit.core)
            api(libs.mokoresources.api)
            implementation(project(":core:extensions"))
            implementation(project(":core:analytics"))
            implementation(project(":core:models"))
            implementation(project(":core:storage"))
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
dependencies {
    implementation("com.google.firebase:firebase-auth-ktx:22.0.0")
}

multiplatformResources {
    resourcesPackage.set("net.bradball.teetimecaddie.features.auth")
    resourcesClassName.set("AR")
    iosBaseLocalizationRegion = "en"
}