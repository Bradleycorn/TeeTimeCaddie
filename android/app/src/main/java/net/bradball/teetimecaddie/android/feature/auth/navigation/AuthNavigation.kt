package net.bradball.teetimecaddie.android.feature.auth.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.auth.login.LoginScreen
import net.bradball.teetimecaddie.android.feature.auth.registration.RegistrationScreen
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
data class RegistrationDestination(val email: String) : TtcNavKey

/**
 * Registers the navigation entries for the Authentication feature.
 *
 * These entries are hosted by `AuthNavDisplay`, **not** by the tabbed `TtcNavDisplay`. Auth is not
 * a destination the signed-in app can navigate to: `TeeTimeCaddieApp` branches on `SessionState`,
 * so the whole auth tree exists only while there is no complete session, and disappears the moment
 * there is one.
 *
 * That branch is also why there are no `onLoggedIn` / `onRegistrationComplete` callbacks. Success
 * is not a navigation event — it is a change of `SessionState`, observed above this subtree.
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

    entry<RegistrationDestination> { destination ->
        RegistrationScreen(email = destination.email, onBack = onBack)
    }
}
