package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import kotlinx.coroutines.flow.map
import net.bradball.teetimecaddie.session.SessionManager
import net.bradball.teetimecaddie.session.SessionState

@Composable
fun rememberTeeTimeCaddieAppState(
    appInitializers: AppInitializers,
    sessionManager: SessionManager,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): TeeTimeCaddieAppState {
    return remember(coroutineScope, appInitializers, sessionManager) {
        TeeTimeCaddieAppState(coroutineScope, appInitializers, sessionManager)
    }
}

class TeeTimeCaddieAppState(
    coroutineScope: CoroutineScope,
    appInitializers: AppInitializers,
    private val sessionManager: SessionManager
) {
    // Transitional: TTC-79 replaces this boolean with a branch on SessionState, which is what
    // distinguishes "signed out" from "signed in but no profile yet". Until then the shell only
    // needs to know whether to show auth.
    val isLoggedIn = sessionManager.sessionState
        .map { it is SessionState.SignedIn }
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = sessionManager.initialSessionState is SessionState.SignedIn
        )

    val appInitStatus = appInitializers.state
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InitializationState.Pending
        )
}