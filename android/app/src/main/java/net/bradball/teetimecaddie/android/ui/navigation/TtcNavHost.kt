package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.google.android.material.transition.platform.MaterialSharedAxis
import net.bradball.teetimecaddie.android.analytics.FirebaseInitializer
import net.bradball.teetimecaddie.android.analytics.LocalEventManager
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.loginScreen
import net.bradball.teetimecaddie.android.feature.auth.login.navigation.navigateToLogin
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.navigateToRegistration
import net.bradball.teetimecaddie.android.feature.auth.registration.navigation.registrationGraph
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.navigateToWelcome
import net.bradball.teetimecaddie.android.feature.auth.welcome.navigation.welcomeScreen
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.homeRoute
import net.bradball.teetimecaddie.android.ui.homeScreen
import net.bradball.teetimecaddie.android.ui.navigateToHome
import net.bradball.teetimecaddie.core.analytics.EventManager

@Composable
fun TtcNavHost(
    navController: NavHostController,
    onShowSnackbar: suspend (String, String?)->SnackbarResult,
    modifier: Modifier = Modifier,
    startDestination: String = homeRoute) {

    navController.popBackStack(1, false)


    NavHost(
        enterTransition = { fadeIn(tween(easing = EaseIn))+ slideInHorizontally { width -> width / 4 } },
        exitTransition = { fadeOut() + slideOutHorizontally { width -> -width / 4 } },
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
            onLoggedIn = { navController.navigateToHome() },
            onShowSnackbar = onShowSnackbar
        )

    }
}