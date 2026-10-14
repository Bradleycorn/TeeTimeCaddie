package net.bradball.teetimecaddie.android.feature.profile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.profile.ProfileScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

/**
 * Navigation destination for the signed-in player's own profile.
 *
 * The root of the [TopLevelDestination.PROFILE] stack.
 */
@Serializable
data object ProfileDestination : TtcNavKey

/**
 * Registers the navigation entries for the Profile feature.
 *
 * Signing out is deliberately **not** a navigation callback. `SessionManager` is what changes, and
 * `TeeTimeCaddieApp` branches on the resulting `SessionState` — so the tab tree is replaced by the
 * auth tree without anyone navigating. Routing sign-out through the [Navigator] instead would leave
 * a signed-out player's Games stack sitting in memory behind the credentials screen.
 *
 * @param navigator The Navigator used to build callbacks. Never passed to a screen composable.
 */
@Suppress("UNUSED_PARAMETER")
fun EntryProviderScope<NavKey>.profileEntries(navigator: Navigator) {
    entry<ProfileDestination> {
        ProfileScreen()
    }
}
