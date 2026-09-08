package net.bradball.teetimecaddie.android.ui.navigation

import androidx.annotation.StringRes
import net.bradball.teetimecaddie.android.feature.teeTimes.navigation.TeeTimesListDestination
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.teetimes.TTR

/**
 * Defines the top-level navigation destinations in the TeeTime Caddie app.
 *
 * Top-level destinations represent the primary sections of the application that users can
 * navigate between using the app's main navigation UI (typically a bottom navigation bar
 * or navigation drawer). Each top-level destination maintains its own independent back stack,
 * allowing users to switch between sections while preserving their navigation state.
 *
 * ## Multi-Stack Navigation
 *
 * When a user switches between top-level destinations, their position within each section
 * is preserved. For example:
 *
 * ```kotlin
 * // User is viewing Tee Times List, then navigates to Add Tee Time
 * navigator.navigateToAddTeeTime()
 *
 * // User switches to a different top-level destination (when more are added)
 * navigator.navigate(TopLevelDestination.PROFILE)
 *
 * // User switches back to Tee Times - they're still on Add Tee Time screen
 * navigator.navigate(TopLevelDestination.TEE_TIMES)
 * ```
 *
 * ## Adding New Top-Level Destinations
 *
 * To add a new top-level destination:
 *
 * 1. Add a new enum value with its icon, label, and starting destination:
 * ```kotlin
 * PROFILE(
 *     icon = Icons.PERSON,
 *     iconTextId = R.string.profile_title,
 *     destination = ProfileDestination
 * )
 * ```
 *
 * 2. Create the corresponding navigation destination object:
 * ```kotlin
 * @Serializable
 * data object ProfileDestination: TtcNavKey
 * ```
 *
 * 3. Add navigation entry definitions in your feature module:
 * ```kotlin
 * fun EntryProviderScope<NavKey>.profileEntries(navigator: Navigator) {
 *     entry<ProfileDestination> {
 *         ProfileScreen(
 *             onSettingsClick = { navigator.navigateToSettings() }
 *         )
 *     }
 * }
 * ```
 *
 * 4. Wire up the entries in your app's root composable:
 * ```kotlin
 * NavDisplay(
 *     backStack = navigator.backStack,
 *     onBack = { navigator.goBack() },
 *     entryProvider = entryProvider {
 *         teeTimesEntries(navigator)
 *         profileEntries(navigator) // Add new entries
 *     }
 * )
 * ```
 *
 * ## Design Considerations
 *
 * Each top-level destination implements [TtcNavKey], which allows the enum values themselves
 * to be used as navigation destinations. This design enables the Navigator to treat top-level
 * destinations uniformly while still maintaining separate back stacks.
 *
 * The [destination] property specifies the initial screen to show when the user first navigates
 * to this top-level destination or when the back stack is cleared.
 *
 * @property icon The icon to display for this destination in the navigation UI.
 * @property iconTextId String resource ID for the accessibility label and display text.
 * @property destination The initial navigation destination for this top-level section.
 *
 * @see Navigator
 * @see TtcNavKey
 */
enum class TopLevelDestination (
    val icon: TtcIcons,
    @StringRes val iconTextId: Int,
    val destination: TtcNavKey
): TtcNavKey {
    TEE_TIMES(
        icon = TtcIcons.TEE_CLOCK,
        iconTextId = TTR.strings.tee_times_title.resourceId,
        destination = TeeTimesListDestination
    ),
}
