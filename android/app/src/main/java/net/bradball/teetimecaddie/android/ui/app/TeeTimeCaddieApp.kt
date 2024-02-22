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
import net.bradball.teetimecaddie.android.ui.common.appBars.TtcCenteredTopAppBar
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavHost
import net.bradball.teetimecaddie.android.ui.navigation.topLevelResources

@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {

    val snackbarHostState = remember { SnackbarHostState() }

    val isLoggedIn by appState.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            appState.navigateToAuthentication()
        }
    }

    // We don't want the top level scaffold to consume any insets
    // because that would prevent a screen that wants to draw under
    // the system bars from doing so. So we create a set of insets
    // with zero values, and give that to the scaffold's contentWindowInsets.
    // Any TopAppBars or Bottom bars should have inherent handling of insets,
    // so everything should be ok if we use those.
    // Any screen that does NOT have a top app bar will be responsible for
    // Handling it's own top insets.
    val contentInsets = WindowInsets(0,0,0,0)

    Scaffold(
        contentWindowInsets = contentInsets,
        floatingActionButton = {
            appState.destinationResources?.fabIcon?.let { icon ->
                FloatingActionButton(onClick = appState::onFabClicked) {
                    Icon(icon, contentDescription = "")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TtcAppBar(appState.currentDestination) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                //.consumeWindowInsets(padding)
        ) {
            TtcNavHost(
                navController = appState.navController,
                startDestination = teeTimesGraphRoute,
                onShowSnackbar = { message, action ->
                    snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = action,
                        duration = SnackbarDuration.Long,
                        withDismissAction = true
                    )
                },
            )

            appState.modelSheetContent?.let { sheetContent ->
                // See Spacer below for info on insets and bottom padding
                ModalBottomSheet(
                    windowInsets = contentInsets,
                    sheetState = appState.modalSheetState,
                    onDismissRequest = { appState.onModalBottomSheetDismissed() }) {
                    when (sheetContent) {
                        AppModalSheetContent.ADD_TEE_TIME -> TeeTimeEntryRoute(
                            onTeeTimeAdded = appState::closeModalBottomSheet
                        )
                    }

                    //This is necessary due to a defect with ModalBottomSheet and padding modifiers.
                    //Since the sheet won't apply padding, we'll add our own spacer for it.
                    //More info: https://issuetracker.google.com/issues/274872542 & https://issuetracker.google.com/issues/275849044
                    Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
                }
            }
        }
    }

}

@Composable
private fun TtcAppBar(destination: NavDestination?) {
    destination?.topLevelResources?.let { resources ->
        TtcCenteredTopAppBar(title = stringResource(id = resources.titleTextId))
    }
}