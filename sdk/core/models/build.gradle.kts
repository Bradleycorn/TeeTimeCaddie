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
            implementation(libs.crashkios)
            implementation(libs.kotlinx.datetime)
            api(libs.mokoresources.api)
            implementation(project(":sdk:core:analytics"))
            implementation(project(":sdk:core:extensions"))
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
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
}

multiplatformResources {
    resourcesPackage.set("net.bradball.teetimecaddie.core.models")
    resourcesClassName.set("GR")
    iosBaseLocalizationRegion = "en"
}

//tasks.matching { it.name == "kspKotlinIosX64" }.configureEach {
//    dependsOn(tasks.getByName("generateMRiosX64Main"))
//}
//tasks.matching { it.name == "kspKotlinIosArm64" }.configureEach {
//    dependsOn(tasks.getByName("generateMRiosArm64Main"))
//}
//tasks.matching { it.name == "kspKotlinIosSimulatorArm64" }.configureEach {
//    dependsOn(tasks.getByName("generateMRiosSimulatorArm64Main"))
//}
