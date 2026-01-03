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
import net.bradball.teetimecaddie.features.auth.AuthRepository

@Composable
fun rememberTeeTimeCaddieAppState(
    appInitializers: AppInitializers,
    authRepository: AuthRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): TeeTimeCaddieAppState {
    return remember(coroutineScope, appInitializers, authRepository) {
        TeeTimeCaddieAppState(coroutineScope, appInitializers, authRepository)
    }
}

class TeeTimeCaddieAppState(
    coroutineScope: CoroutineScope,
    appInitializers: AppInitializers,
    private val authRepository: AuthRepository
) {
    val hasLoggedInOnce: Boolean
        get() = authRepository.hasLoggedInOnce

    val isLoggedIn = authRepository.loginState
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = authRepository.isLoggedIn
        )

    val appInitStatus = appInitializers.state
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InitializationState.Pending
        )
}