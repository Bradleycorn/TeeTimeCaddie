package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bradball.teetimecaddie.android.feature.auth.navigation.CreateAccountDestination
import net.bradball.teetimecaddie.android.feature.auth.navigation.LoginDestination
import net.bradball.teetimecaddie.android.feature.auth.navigation.authEntries
import net.bradball.teetimecaddie.android.feature.auth.navigation.navigateToCreateAccount
import net.bradball.teetimecaddie.android.ui.navigation.BasicNavigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey
import net.bradball.teetimecaddie.android.ui.navigation.rememberBasicNavigator
import net.bradball.teetimecaddie.session.SessionState

/**
 * The auth flow: a [NavDisplay] with no navigation bar, backed by a [BasicNavigator].
 *
 * This is the signed-out half of [TeeTimeCaddieApp]. Auth gets its own navigator rather than a tab
 * in [NavBarNavDisplay]'s: it is the alternative to the app, not a section of it, and keeping its
 * stack separate is what stops signing out from leaving a credentials screen stacked on top of a
 * stale tee-times history.
 *
 * Each entry keeps its own `ViewModelStore`, courtesy of the decorator, so popping back from the
 * profile step to the credentials screen **restores the typed email for free** — `LoginViewModel`
 * was alive underneath the whole time.
 *
 * @param sessionState The current session. Read once, to seed the stack; see [initialAuthStack].
 *
 * @see NavBarNavDisplay
 * @see BasicNavigator
 * @see authEntries
 */
@Composable
fun AuthNavDisplay(sessionState: SessionState, modifier: Modifier = Modifier) {
    val navigator = rememberBasicNavigator(*initialAuthStack(sessionState))

    NavDisplay(
        backStack = navigator.backStack,
        modifier = modifier,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            authEntries(
                onCreateAccount = navigator::navigateToCreateAccount,
                onBack = navigator::goBack,
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
 * Only ever read at first composition — [rememberBasicNavigator] keeps the stack across later
 * session changes, which is what makes the credentials → profile step transition (`SignedOut`
 * becoming `ProfileIncomplete`) leave the stack alone instead of resetting it.
 *
 * A restored [SessionState.ProfileIncomplete] session — someone who force-quit mid-sign-up — starts
 * **two** deep rather than landing straight on the profile step with nothing underneath. Abandoning
 * sign-up then has somewhere to go back to.
 */
private fun initialAuthStack(sessionState: SessionState): Array<TtcNavKey> =
    when (sessionState) {
        is SessionState.ProfileIncomplete ->
            arrayOf(LoginDestination, CreateAccountDestination(sessionState.email))

        else -> arrayOf(LoginDestination)
    }
