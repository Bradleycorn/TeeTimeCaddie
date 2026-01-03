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

@Composable
fun TtcNavDisplay(navigator: Navigator, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.weight(1F),
            onBack = { navigator.goBack() },
            entryProvider = entryProvider {
                teeTimesEntries()
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