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
            implementation(libs.crashkios)
            implementation(libs.kotlinx.datetime)
            api(libs.mokoresources.api)
            implementation(project(":core:analytics"))
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
    namespace = "net.bradball.teetimecaddie.core.models"
    compileSdk = 33
    defaultConfig {
        minSdk = 24
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "net.bradball.teetimecaddie.core.models"
    multiplatformResourcesClassName = "MR"
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
