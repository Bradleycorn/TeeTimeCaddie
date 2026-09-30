package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.AnimatedLoadingScrim
import net.bradball.teetimecaddie.android.ui.common.modifiers.blur
import net.bradball.teetimecaddie.session.SessionState

/**
 * The root of the app's UI.
 *
 * The one structural decision here is that **auth and the tabs are alternatives, not destinations**.
 * The app branches on [SessionState] rather than navigating to a login screen, so there is no route
 * by which a signed-out person's Games back stack can survive underneath the credentials screen,
 * and no `LaunchedEffect` racing the session to push one.
 *
 * Each branch owns its own navigator, created inside its own display composable:
 * [NavBarNavDisplay] for the signed-in tabs, [AuthNavDisplay] for the auth flow. Both are written
 * against the same `Navigator` interface, so a feature's navigation looks the same either side of
 * the branch.
 *
 * The snackbar host sits **above** that branch, because the confirmation for "you are signed in" is
 * produced by a screen that signing in immediately destroys. See `TtcMessenger`.
 */
@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
    val initStatus by appState.appInitStatus.collectAsStateWithLifecycle()
    val sessionState by appState.sessionState.collectAsStateWithLifecycle()

    val showLoadingScrim = initStatus == InitializationState.Pending || sessionState is SessionState.Loading
    val snackbarHostState = remember { SnackbarHostState() }

    // Read through rememberUpdatedState so the collector is keyed on the flow alone: re-subscribing
    // on every configuration change would drop a message emitted mid-rotation.
    val resources by rememberUpdatedState(LocalResources.current)

    LaunchedEffect(appState.messages) {
        appState.messages.collect { message ->
            snackbarHostState.showSnackbar(
                message = resources.getString(message.text.resourceId, *message.args.toTypedArray()),
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
            is SessionState.SignedIn -> NavBarNavDisplay(modifier = contentModifier)

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
