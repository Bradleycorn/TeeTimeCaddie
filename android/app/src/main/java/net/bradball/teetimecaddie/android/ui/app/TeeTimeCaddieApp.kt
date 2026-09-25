package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bradball.teetimecaddie.android.feature.profile.navigation.profileEntries
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesEntries
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.AnimatedLoadingScrim
import net.bradball.teetimecaddie.android.ui.common.modifiers.blur
import net.bradball.teetimecaddie.android.ui.common.navigation.TtcNavigationBar
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination
import net.bradball.teetimecaddie.android.ui.navigation.rememberNavigator
import net.bradball.teetimecaddie.session.SessionState

/**
 * The root of the app's UI.
 *
 * The one structural decision here is that **auth and the tabs are alternatives, not destinations**.
 * The app branches on [SessionState] rather than navigating to a login screen, so there is no route
 * by which a signed-out person's Games back stack can survive underneath the credentials screen,
 * and no `LaunchedEffect` racing the session to push one.
 *
 * The snackbar host sits **above** that branch for the same reason: the confirmation for "you are
 * signed in" is produced by a screen that signing in immediately destroys. See `TtcMessenger`.
 */
@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
    val initStatus by appState.appInitStatus.collectAsStateWithLifecycle()
    val sessionState by appState.sessionState.collectAsStateWithLifecycle()

    val showLoadingScrim = initStatus == InitializationState.Pending || sessionState is SessionState.Loading
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(appState.messages, context) {
        appState.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = context.getString(message.text.resourceId, *message.args.toTypedArray()),
                duration = SnackbarDuration.Short,
            )
        }
    }

    if (initStatus is InitializationState.Failed) {
        AppErrorScreen(initStatus as InitializationState.Failed)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val contentModifier = Modifier.blur(enabled = showLoadingScrim)

        when (sessionState) {
            is SessionState.SignedIn -> SignedInNavDisplay(modifier = contentModifier)

            // The profile step belongs to the auth flow: there is a Firebase account, but no
            // player yet, so there is nothing for the tabs to render.
            is SessionState.SignedOut,
            is SessionState.ProfileIncomplete ->
                AuthNavDisplay(sessionState = sessionState, modifier = contentModifier)

            // Seeded only when a session probably exists; the scrim covers it.
            is SessionState.Loading -> Unit
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        AnimatedLoadingScrim(isVisible = showLoadingScrim)
    }
}

/**
 * The tabbed, signed-in app: a [Navigator]-backed [NavDisplay] above the [TtcNavigationBar].
 *
 * Auth entries are deliberately absent — see [TeeTimeCaddieApp].
 */
@Composable
private fun SignedInNavDisplay(modifier: Modifier = Modifier) {
    val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)

    Column(modifier = modifier) {
        TtcNavDisplay(navigator = navigator, modifier = Modifier.weight(1F))

        TtcNavigationBar(
            destinations = navigator.topLevelDestinations,
            selected = navigator.selectedTopLevelDestination,
            onSelect = navigator::navigate,
        )
    }
}

/**
 * The primary navigation display for the signed-in app.
 *
 * `TtcNavDisplay` wraps the androidx.navigation3 [NavDisplay] component and configures it with
 * every feature's navigation entries, entry decorators, and back handling. This is the central
 * point where feature module navigation entries are registered and wired together.
 *
 * ## Navigation Entry Registration
 *
 * Navigation entries are defined in feature module extension functions and registered here via the
 * `entryProvider` parameter:
 *
 * ```kotlin
 * entryProvider = entryProvider {
 *     teeTimesEntries(navigator)
 *     profileEntries(navigator)
 * }
 * ```
 *
 * Auth is **not** among them. It is not a place the signed-in app can navigate to; it is the other
 * branch of [TeeTimeCaddieApp], hosted by [AuthNavDisplay].
 *
 * ## Entry Decorators
 *
 * - **SaveableStateHolder**: preserves Compose state (scroll position, text field values) across
 *   navigating away and back.
 * - **ViewModelStore**: scopes ViewModels per navigation entry, so they survive configuration
 *   changes but are cleared when the entry leaves the back stack.
 *
 * ## Navigation Callback Wiring
 *
 * This is where navigation callbacks are connected between features. **Screen composables never
 * receive the Navigator** — they receive the lambdas built here and passed through the entry
 * definitions.
 *
 * @param navigator The Navigator instance that manages navigation state and back stacks.
 * @param modifier Optional modifier to apply to the navigation display.
 *
 * @see Navigator
 * @see NavDisplay
 * @see teeTimesEntries
 * @see profileEntries
 */
@Composable
fun TtcNavDisplay(navigator: Navigator, modifier: Modifier = Modifier) {
    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            teeTimesEntries(navigator)
            profileEntries(navigator)
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    )
}
