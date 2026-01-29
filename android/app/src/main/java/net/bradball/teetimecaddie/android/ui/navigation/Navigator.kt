package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

/**
 * Remembers a [Navigator] instance that persists across recompositions and process death.
 *
 * This composable function creates or restores a Navigator instance using Compose's
 * [rememberSaveable] mechanism. The Navigator's complete state—including all back stacks
 * and the current top-level destination—is automatically saved and restored during
 * configuration changes (e.g., screen rotation) and process death.
 *
 * ## Navigation Best Practices
 *
 * Pass navigation callbacks to screens rather than the Navigator instance itself. Wire up
 * these callbacks in your navigation entry definitions:
 *
 * ```kotlin
 * fun EntryProviderScope<NavKey>.teeTimesEntries(navigator: Navigator) {
 *     entry<TeeTimesListDestination> {
 *         TeeTimesListScreen(
 *             onAddTeeTimeClick = { navigator.navigateToAddTeeTime() }
 *         )
 *     }
 * }
 * ```
 *
 * This keeps screens decoupled from the navigation system and easier to test.
 * 
 * ## State Preservation
 *
 * The following state is preserved:
 * - The currently selected top-level destination
 * - All navigation back stacks for each top-level destination
 * - All destination arguments (as long as they are serializable)
 *
 * ## Usage
 *
 * Typically called once at the top level of your app to create the primary Navigator:
 *
 * ```kotlin
 * @Composable
 * fun TeeTimeCaddieApp() {
 *     val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)
 *
 *     NavDisplay(
 *         backStack = navigator.backStack,
 *         onBack = { navigator.goBack() },
 *         entryProvider = entryProvider {
 *             teeTimesEntries(navigator)
 *             authEntries(navigator)
 *         }
 *     )
 * }
 * ```
 *
 * @param startDestination The initial top-level destination to display. This is only used when
 *                         creating a new Navigator instance; on restoration, the saved destination
 *                         is used instead.
 * @return A remembered Navigator instance that persists across recompositions and process death.
 *
 * @see Navigator
 * @see Navigator.saver
 */
@Composable
fun rememberNavigator(startDestination: TopLevelDestination): Navigator {
    return rememberSaveable(startDestination, saver = Navigator.saver) {
        Navigator(startDestination)
    }
}


/**
 * # Navigation System
 *
 * The TeeTime Caddie navigation system is built on top of the androidx.navigation3 library and provides
 * a multi-stack navigation architecture with top-level destinations.
 * 
 * ## Navigation Best Practices
 *
 * **DO NOT** pass the Navigator instance down into screens or view models. Instead, screens should
 * accept lambda callbacks that are wired up in the navigation entry definitions.
 *
 * ❌ **Incorrect - Don't do this:**
 * ```kotlin
 * fun EntryProviderScope<NavKey>.teeTimesEntries(navigator: Navigator) {
 *     entry<TeeTimesListDestination> {
 *         TeeTimesListScreen(navigator = navigator) // ❌ Bad!
 *     }
 * }
 * ```
 *
 * ✅ **Correct - Do this instead:**
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
 * The navigation system consists of several key components:
 *
 * - **Navigator**: The core navigation controller that manages multiple back stacks and top-level destinations
 * - **TtcNavKey**: A marker interface that all navigation destinations must implement
 * - **TopLevelDestination**: An enum of primary app sections (e.g., TEE_TIMES, AUTH)
 * - **NavDisplay**: The Compose component that renders the current destination
 * - **Navigation Extensions**: Helper functions in feature modules (e.g., `AuthNavigation.kt`, `TeeTimesNavigation.kt`)
 *
 * ## Multi-Stack Navigation
 *
 * Each top-level destination maintains its own independent back stack, allowing users to switch
 * between sections while preserving their navigation state within each section. For example:
 *
 * ```kotlin
 * // User navigates: Tee Times List → Add Tee Time
 * navigator.navigateToAddTeeTime()
 *
 * // User switches to a different top-level destination
 * navigator.navigate(TopLevelDestination.PROFILE)
 *
 * // When user returns to Tee Times, they're still on Add Tee Time screen
 * navigator.navigate(TopLevelDestination.TEE_TIMES)
 * ```
 *
 * ## Defining Destinations
 *
 * Destinations are defined as serializable objects that implement `TtcNavKey`:
 *
 * ```kotlin
 * @Serializable
 * data object LoginDestination: TtcNavKey
 *
 * @Serializable
 * data class TeeTimeDetailDestination(val teeTimeId: String): TtcNavKey
 * ```
 *
 * ## Navigation Extensions
 *
 * Each feature module provides navigation extension functions and entry point definitions:
 *
 * ```kotlin
 * // Navigation helper functions
 * fun Navigator.navigateToLogin() {
 *     navigate(LoginDestination, clearBackStack = true)
 * }
 *
 * // Entry point definitions
 * fun EntryProviderScope<NavKey>.authEntries(
 *     onLoginClick: () -> Unit,
 *     onLoggedIn: () -> Unit
 * ) {
 *     entry<LoginDestination> {
 *         LoginScreen(onLoginClick = onLoginClick, onLoggedIn = onLoggedIn)
 *     }
 * }
 * ```
 *
 * ## State Persistence
 *
 * The Navigator automatically saves and restores its state across configuration changes and
 * process death using `rememberSaveable` and a custom `Saver` implementation. This includes:
 *
 * - Currently selected top-level destination
 * - All back stacks for each top-level destination
 * - Each destination's serializable parameters
 *
 * ## Usage Example
 *
 * ```kotlin
 * @Composable
 * fun MyApp() {
 *     val navigator = rememberNavigator(TopLevelDestination.TEE_TIMES)
 *
 *     NavDisplay(
 *         backStack = navigator.backStack,
 *         onBack = { navigator.goBack() },
 *         entryProvider = entryProvider {
 *             teeTimesEntries(navigator)
 *             authEntries(
 *                 onLoginClick = navigator::navigateToLogin,
 *                 onLoggedIn = { navigator.navigateToTeeTimesList(true) }
 *             )
 *         }
 *     )
 * }
 * ```
 *
 * @property topLevelDestinations List of all available top-level destinations in the app.
 * @property selectedTopLevelDestination The currently active top-level destination.
 * @property backStack The current back stack for the selected top-level destination. This is
 *           exposed as a [SnapshotStateList] so that [NavDisplay] can observe changes.
 * @property currentDestination The current destination at the top of the back stack, or null if empty.
 *
 * @constructor Creates a new Navigator instance starting at the specified top-level destination.
 * @param startDestination The initial top-level destination to display.
 *
 * @see rememberNavigator
 * @see TtcNavKey
 * @see TopLevelDestination
 */
class Navigator(startDestination: TopLevelDestination) {

    /**
     * List of all top-level destinations.
     */
    val topLevelDestinations = TopLevelDestination.entries.toList()

    private var topLevelStacks : MutableMap<TopLevelDestination, SnapshotStateList<TtcNavKey>> = topLevelDestinations
        .associateWith { key -> mutableStateListOf(key.destination) }
        .toMutableMap()

    /**
     * Currently selected top-level destination.
     */
    var selectedTopLevelDestination by mutableStateOf(startDestination)
        private set

    /**
     * Current back stack for the selected top-level destination.
     */
    val backStack: SnapshotStateList<NavKey> = mutableStateListOf<NavKey>()
        .apply {
            val initialEntries = topLevelStacks[startDestination]
                ?: throw IllegalStateException("$startDestination was not found in $topLevelDestinations")
            addAll(initialEntries)
        }

    val currentDestination: NavKey?
        get() = backStack.lastOrNull()

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks[selectedTopLevelDestination]!!)
        }

    /**
     * Navigates to the specified destination, optionally clearing the current top-level stack.
     *
     * @param destination The navigation key to add to the stack.
     * @param clearBackStack If true, clears the current top-level stack before navigating.
     * @return true if navigation was successful, false if blocked by policy
     */
    fun navigate(destination: TtcNavKey, clearBackStack: Boolean = false) {
        // No need to do anything if we're already at the requested destination.
        if (selectedTopLevelDestination == destination && !clearBackStack) return
        if (backStack.lastOrNull() == destination) return

        val finalDestination = when (destination) {
            is TopLevelDestination -> destination.destination
            else -> destination
        }

//        if (finalDestination.featureToggle?.isEnabled == false) {
//            return
//        }


        if (destination is TopLevelDestination) {
            val newStack = topLevelStacks[destination]
                ?: throw IllegalArgumentException("Cannot switch to backstack $destination. It does not exist in the list of top level destinations.")

            if (clearBackStack) {
                newStack.clear()
                newStack.add(destination.destination)
            }

            selectedTopLevelDestination = destination
        } else {
            if (clearBackStack) {
                topLevelStacks[selectedTopLevelDestination]?.clear()
            }
            topLevelStacks[selectedTopLevelDestination]?.add(destination)
        }

        updateBackStack()
    }

    /**
     * Goes back to the previous destination in the current top-level stack.
     */
    fun goBack() {
        topLevelStacks[selectedTopLevelDestination]?.removeLastOrNull()
        updateBackStack()
    }

    companion object {
        /**
         * A [Saver] implementation for the [Navigator] class that handles saving and restoring its state.
         */
        val saver = Saver<Navigator, SavedState>(
            save = { value -> encodeToSavedState(serializer = NavigatorSerializer(), value ) },
            restore = { saveable -> decodeFromSavedState(NavigatorSerializer(), saveable) }
        )

        /**
         * A [KSerializer] for the [Navigator] class that handles serialization of its navigation state.
         *
         * This serializer preserves:
         * - The currently selected top-level destination
         * - All back stacks for each top-level destination
         *
         * During deserialization, it reconstructs the Navigator with the saved state.
         */
        @OptIn(InternalSerializationApi::class)
        class NavigatorSerializer : KSerializer<Navigator> {
            private val topLevelDestinationSerializer = serializer<TopLevelDestination>()
            private val mapNavigatorSerializer = MapSerializer(topLevelDestinationSerializer, NavBackStackSerializer<TtcNavKey>())

            override val descriptor = buildClassSerialDescriptor("Navigator") {
                element("selectedTopLevelDestination", topLevelDestinationSerializer.descriptor)
                element("topLevelStacks", mapNavigatorSerializer.descriptor)
            }

            override fun serialize(encoder: Encoder, value: Navigator) {
                encoder.encodeStructure(descriptor) {
                    encodeSerializableElement(descriptor, 0, topLevelDestinationSerializer, value.selectedTopLevelDestination)
                    encodeSerializableElement(descriptor, 1, mapNavigatorSerializer, value.topLevelStacks)
                }
            }

            override fun deserialize(decoder: Decoder): Navigator {
                return decoder.decodeStructure(descriptor) {
                    var selectedTopLevelDestination: TopLevelDestination? = null
                    var topLevelStacks: Map<TopLevelDestination, SnapshotStateList<TtcNavKey>>? = null

                    while (true) {
                        when (val index = decodeElementIndex(descriptor)) {
                            0 -> selectedTopLevelDestination = decodeSerializableElement(descriptor, 0, topLevelDestinationSerializer)
                            1 -> topLevelStacks = decodeSerializableElement(descriptor, 1, mapNavigatorSerializer)
                            CompositeDecoder.DECODE_DONE -> break
                            else -> error("Unexpected index: $index")
                        }
                    }

                    requireNotNull(selectedTopLevelDestination) { "selectedTopLevelDestination is required" }
                    requireNotNull(topLevelStacks) { "topLevelStacks is required" }

                    // Create a new Navigator with the selected destination
                    // NavigationPolicy will be set by the ViewModel after restoration
                    val navigator = Navigator(selectedTopLevelDestination)
                    navigator.topLevelStacks = topLevelStacks.toMutableMap()
                    navigator.updateBackStack()

                    navigator
                }
            }
        }



    }
}