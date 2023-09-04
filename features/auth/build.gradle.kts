import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
    alias(libs.plugins.ksp)
    alias(libs.plugins.nativecoroutines)
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
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines)
                implementation(libs.firebase.mpp.auth)
                api(libs.mokoresources.api)
                implementation(project(":core:analytics"))
                implementation(project(":core:models"))
                implementation(project(":core:storage"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mokoresources.test)
            }
        }

        val androidMain by getting

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
    multiplatformResourcesPackage = "net.bradball.teetimecaddie.features.auth"
    multiplatformResourcesClassName = "AR"
    iosBaseLocalizationRegion = "en"
}

tasks.matching { it.name == "kspKotlinIosX64" }.configureEach {
    dependsOn(tasks.getByName("generateMRiosX64Main"))
}
tasks.matching { it.name == "kspKotlinIosArm64" }.configureEach {
    dependsOn(tasks.getByName("generateMRiosArm64Main"))
}
tasks.matching { it.name == "kspKotlinIosSimulatorArm64" }.configureEach {
    dependsOn(tasks.getByName("generateMRiosSimulatorArm64Main"))
}
