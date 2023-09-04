package net.bradball.teetimecaddie.android.feature.auth.welcome.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import net.bradball.teetimecaddie.android.feature.auth.welcome.WelcomeRoute
import net.bradball.teetimecaddie.android.ui.navigateToHome
import net.bradball.teetimecaddie.android.ui.navigation.clearBackStack

const val welcomeRoute = "welcome"

/**
 * Navigate to the Welcome Screen
 *
 * This method will first clear the backstack and navigate to home,
 * and then navigate to the Welcome Screen. If the user gestures "back"
 * from the welcome screen, they will wind up on the home screen
 */
fun NavHostController.navigateToWelcome(navOptions: NavOptions? = null) {
    // navigate to home first, clearing the back stack.
    this.navigateToHome(navOptions = clearBackStack(this))

    // Then navigate to welcome
    this.navigate(welcomeRoute, navOptions)
}

fun NavGraphBuilder.welcomeScreen(onClose: ()->Unit) {
    composable(route = welcomeRoute) {
        WelcomeRoute(onClose = onClose)
    }
}