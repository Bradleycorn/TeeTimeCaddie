package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.loginScreen
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.registrationGraph
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.navigateToWelcome
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.welcomeScreen
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.navigateToTeeTimes
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesGraph
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesGraphRoute

@Composable
fun TtcNavHost(
    navController: NavHostController,
    onShowSnackbar: suspend (String, String?)->SnackbarResult,
    modifier: Modifier = Modifier,
    startDestination: String = teeTimesGraphRoute) {

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = { fadeIn(tween(easing = EaseIn))+ slideInHorizontally { width -> width / 4 } },
        exitTransition = { fadeOut() + slideOutHorizontally { width -> -width / 4 } },
    ) {

        teeTimesGraph(onShowSnackbar = onShowSnackbar) {

        }

        registrationGraph(
            onLoginClick = { navController.navigateToLogin() },
            onRegistrationComplete = { navController.navigateToWelcome() },
            onShowSnackbar = onShowSnackbar
        ) {
            welcomeScreen(
                onClose = {
                    navController.navigateToTeeTimes(clearBackStack = true)
                }
            )
        }

        loginScreen(
            onRegisterClick = { navController.navigateToRegistration() },
            onLoggedIn = { navController.navigateToTeeTimes() },
            onShowSnackbar = onShowSnackbar
        )
    }
}