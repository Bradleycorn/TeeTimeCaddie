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
import androidx.navigation3.ui.NavDisplay
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
 * Remembers a [NavBarNavigator] that persists across recompositions and process death.
 *
 * The navigator's complete state — every tab's back stack, and which tab is selected — is saved and
 * restored through [rememberSaveable].
 *
 * Create the navigator inside the composable that hosts its [NavDisplay], not above it. Where it is
 * remembered decides how long its stacks live: remembered inside the signed-in branch, the tab
 * stacks are discarded when that branch goes away, which is what stops a signed-out player's Games
 * history from surviving underneath the credentials screen.
 *
 * @param startDestination The tab to open on. Used only when creating a new navigator; on
 *   restoration the saved tab wins.
 * @return A remembered navigator that survives recomposition and process death.
 *
 * @see NavBarNavigator
 * @see NavBarNavigator.saver
 */
@Composable
fun rememberNavBarNavigator(startDestination: TopLevelDestination): NavBarNavigator {
    return rememberSaveable(startDestination, saver = NavBarNavigator.saver) {
        NavBarNavigator(startDestination)
    }
}


/**
 * A [Navigator] with one back stack **per** [TopLevelDestination] — the tabbed, signed-in app.
 *
 * Switching tabs preserves your position in each: navigate Games → Add Tee Time, switch to Profile,
 * switch back, and Add Tee Time is still there.
 *
 * ```kotlin
 * navigator.navigateToAddTeeTime()                     // pushes onto the Games stack
 * navigator.navigate(TopLevelDestination.PROFILE)      // switches tab; Games stack is untouched
 * navigator.navigate(TopLevelDestination.TEE_TIMES)    // back on Add Tee Time
 * ```
 *
 * Passing a [TopLevelDestination] to [navigate] switches tabs; passing anything else pushes onto
 * the selected tab's stack. That is the only thing this implementation adds over [BasicNavigator] —
 * everything a feature needs is on [Navigator], so features never name this type.
 *
 * @property topLevelDestinations Every tab, in the order the navigation bar should show them.
 * @property selectedTopLevelDestination The tab currently showing.
 *
 * @constructor Prefer [rememberNavBarNavigator]; a directly constructed navigator is not saved.
 * @param startDestination The tab to open on.
 *
 * @see rememberNavBarNavigator
 * @see Navigator
 */
class NavBarNavigator(startDestination: TopLevelDestination) : Navigator {

    /**
     * List of all top-level destinations.
     */
    val topLevelDestinations = TopLevelDestination.entries.toList()

    private var topLevelStacks : MutableMap<TopLevelDestination, SnapshotStateList<TtcNavKey>> = topLevelDestinations
        .associateWith { key -> mutableStateListOf(key.destination) }
        .toMutableMap()

    /**
     * Returns the back stack for [destination], creating it if it is missing.
     *
     * Every read of [topLevelStacks] goes through here. A plain lookup would be a latent crash:
     * [topLevelStacks] is replaced wholesale when a saved [Navigator] is restored, so state written
     * by a build that had fewer tabs restores a map with no entry for a tab added since. Seeding the
     * stack on demand makes adding a top-level destination safe without a state migration.
     */
    private fun stackFor(destination: TopLevelDestination): SnapshotStateList<TtcNavKey> =
        topLevelStacks.getOrPut(destination) { mutableStateListOf(destination.destination) }

    /**
     * Currently selected top-level destination.
     */
    var selectedTopLevelDestination by mutableStateOf(startDestination)
        private set

    /**
     * Current back stack for the selected top-level destination.
     */
    override val backStack: SnapshotStateList<NavKey> = mutableStateListOf<NavKey>()
        .apply { addAll(stackFor(startDestination)) }

    override val currentDestination: NavKey?
        get() = backStack.lastOrNull()

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(stackFor(selectedTopLevelDestination))
        }

    /**
     * Navigates to the specified destination, optionally clearing the current top-level stack.
     *
     * @param destination The navigation key to add to the stack.
     * @param clearBackStack If true, clears the current top-level stack before navigating.
     * @return true if navigation was successful, false if blocked by policy
     */
    override fun navigate(destination: TtcNavKey, clearBackStack: Boolean) {
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
            val newStack = stackFor(destination)

            if (clearBackStack) {
                newStack.clear()
                newStack.add(destination.destination)
            }

            selectedTopLevelDestination = destination
        } else {
            val currentStack = stackFor(selectedTopLevelDestination)
            if (clearBackStack) {
                currentStack.clear()
            }
            currentStack.add(destination)
        }

        updateBackStack()
    }

    /**
     * Goes back to the previous destination in the current top-level stack.
     */
    override fun goBack() {
        stackFor(selectedTopLevelDestination).removeLastOrNull()
        updateBackStack()
    }

    companion object {
        /**
         * A [Saver] implementation for [NavBarNavigator] that handles saving and restoring its state.
         */
        val saver = Saver<NavBarNavigator, SavedState>(
            save = { value -> encodeToSavedState(serializer = NavBarNavigatorSerializer(), value ) },
            restore = { saveable -> decodeFromSavedState(NavBarNavigatorSerializer(), saveable) }
        )

        /**
         * A [KSerializer] for [NavBarNavigator] that handles serialization of its navigation state.
         *
         * This serializer preserves:
         * - The currently selected top-level destination
         * - All back stacks for each top-level destination
         *
         * During deserialization, it reconstructs the navigator with the saved state.
         */
        @OptIn(InternalSerializationApi::class)
        class NavBarNavigatorSerializer : KSerializer<NavBarNavigator> {
            private val topLevelDestinationSerializer = serializer<TopLevelDestination>()
            private val mapNavigatorSerializer = MapSerializer(topLevelDestinationSerializer, NavBackStackSerializer<TtcNavKey>())

            override val descriptor = buildClassSerialDescriptor("NavBarNavigator") {
                element("selectedTopLevelDestination", topLevelDestinationSerializer.descriptor)
                element("topLevelStacks", mapNavigatorSerializer.descriptor)
            }

            override fun serialize(encoder: Encoder, value: NavBarNavigator) {
                encoder.encodeStructure(descriptor) {
                    encodeSerializableElement(descriptor, 0, topLevelDestinationSerializer, value.selectedTopLevelDestination)
                    encodeSerializableElement(descriptor, 1, mapNavigatorSerializer, value.topLevelStacks)
                }
            }

            override fun deserialize(decoder: Decoder): NavBarNavigator {
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
                    val navigator = NavBarNavigator(selectedTopLevelDestination)
                    navigator.topLevelStacks = topLevelStacks.toMutableMap()
                    navigator.updateBackStack()

                    navigator
                }
            }
        }



    }
}