package net.bradball.teetimecaddie.core.analytics

/**
 * Represents the different types of screens that can be tracked in analytics.
 *
 * This enum categorizes screens by their presentation style, which can be useful
 * for filtering and analyzing user navigation patterns.
 *
 * @property displayName The human-readable name of the screen type
 */
enum class ScreenType(val displayName: String) {
    /** A full-screen view */
    SCREEN("Screen"),
    /** A modal dialog that overlays other content */
    MODAL_DIALOG("Modal"),

    /** A bottom sheet that slides up from the bottom of the screen */
    BOTTOM_SHEET("Bottom Sheet")
}

/**
 * Base class for defining screen views to be tracked in analytics.
 *
 * Each screen in the application should have a corresponding `AnalyticsScreen` subclass
 * that defines its name and any relevant parameters. When a user navigates to a screen,
 * create an instance of the appropriate subclass and pass it to `EventManager.logScreenView()`.
 *
 * ## Creating Screen Definitions
 *
 * Define screen classes as subclasses of `AnalyticsScreen`:
 *
 * ```kotlin
 * class MyScreen(viewName: String, userId: String? = null) : AnalyticsScreen(
 *     name = "MyScreen",
 *     viewName = viewName,
 *     parameters = buildMap {
 *         userId?.let { put("user_id", it) }
 *     }
 * )
 * ```
 *
 * ## Logging Screen Views
 *
 * In your UI code, create and log the screen when it appears:
 *
 * ```kotlin
 * val screen = MyScreen(viewName = "my_screen_view", userId = currentUserId)
 * eventManager.logScreenView(screen)
 * ```
 *
 * @property name The logical name of the screen, typically matching the class or feature name
 * @property viewName The technical view identifier, often matching the UI component name
 * @property parameters Additional key-value pairs providing context about the screen view
 */
sealed class AnalyticsScreen(val name: String, val viewName: String, val parameters: Map<String, String> = mapOf()) {

    /** Represents no screen. Use this for screens that should not be logged as screen views. */
    object None: AnalyticsScreen("", viewName = "")

    /** The user registration screen */
    class Registration(viewName: String): AnalyticsScreen(name = "Registration", viewName)

    /** The user login screen */
    class Login(viewName: String): AnalyticsScreen(name = "Login", viewName)

    /** The list view showing available tee times */
    class TeeTimeList(viewName: String): AnalyticsScreen(name = "TeeTimeList", viewName)

    /** The screen for adding a new tee time */
    class AddTeeTime(viewName: String): AnalyticsScreen(name = "AddTeeTime", viewName)
}