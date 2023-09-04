package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.features.auth.AuthRepository

@Composable
fun rememberTeeTimeCaddieAppState(
    appInitializers: AppInitializers,
    authRepository: AuthRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
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

    val isLoggedIn = authRepository.loginState
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = authRepository.isLoggedIn
        )

    val hasRegistered: Boolean
        get() = authRepository.hasLoggedInOnce

    val appInitStatus = appInitializers.state
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InitializationState.Pending
        )
}