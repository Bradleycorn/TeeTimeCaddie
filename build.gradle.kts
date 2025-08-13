@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version(libs.versions.agp).apply(false)
    id("com.android.library").version(libs.versions.agp).apply(false)
    kotlin("android").version(libs.versions.kotlin).apply(false)
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.mokoresources).apply(false)
    alias(libs.plugins.skie).apply(false)
}

allprojects {
    group = "net.bradball.teetimecaddie"
    version = System.getenv("PUBLISH_VERSION") ?: "0.0.12"
}

buildscript {
    dependencies {
        classpath(libs.google.gms.services)
        classpath(libs.firebase.plugins.crashlytics)
        classpath(libs.firebase.plugins.performance)
        classpath(libs.mokoresources.classpath)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

//tasks.getByName(":features:auth:kspKotlinIosX64")
//    .dependsOn(":features:auth:generateMRiosX64Main")