package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesGraphRoute
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeEntry.TeeTimeEntryRoute
import net.bradball.teetimecaddie.android.initializers.InitializationState
import net.bradball.teetimecaddie.android.ui.common.AnimatedLoadingScrim
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.common.modifiers.blur
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestinationResources
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavHost
import net.bradball.teetimecaddie.android.ui.navigation.topLevelResources

@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
    val initStatus by appState.appInitStatus.collectAsStateWithLifecycle()
    val isLoggedIn by appState.isLoggedIn.collectAsStateWithLifecycle()

    val showLoadingScrim = remember(initStatus) { initStatus == InitializationState.Pending}

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            appState.navigateToAuthentication()
        }
    }

    if (initStatus is InitializationState.Failed) {
        AppErrorScreen(initStatus as InitializationState.Failed)
    } else {
        TtcNavHost(
            navController = appState.navController,
            startDestination = teeTimesGraphRoute,
            modifier = Modifier.blur(enabled = showLoadingScrim)
        )
        AnimatedLoadingScrim(isVisible = showLoadingScrim)
    }
}