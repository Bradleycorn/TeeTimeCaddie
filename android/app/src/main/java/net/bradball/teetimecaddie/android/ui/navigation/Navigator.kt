package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay

/**
 * # Navigation System
 *
 * The TeeTime Caddie navigation system is built on the androidx.navigation3 library. A `Navigator`
 * owns the back stack that a [NavDisplay] renders, and is the single vocabulary every feature's
 * navigation is written against.
 *
 * ## Implementations
 *
 * The app has two navigation contexts, and one implementation for each:
 *
 * - [NavBarNavigator] — a stack **per** [TopLevelDestination], for the tabbed, signed-in app.
 *   Switching tabs preserves where you were in each one.
 * - [BasicNavigator] — a single stack, for a flow with no tab bar. The auth flow uses this.
 *
 * Everything a feature needs to navigate is on this interface, so **feature navigation extensions
 * are declared on `Navigator`, not on an implementation**:
 *
 * ```kotlin
 * fun Navigator.navigateToAddTeeTime() {
 *     navigate(AddTeeTimeDestination)
 * }
 * ```
 *
 * Written that way, a feature can be hosted in either context without being rewritten, and there is
 * only one navigation architecture to learn.
 *
 * ## Navigation Best Practices
 *
 * **DO NOT** pass the Navigator into screens or view models. Screens accept lambda callbacks that
 * are wired up in the navigation entry definitions.
 *
 * ❌ **Incorrect — don't do this:**
 * ```kotlin
 * fun EntryProviderScope<NavKey>.teeTimesEntries(navigator: Navigator) {
 *     entry<TeeTimesListDestination> {
 *         TeeTimesListScreen(navigator = navigator) // ❌ Bad!
 *     }
 * }
 * ```
 *
 * ✅ **Correct — do this instead:**
 * ```kotlin
 * fun EntryProviderScope<NavKey>.teeTimesEntries(navigator: Navigator) {
 *     entry<TeeTimesListDestination> {
 *         TeeTimesListScreen(
 *             onAddTeeTimeClick = { navigator.navigateToAddTeeTime() } // ✅ Good!
 *         )
 *     }
 * }
 *
 * @Composable
 * fun TeeTimesListScreen(
 *     onAddTeeTimeClick: () -> Unit // ✅ Screen only knows about callbacks
 * ) {
 *     // Screen implementation
 * }
 * ```
 *
 * This approach:
 * - Keeps screens decoupled from the navigation system
 * - Makes screens easier to test in isolation
 * - Allows screens to be reused in different navigation contexts
 * - Centralizes navigation logic in the navigation entry definitions
 *
 * ## Architecture Overview
 *
 * - **Navigator** — this interface: the back stack a [NavDisplay] renders, and the operations on it
 * - **[TtcNavKey]** — the marker interface every navigation destination implements
 * - **[TopLevelDestination]** — the app's tabs, and the only thing [NavBarNavigator] adds
 * - **NavDisplay** — the Compose component that renders the current destination
 * - **Navigation extensions** — per-feature helpers (`AuthNavigation.kt`, `TeeTimesNavigation.kt`)
 *
 * ## Defining Destinations
 *
 * Destinations are serializable objects implementing [TtcNavKey]:
 *
 * ```kotlin
 * @Serializable
 * data object LoginDestination: TtcNavKey
 *
 * @Serializable
 * data class TeeTimeDetailDestination(val teeTimeId: String): TtcNavKey
 * ```
 *
 * ## State Persistence
 *
 * Both implementations save and restore their full state across configuration changes and process
 * death, via `rememberSaveable` and a custom `Saver` — including each destination's serializable
 * parameters. Use the `remember*` function that goes with the implementation
 * ([rememberNavBarNavigator], [rememberBasicNavigator]) rather than constructing one directly.
 *
 * @see NavBarNavigator
 * @see BasicNavigator
 * @see TtcNavKey
 * @see TopLevelDestination
 */
interface Navigator {

    /**
     * The back stack to render, top-most entry last.
     *
     * A [SnapshotStateList] so [NavDisplay] recomposes as it changes. Never empty — an empty stack
     * has nothing to render, so implementations guarantee at least one entry.
     */
    val backStack: SnapshotStateList<NavKey>

    /** The entry currently on screen: the top of [backStack]. */
    val currentDestination: NavKey?

    /**
     * Navigates to [destination].
     *
     * @param destination The key to show. [NavBarNavigator] additionally understands a
     *   [TopLevelDestination] here, and switches tabs rather than pushing.
     * @param clearBackStack Clears the current stack before navigating, so [destination] becomes
     *   the only thing left to go back to.
     */
    fun navigate(destination: TtcNavKey, clearBackStack: Boolean = false)

    /**
     * Goes back one entry.
     *
     * A no-op when there is nothing left to go back to, since [backStack] must not empty.
     */
    fun goBack()
}
