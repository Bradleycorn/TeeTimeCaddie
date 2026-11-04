@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version(libs.versions.agp).apply(false)
    id("com.android.library").version(libs.versions.agp).apply(false)
    kotlin("android").version(libs.versions.kotlin).apply(false)
    kotlin("multiplatform").version(libs.versions.kotlin).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    alias(libs.plugins.hilt).apply(false)
    alias(libs.plugins.mokoresources).apply(false)
    alias(libs.plugins.skie).apply(false)
    alias(libs.plugins.kover)
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

// Merged coverage for the shared SDK and the Android app: `./gradlew koverHtmlReportCoverage`.
//
// Each module maps its Android `debug` unit tests into a variant named "coverage", and the empty
// variant of the same name here merges them. Shared code is measured on the JVM because the iOS
// test binaries cannot be linked (see sdk/build.gradle.kts). Every module is listed, tests or not,
// so untested code shows as 0% rather than disappearing from the report.
dependencies {
    kover(project(":android:app"))
    kover(project(":sdk"))
    kover(project(":sdk:core:analytics"))
    kover(project(":sdk:core:extensions"))
    kover(project(":sdk:core:models"))
    kover(project(":sdk:core:network"))
    kover(project(":sdk:core:storage"))
    kover(project(":sdk:features:auth"))
    kover(project(":sdk:features:teetimes"))
}

kover {
    currentProject {
        createVariant("coverage") {}
    }
    reports {
        filters {
            excludes {
                // Compose UI never runs in unit tests; screens and previews would only add 0%.
                annotatedBy("androidx.compose.runtime.Composable")
                classes("*.ComposableSingletons*")
                // Generated code: Hilt/Dagger, moko-resources, BuildConfig.
                classes(
                    "*_Factory",
                    "*_Factory\$*",
                    "*_Provide*Factory",
                    "*_MembersInjector",
                    "*Hilt_*",
                    "*_HiltModules*",
                    "*_GeneratedInjector",
                    "*_ComponentTreeDeps",
                    "dagger.hilt.*",
                    "hilt_aggregated_deps.*",
                )
                classes("*.MR", "*.MR\$*", "*.GR", "*.GR\$*", "*.AR", "*.AR\$*", "*.TTR", "*.TTR\$*")
                classes("*.BuildConfig")
            }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

//tasks.getByName(":sdk:features:auth:kspKotlinIosX64")
//    .dependsOn(":sdk:features:auth:generateMRiosX64Main")