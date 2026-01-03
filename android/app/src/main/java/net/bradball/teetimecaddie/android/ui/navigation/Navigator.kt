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
 * Remembers a Navigator instance that persists across recompositions and process death.
 *
 * @param startDestination The initial top-level destination to start the navigation from.
 * @param isLoggedIn Boolean indicating if the user is logged in, used for navigation policy.
 * @return A remembered Navigator instance.
 */
@Composable
fun rememberNavigator(startDestination: TopLevelDestination): Navigator {
    return rememberSaveable(startDestination, saver = Navigator.saver) {
        Navigator(startDestination)
    }
}

/**
 * Navigator manages navigation between top-level destinations and their respective back stacks.
 *
 * @property topLevelDestinations List of all top-level destinations.
 * @property selectedTopLevelDestination Currently selected top-level destination.
 * @property backStack Current back stack for the selected top-level destination.
 * @constructor Initializes the navigator with the given start destination.
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