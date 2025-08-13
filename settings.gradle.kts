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
include(":businessLogic")
include(":core:network")
include(":core:analytics")
include(":core:extensions")
include(":features:auth")
include(":core:storage")
include(":core:models")
include(":features:teetimes")
