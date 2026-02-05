package net.bradball.teetimecaddie.android.feature.teeTimes.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime.AddTeeTimeScreen
import net.bradball.teetimecaddie.android.feature.teeTimes.editTeeTime.EditTeeTimeScreen
import net.bradball.teetimecaddie.android.feature.teeTimes.teeTimeList.TeeTimesListScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

/**
 * Navigation destination for the Tee Times list screen.
 *
 * This is the main entry point for the Tee Times feature, displaying all upcoming
 * tee times for the user.
 */
@Serializable
data object TeeTimesListDestination: TtcNavKey

/**
 * Navigation destination for the Add Tee Time screen.
 *
 * Allows users to create a new tee time by entering course, date, and time information.
 */
@Serializable
data object AddTeeTimeDestination: TtcNavKey

/**
 * Navigation destination for the Edit Tee Time screen.
 *
 * Allows users to edit an existing tee time.
 *
 * @param teeTimeId The ID of the tee time to edit.
 */
@Serializable
data class EditTeeTimeDestination(val teeTimeId: String): TtcNavKey


/**
 * Navigates to the Tee Times list screen.
 *
 * @param clearBackStack If true, clears the entire back stack before navigating. This is
 *                       typically used after login/registration to prevent users from
 *                       navigating back to auth screens.
 */
fun Navigator.navigateToTeeTimesList(clearBackStack: Boolean = false) {
    navigate(TeeTimesListDestination, clearBackStack)
}

/**
 * Navigates to the Add Tee Time screen.
 *
 * Adds the screen to the back stack, allowing the user to return to the previous
 * screen via back navigation.
 */
fun Navigator.navigateToAddTeeTime() {
    navigate(AddTeeTimeDestination)
}

/**
 * Navigates to the Edit Tee Time screen.
 *
 * @param teeTimeId The ID of the tee time to edit.
 */
fun Navigator.navigateToEditTeeTime(teeTimeId: String) {
    navigate(EditTeeTimeDestination(teeTimeId))
}

/**
 * Registers all navigation entries for the Tee Times feature.
 *
 * This function defines the navigation graph for the Tee Times feature by registering
 * all screen composables and wiring up their navigation callbacks. Each entry maps a
 * destination to its corresponding screen composable.
 *
 * ## Navigation Callback Pattern
 *
 * Navigation callbacks are defined here and passed to screen composables as lambda functions.
 * Screens should never receive the Navigator instance directly:
 *
 * ```kotlin
 * entry<TeeTimesListDestination> {
 *     TeeTimesListScreen(
 *         onAddTeeTimeClick = { navigator.navigateToAddTeeTime() } // ✅ Good!
 *     )
 * }
 * ```
 *
 * This approach:
 * - Keeps screens decoupled from the navigation system
 * - Makes screens easier to test in isolation
 * - Allows screens to be reused in different contexts
 * - Centralizes navigation logic in this file
 *
 * ## Usage
 *
 * This function is called from [TtcNavDisplay] during app initialization:
 *
 * ```kotlin
 * entryProvider = entryProvider {
 *     teeTimesEntries(navigator)
 *     authEntries(...)
 * }
 * ```
 *
 * @param navigator The Navigator instance used to create navigation callbacks. This is passed
 *                  to this function but should never be passed directly to screen composables.
 *
 * @see TeeTimesListDestination
 * @see AddTeeTimeDestination
 * @see Navigator
 */
fun EntryProviderScope<NavKey>.teeTimesEntries(navigator: Navigator) {
    entry<TeeTimesListDestination> {
        TeeTimesListScreen(
            onAddTeeTimeClick = { navigator.navigateToAddTeeTime() },
            onTeeTimeClick = { teeTimeId -> navigator.navigateToEditTeeTime(teeTimeId) }
        )
    }

    entry<AddTeeTimeDestination> {
        AddTeeTimeScreen(
            onBack = { navigator.goBack() },
            onTeeTimeCreated = { navigator.goBack() }
        )
    }

    entry<EditTeeTimeDestination> {
        EditTeeTimeScreen(
            onBack = { navigator.goBack() },
            onTeeTimeUpdated = { navigator.goBack() }
        )
    }
}