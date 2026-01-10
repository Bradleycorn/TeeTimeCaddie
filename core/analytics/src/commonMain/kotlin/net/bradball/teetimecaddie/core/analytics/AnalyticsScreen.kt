package net.bradball.teetimecaddie.core.analytics

enum class ScreenType(val displayName: String) {
    SCREEN("Screen"),
    MODAL_DIALOG("Modal"),
    BOTTOM_SHEET("Bottom Sheet")
}

sealed class AnalyticsScreen(val name: String, val viewName: String, val parameters: Map<String, String> = mapOf()) {
    object None: AnalyticsScreen("", viewName = "")
    class Registration(viewName: String): AnalyticsScreen(name = "Registration", viewName)
    class Login(viewName: String): AnalyticsScreen(name = "Login", viewName)
    class TeeTimeList(viewName: String): AnalyticsScreen(name = "TeeTimeList", viewName)
}