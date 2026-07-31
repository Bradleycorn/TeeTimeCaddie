package net.bradball.teetimecaddie.android.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.auth.login.LoginScreen
import net.bradball.teetimecaddie.android.feature.auth.registration.RegistrationScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

/**
 * Navigation destination for the Login screen.
 *
 * Allows existing users to authenticate with their email and password.
 * This screen is shown when the user has previously logged in.
 */
@Serializable
data object LoginDestination: TtcNavKey

/**
 * Navigation destination for the Registration screen.
 *
 * Allows new users to create an account by providing their information.
 * This screen is shown for first-time users who haven't logged in before.
 */
@Serializable
data object RegistrationDestination: TtcNavKey

/**
 * Navigates to the appropriate authentication screen based on user history.
 *
 * This function implements smart routing to provide a better user experience:
 * - Existing users (who have logged in before) go directly to the Login screen
 * - New users go to the Registration screen first
 *
 * @param hasLoggedInOnce True if the user has successfully logged in at least once before.
 *                        This is typically stored in app preferences or settings.
 */
fun Navigator.navigateToAuthentication(hasLoggedInOnce: Boolean) {
    when {
        hasLoggedInOnce -> navigateToLogin()
        else -> navigateToRegistration()
    }
}

/**
 * Navigates to the Login screen.
 *
 * Clears the entire back stack to prevent users from navigating back to
 * the previous screen (typically used when the user's session has expired
 * or they've been logged out).
 */
fun Navigator.navigateToLogin() {
    navigate(LoginDestination, clearBackStack = true)
}

/**
 * Navigates to the Registration screen.
 *
 * Clears the entire back stack to ensure a clean navigation state for new users.
 * Users cannot navigate back from registration to any previous screens.
 */
fun Navigator.navigateToRegistration() {
    navigate(RegistrationDestination, clearBackStack = true)
}

/**
 * Registers all navigation entries for the Authentication feature.
 *
 * This function defines the navigation graph for the Authentication feature by registering
 * all screen composables and wiring up their navigation callbacks. The auth flow supports
 * multiple paths through the screens depending on user actions.
 *
 * ## Authentication Flow
 *
 * The authentication flow can follow different paths:
 *
 * **New User Flow**:
 * ```kotlin
 * Registration -> Tee Times List
 * ```
 *
 * **Returning User Flow**:
 * ```kotlin
 * Login -> Tee Times List
 * ```
 *
 * **User Switching Flow**:
 * ```kotlin
 * Registration -> Login -> Tee Times List
 * ```
 *
 * ## Navigation Callback Pattern
 *
 * Navigation callbacks are defined in [TtcNavDisplay] and passed to this function, which then
 * passes them to individual screen composables. Screens never receive the Navigator directly:
 *
 * ```kotlin
 * // In TtcNavDisplay
 * authEntries(
 *     onLoginClick = navigator::navigateToLogin,
 *     onRegisterClick = navigator::navigateToRegistration,
 *     onLoggedIn = { navigator.navigateToTeeTimesList(true) },
 *     onRegistrationComplete = navigator::navigateToWelcome,
 *     onWelcomeClosed = { navigator.navigateToTeeTimesList(true) }
 * )
 * ```
 *
 * This approach:
 * - Keeps screens decoupled from the navigation system
 * - Centralizes all navigation logic in the navigation files
 * - Makes screens testable without mocking navigation
 * - Allows screens to be reused in different contexts
 *
 * ## Usage
 *
 * This function is called from [TtcNavDisplay] during app initialization alongside
 * other feature entry definitions:
 *
 * ```kotlin
 * entryProvider = entryProvider {
 *     authEntries(
 *         onLoginClick = navigator::navigateToLogin,
 *         onRegisterClick = navigator::navigateToRegistration,
 *         onLoggedIn = { navigator.navigateToTeeTimesList(true) },
 *         onRegistrationComplete = navigator::navigateToWelcome,
 *         onWelcomeClosed = { navigator.navigateToTeeTimesList(true) }
 *     )
 *     teeTimesEntries(navigator)
 * }
 * ```
 *
 * @param onLoginClick Callback invoked when the user wants to switch from registration to login.
 * @param onRegisterClick Callback invoked when the user wants to switch from login to registration.
 * @param onLoggedIn Callback invoked after successful login, typically navigates to the main app.
 * @param onRegistrationComplete Callback invoked after successful registration, typically shows welcome screen.
 * @param onWelcomeClosed Callback invoked when the user dismisses the welcome screen, navigates to main app.
 *
 * @see LoginDestination
 * @see RegistrationDestination
 * @see WelcomeDestination
 * @see Navigator
 */
fun EntryProviderScope<NavKey>.authEntries(onLoginClick: ()->Unit, onRegisterClick: ()->Unit, onLoggedIn: ()->Unit, onRegistrationComplete: ()->Unit) {
    entry<LoginDestination> {
        LoginScreen(onRegisterClick = onRegisterClick, onLoggedIn = onLoggedIn )
    }

    entry<RegistrationDestination> {
        RegistrationScreen(onLoginClick = onLoginClick, onRegistrationComplete = onRegistrationComplete)
    }

}
