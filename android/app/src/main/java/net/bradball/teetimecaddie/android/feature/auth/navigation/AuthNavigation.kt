package net.bradball.teetimecaddie.android.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.android.feature.auth.createAccount.CreateAccountScreen
import net.bradball.teetimecaddie.android.feature.auth.createAccount.CreateAccountViewModel
import net.bradball.teetimecaddie.android.feature.auth.createAccount.CreateAccountViewModelFactory
import net.bradball.teetimecaddie.android.feature.auth.login.LoginScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey
import net.bradball.teetimecaddie.session.SessionState

/**
 * Navigation destination for the credentials screen.
 *
 * One screen serves both signing in and starting a new account: the person types an email and a
 * password, then chooses which of the two they meant. There is no separate "sign up" form.
 */
@Serializable
data object LoginDestination : TtcNavKey

/**
 * Navigation destination for the profile step of creating an account.
 *
 * Reached once the Firebase account exists, so the session is already authenticated and the app is
 * in [SessionState.ProfileIncomplete].
 *
 * The key carries the **email only** — never the password. A nav key is `Hashable`, serialized into
 * saved state and written to disk; a password has no business in it. The email is here because the
 * screen displays it, and because it is what a restored `ProfileIncomplete` session has to hand.
 */
@Serializable
data class CreateAccountDestination(val email: String) : TtcNavKey

/**
 * Navigates to the profile step of creating an account.
 *
 * Pushes rather than replaces, so going back returns to the credentials screen with the typed email
 * still in it.
 *
 * @param email The address the account was created with, shown on the profile step.
 */
fun Navigator.navigateToCreateAccount(email: String) {
    navigate(CreateAccountDestination(email))
}

/**
 * Registers the navigation entries for the Authentication feature.
 *
 * These entries are hosted by `AuthNavDisplay`, **not** by the tabbed `NavBarNavDisplay`. Auth is not
 * a destination the signed-in app can navigate to: `TeeTimeCaddieApp` branches on `SessionState`,
 * so the whole auth tree exists only while there is no complete session, and disappears the moment
 * there is one.
 *
 * That branch is also why there are no `onLoggedIn` / `onAccountCreated` callbacks. Success is not
 * a navigation event — it is a change of `SessionState`, observed above this subtree.
 *
 * @param onCreateAccount Invoked with the typed email once the account exists, to move to the
 *   profile step.
 * @param onBack Invoked to leave the profile step and return to the credentials screen.
 */
fun EntryProviderScope<NavKey>.authEntries(
    onCreateAccount: (email: String) -> Unit,
    onBack: () -> Unit,
) {
    entry<LoginDestination> {
        LoginScreen(onCreateAccount = onCreateAccount)
    }

    entry<CreateAccountDestination> { destination ->
        // Assisted injection: navigation3's type-safe keys don't populate a SavedStateHandle, so
        // the email reaches the ViewModel through its factory rather than through saved state.
        val viewModel = hiltViewModel<CreateAccountViewModel, CreateAccountViewModelFactory> { factory ->
            factory.create(destination.email)
        }
        CreateAccountScreen(viewModel = viewModel, onBack = onBack)
    }
}
