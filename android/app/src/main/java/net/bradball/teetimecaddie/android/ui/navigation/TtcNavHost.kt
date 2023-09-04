package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.loginScreen
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.registrationGraph
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.navigateToWelcome
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.welcomeScreen
import net.bradball.teetimecaddie.android.ui.homeRoute
import net.bradball.teetimecaddie.android.ui.homeScreen
import net.bradball.teetimecaddie.android.ui.navigateToHome

@Composable
fun TtcNavHost(
    navController: NavHostController,
    onShowSnackbar: suspend (String, String?)->SnackbarResult,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute) {

    navController.popBackStack(1, false)


    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier) {

        homeScreen()

        registrationGraph(
            onLoginClick = { navController.navigateToLogin() },
            onRegistrationComplete = { navController.navigateToWelcome() },
            onShowSnackbar = onShowSnackbar
        ) {
            welcomeScreen(
                onClose = { navController.navigateToHome() }
            )
        }

        loginScreen(
            onRegisterClick = { navController.navigateToRegistration() },
            onLoggedIn = { navController.navigateToHome() }
        )

    }
}