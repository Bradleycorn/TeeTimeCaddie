package net.bradball.teetimecaddie.android.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.auth.login.LoginScreen
import net.bradball.teetimecaddie.android.feature.auth.registration.RegistrationScreen
import net.bradball.teetimecaddie.android.feature.auth.welcome.WelcomeScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

@Serializable
data object LoginDestination: TtcNavKey

@Serializable
data object RegistrationDestination: TtcNavKey

@Serializable
data object WelcomeDestination: TtcNavKey

fun Navigator.navigateToAuthentication(hasLoggedInOnce: Boolean) {
    when {
        hasLoggedInOnce -> navigateToLogin()
        else -> navigateToRegistration()
    }
}

fun Navigator.navigateToLogin() {
    navigate(LoginDestination, clearBackStack = true)
}

fun Navigator.navigateToRegistration() {
    navigate(RegistrationDestination, clearBackStack = true)
}

fun Navigator.navigateToWelcome() {
    navigate(WelcomeDestination)
}

fun EntryProviderScope<NavKey>.authEntries(onLoginClick: ()->Unit, onRegisterClick: ()->Unit, onLoggedIn: ()->Unit, onRegistrationComplete: ()->Unit, onWelcomeClosed: ()->Unit) {
    entry<LoginDestination> {
        LoginScreen(onRegisterClick = onRegisterClick, onLoggedIn = onLoggedIn )
    }

    entry<RegistrationDestination> {
        RegistrationScreen(onLoginClick = onLoginClick, onRegistrationComplete = onRegistrationComplete)
    }

    entry<WelcomeDestination> {
        WelcomeScreen(onClose = onWelcomeClosed)
    }
}
