@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    kotlin("multiplatform")
    id("com.android.library")
    alias(libs.plugins.mokoresources)
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
        val commonMain by getting {
            dependencies {
                implementation(libs.crashkios)
                api(libs.mokoresources.api)
                implementation(project(":core:analytics"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.mokoresources.test)
            }
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
