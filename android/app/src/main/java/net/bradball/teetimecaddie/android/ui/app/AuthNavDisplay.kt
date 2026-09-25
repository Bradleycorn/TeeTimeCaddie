package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bradball.teetimecaddie.android.feature.auth.navigation.LoginDestination
import net.bradball.teetimecaddie.android.feature.auth.navigation.RegistrationDestination
import net.bradball.teetimecaddie.android.feature.auth.navigation.authEntries
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.session.SessionState

/**
 * The navigation host for the signed-out half of the app.
 *
 * Deliberately **not** a [Navigator] stack. Auth is not a section of the app the way Games and
 * Profile are — it is the alternative to the app, shown only while `SessionState` says there is no
 * complete session. Giving it its own plain back stack is what keeps auth out of the Games history,
 * so signing out can't leave a credentials screen sitting on top of a stale tee-times stack.
 *
 * Each entry keeps its own `ViewModelStore`, courtesy of the decorator, so popping back from the
 * profile step to the credentials screen **restores the typed email for free** — `LoginViewModel`
 * was alive underneath the whole time.
 *
 * @param sessionState The current session. Read once, to seed the stack; see [initialAuthStack].
 */
@Composable
fun AuthNavDisplay(sessionState: SessionState, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(*initialAuthStack(sessionState))

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { popAuth(backStack) },
        entryProvider = entryProvider {
            authEntries(
                onCreateAccount = { email -> backStack.add(RegistrationDestination(email)) },
                onBack = { popAuth(backStack) },
            )
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    )
}

/**
 * The entries the auth stack starts with.
 *
 * Only ever read at first composition — [rememberNavBackStack] keeps the stack across later session
 * changes, which is what makes the credentials → profile step transition (`SignedOut` becoming
 * `ProfileIncomplete`) leave the stack alone instead of resetting it.
 *
 * A restored [SessionState.ProfileIncomplete] session — someone who force-quit mid-sign-up — starts
 * **two** deep rather than landing straight on the profile step with nothing underneath. Abandoning
 * sign-up then has somewhere to go back to.
 */
private fun initialAuthStack(sessionState: SessionState): Array<NavKey> =
    when (sessionState) {
        is SessionState.ProfileIncomplete ->
            arrayOf(LoginDestination, RegistrationDestination(sessionState.email))

        else -> arrayOf(LoginDestination)
    }

/**
 * Pops the auth stack, refusing to empty it.
 *
 * [NavDisplay] renders the last entry, so an empty stack is a crash rather than a sensible
 * "nothing left". The credentials screen is the floor of this flow.
 */
private fun popAuth(backStack: MutableList<NavKey>) {
    if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
}
