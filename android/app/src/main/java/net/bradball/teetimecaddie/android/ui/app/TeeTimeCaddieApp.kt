package net.bradball.teetimecaddie.android.ui.app

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.registrationRoute
import net.bradball.teetimecaddie.android.initializers.AppInitializers
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.homeRoute
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavHost
import net.bradball.teetimecaddie.features.auth.AuthRepository

@Composable
fun TeeTimeCaddieApp(appState: TeeTimeCaddieAppState) {
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    val isLoggedIn by appState.isLoggedIn.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            if (appState.hasRegistered) {
                navController.navigateToLogin()
            } else {
                navController.navigateToRegistration()
            }
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {

            TtcNavHost(
                navController = navController,
                onShowSnackbar = { message, action ->
                  snackbarHostState.showSnackbar(
                      message = message,
                      actionLabel = action,
                      duration = SnackbarDuration.Long,
                      withDismissAction = true
                  )
                },
                startDestination = homeRoute)

        }

    }
}