package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bradball.teetimecaddie.android.feature.auth.navigation.authEntries
import net.bradball.teetimecaddie.android.feature.auth.navigation.navigateToAuthentication
import net.bradball.teetimecaddie.android.feature.auth.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.auth.navigation.navigateToWelcome
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.navigateToTeeTimesList
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesEntries
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.AnimatedLoadingScrim
import net.bradball.teetimecaddie.android.ui.common.modifiers.blur
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination
import net.bradball.teetimecaddie.android.ui.navigation.rememberNavigator

@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
    val initStatus by appState.appInitStatus.collectAsStateWithLifecycle()
    val isLoggedIn by appState.isLoggedIn.collectAsStateWithLifecycle()

    val showLoadingScrim = remember(initStatus) { initStatus == InitializationState.Pending}
    val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            navigator.navigateToAuthentication(appState.hasLoggedInOnce)
        }
    }

    if (initStatus is InitializationState.Failed) {
        AppErrorScreen(initStatus as InitializationState.Failed)
    } else {
        TtcNavDisplay(
            navigator = navigator,
            modifier = Modifier.blur(enabled = showLoadingScrim)
        )
        AnimatedLoadingScrim(isVisible = showLoadingScrim)
    }
}


/**
 * The primary navigation display component for the TeeTime Caddie app.
 *
 * `TtcNavDisplay` wraps the androidx.navigation3 [NavDisplay] component and configures it with
 * all navigation entries, entry decorators, and back navigation handling for the entire app.
 * This is the central point where all feature module navigation entries are registered and
 * wired together.
 *
 * ## Responsibilities
 *
 * This composable:
 * - Registers all navigation entry definitions from feature modules
 * - Wires up navigation callbacks between features
 * - Configures entry decorators for state preservation and ViewModel scoping
 * - Handles system back button navigation
 *
 * ## Navigation Entry Registration
 *
 * Navigation entries are defined in feature module extension functions and registered here
 * via the `entryProvider` parameter. Each feature module provides its own entry definitions:
 *
 * ```kotlin
 * entryProvider = entryProvider {
 *     // Tee Times feature entries
 *     teeTimesEntries(navigator)
 *
 *     // Auth feature entries with callbacks
 *     authEntries(
 *         onLoginClick = navigator::navigateToLogin,
 *         onLoggedIn = { navigator.navigateToTeeTimesList(true) }
 *     )
 * }
 * ```
 *
 * ## Entry Decorators
 *
 * Entry decorators enhance navigation entries with additional functionality:
 *
 * - **SaveableStateHolder**: Preserves Compose state across navigation (e.g., scroll position,
 *   text field values) when navigating away and back to a destination
 * - **ViewModelStore**: Provides proper ViewModel scoping per navigation entry, ensuring
 *   ViewModels survive configuration changes but are cleared when the entry is removed from
 *   the back stack
 *
 * ## Navigation Callback Wiring
 *
 * This is where navigation callbacks are connected between features. The Navigator instance
 * is used to create lambda callbacks that are passed to feature entry definitions:
 *
 * ```kotlin
 * authEntries(
 *     onLoginClick = navigator::navigateToLogin,          // Method reference
 *     onLoggedIn = { navigator.navigateToTeeTimesList(true) }  // Lambda wrapper
 * )
 * ```
 *
 * **Important**: Screen composables should never receive the Navigator instance directly.
 * They should only receive these lambda callbacks, which are defined here and passed through
 * the entry definitions.
 *
 * ## Usage
 *
 * This composable is typically used once in the app's root composable:
 *
 * ```kotlin
 * @Composable
 * fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
 *     val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)
 *
 *     TtcNavDisplay(
 *         navigator = navigator,
 *         modifier = Modifier.blur(enabled = showLoadingScrim)
 *     )
 * }
 * ```
 *
 * @param navigator The Navigator instance that manages navigation state and back stacks.
 * @param modifier Optional modifier to apply to the navigation display (e.g., for loading overlays).
 *
 * @see Navigator
 * @see NavDisplay
 * @see authEntries
 * @see teeTimesEntries
 */
@Composable
fun TtcNavDisplay(navigator: Navigator, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.weight(1F),
            onBack = { navigator.goBack() },
            entryProvider = entryProvider {
                teeTimesEntries(navigator)
                authEntries(
                    onLoginClick = navigator::navigateToLogin,
                    onRegisterClick = navigator::navigateToRegistration,
                    onLoggedIn = { navigator.navigateToTeeTimesList(true) },
                    onRegistrationComplete = navigator::navigateToWelcome,
                    onWelcomeClosed = { navigator.navigateToTeeTimesList(true) }
                )
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            )
        )
    }
}