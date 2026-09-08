pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TeeTimeCaddie"
include(":android:app")
include(":sdk")
include(":sdk:core:network")
include(":sdk:core:analytics")
include(":sdk:core:extensions")
include(":sdk:features:auth")
include(":sdk:core:storage")
include(":sdk:core:models")
include(":sdk:features:teetimes")
