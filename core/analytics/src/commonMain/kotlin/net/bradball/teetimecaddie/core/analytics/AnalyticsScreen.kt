package net.bradball.teetimecaddie.core.analytics

enum class ScreenType(val displayName: String) {
    SCREEN("Screen"),
    MODAL_DIALOG("Modal"),
    BOTTOM_SHEET("Bottom Sheet")
}

sealed class AnalyticsScreen(val name: String, val parameters: Map<String, String> = mapOf()) {
    object None: AnalyticsScreen("")
    object Registration: AnalyticsScreen(name = "Registration")
    object Login: AnalyticsScreen(name = "Login")
}