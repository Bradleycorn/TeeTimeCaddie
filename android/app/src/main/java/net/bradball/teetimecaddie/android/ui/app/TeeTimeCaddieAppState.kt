package net.bradball.teetimecaddie.android.ui.app

import android.util.Log
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.auth.AuthModule
import net.bradball.teetimecaddie.android.feature.auth.AuthModule_ProvideAuthRepositoryFactory
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesListRoute
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.navigation.TtcDestinationResources
import net.bradball.teetimecaddie.android.ui.navigation.resources
import net.bradball.teetimecaddie.features.auth.AuthRepository

@Composable
fun rememberTeeTimeCaddieAppState(
    appInitializers: AppInitializers,
    authRepository: AuthRepository,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController(),
): TeeTimeCaddieAppState {
    return remember(coroutineScope, appInitializers, authRepository) {
        TeeTimeCaddieAppState(coroutineScope, navController, appInitializers, authRepository)
    }
}

class TeeTimeCaddieAppState(
    coroutineScope: CoroutineScope,
    val navController: NavHostController,
    appInitializers: AppInitializers,
    private val authRepository: AuthRepository
) {
    val currentDestination: NavDestination?
        @Composable get() =
            navController.currentBackStackEntryAsState().value?.destination

    val destinationResources: TtcDestinationResources?
        @Composable get() = currentDestination?.resources

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

    fun navigateToAuthentication() {
        when {
            authRepository.hasLoggedInOnce -> navController.navigateToLogin()
            else -> navController.navigateToRegistration()
        }
    }
}