package net.bradball.teetimecaddie.android.feature.auth.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import net.bradball.teetimecaddie.android.feature.auth.login.LoginRoute
import net.bradball.teetimecaddie.android.ui.navigation.clearBackStack

const val loginRoute = "login"

fun NavHostController.navigateToLogin(navOptions: NavOptions? = clearBackStack(this)) {
    this.navigate(loginRoute, navOptions)
}

fun NavGraphBuilder.loginScreen(onRegisterClick: ()->Unit, onLoggedIn: ()->Unit) {
    composable(route = loginRoute) {
        LoginRoute(onRegisterClick = onRegisterClick, onLoggedIn = onLoggedIn)
    }
}