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
    modalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
): TeeTimeCaddieAppState {
    return remember(coroutineScope, appInitializers, authRepository) {
        TeeTimeCaddieAppState(coroutineScope, modalSheetState, appInitializers, navController, authRepository)
    }
}

class TeeTimeCaddieAppState(
    private val coroutineScope: CoroutineScope,
    val modalSheetState: SheetState,
    appInitializers: AppInitializers,
    val navController: NavHostController,
    private val authRepository: AuthRepository
) {
    val currentDestination: NavDestination?
        @Composable get() =
            navController.currentBackStackEntryAsState().value?.destination

    val destinationResources: TtcDestinationResources?
        @Composable get() = currentDestination?.resources


    var modelSheetContent: AppModalSheetContent? by mutableStateOf(null)
        private set

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

    fun onFabClicked() {
        modelSheetContent = when (navController.currentBackStackEntry?.destination?.route) {
            teeTimesListRoute -> AppModalSheetContent.ADD_TEE_TIME
            else -> null
        }
        modelSheetContent?.let {
            coroutineScope.launch {
                delay(1_000)
                modalSheetState.show()
            }
        }
    }

    fun onModalBottomSheetDismissed() {
        modelSheetContent = null
    }

    fun closeModalBottomSheet() {
        coroutineScope.launch {
            modalSheetState.hide()
        }.invokeOnCompletion {
            onModalBottomSheetDismissed()
        }
    }
}