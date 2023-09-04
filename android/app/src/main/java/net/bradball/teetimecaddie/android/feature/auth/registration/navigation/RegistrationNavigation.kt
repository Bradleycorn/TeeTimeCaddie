package net.bradball.teetimecaddie.android.feature.auth.registration.navigation

import androidx.compose.material3.SnackbarResult
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import net.bradball.teetimecaddie.android.feature.auth.registration.RegistrationRoute
import net.bradball.teetimecaddie.android.ui.navigation.clearBackStack

const val registrationGraphRoute = "registration"
const val registrationRoute = "register"

fun NavHostController.navigateToRegistration(navOptions: NavOptions? = clearBackStack(this)) {
    this.navigate(registrationGraphRoute, navOptions)
}

fun NavGraphBuilder.registrationGraph(
    onLoginClick: () -> Unit,
    onRegistrationComplete: ()->Unit,
    onShowSnackbar: suspend (String, String?) -> SnackbarResult,
    nestedDestinations: NavGraphBuilder.()->Unit = {}
) {
    navigation(
        route = registrationGraphRoute,
        startDestination = registrationRoute
    ) {
        composable(route = registrationRoute) {
            RegistrationRoute(onLoginClick, onRegistrationComplete, onShowSnackbar)
        }

        nestedDestinations()
    }
}