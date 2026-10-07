package net.bradball.teetimecaddie.android.ui.app

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bradball.teetimecaddie.android.feature.profile.navigation.profileEntries
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.teeTimesEntries
import net.bradball.teetimecaddie.android.ui.common.navigation.TtcNavigationBar
import net.bradball.teetimecaddie.android.ui.navigation.NavBarNavigator
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination
import net.bradball.teetimecaddie.android.ui.navigation.rememberNavBarNavigator

/**
 * The tabbed app: a [NavDisplay] above a [TtcNavigationBar], backed by a [NavBarNavigator].
 *
 * This is the signed-in half of [TeeTimeCaddieApp]. Auth entries are deliberately absent — auth is
 * not somewhere the signed-in app can navigate to, it is the other branch, hosted by
 * [AuthNavDisplay].
 *
 * ## Navigation Entry Registration
 *
 * Every feature that has a tab registers its entries here, through its own extension function:
 *
 * ```kotlin
 * entryProvider = entryProvider {
 *     teeTimesEntries(navigator)
 *     profileEntries(navigator)
 * }
 * ```
 *
 * ## Entry Decorators
 *
 * - **SaveableStateHolder** preserves Compose state (scroll position, field values) across
 *   navigating away and back.
 * - **ViewModelStore** scopes ViewModels per entry, so they survive configuration changes but are
 *   cleared when the entry leaves the stack.
 *
 * ## Navigation Callback Wiring
 *
 * Navigation callbacks are built here from the [Navigator] and passed through the entry
 * definitions. **Screen composables never receive the Navigator itself.**
 *
 * The navigator is created here rather than passed in, so its tab stacks live exactly as long as
 * this composable does — signing out discards them rather than leaving a stale Games history behind
 * the credentials screen.
 *
 * @see AuthNavDisplay
 * @see NavBarNavigator
 * @see teeTimesEntries
 * @see profileEntries
 */
@Composable
fun NavBarNavDisplay(modifier: Modifier = Modifier) {
    val navigator = rememberNavBarNavigator(TopLevelDestination.TEE_TIMES)

    Column(modifier = modifier) {
        NavDisplay(
            backStack = navigator.backStack,
            modifier = Modifier.weight(1F),
            onBack = { navigator.goBack() },
            entryProvider = entryProvider {
                teeTimesEntries(navigator)
                profileEntries(navigator)
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            )
        )

        TtcNavigationBar(
            destinations = navigator.topLevelDestinations,
            selected = navigator.selectedTopLevelDestination,
            onSelect = { navigator.navigate(it) },
        )
    }
}
