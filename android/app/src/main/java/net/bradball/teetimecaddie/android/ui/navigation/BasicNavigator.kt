package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState

/**
 * Remembers a [BasicNavigator] that persists across recompositions and process death.
 *
 * The seed destinations are read **once**. A later change to what you pass here will not reset the
 * stack — which is deliberate: the auth flow's seed depends on `SessionState`, and completing a step
 * changes that state without meaning "start over".
 *
 * Create the navigator inside the composable that hosts its [NavDisplay], not above it, so its stack
 * is discarded when that branch of the UI goes away.
 *
 * @param startDestinations The initial stack, bottom first. At least one is required.
 * @return A remembered navigator that survives recomposition and process death.
 *
 * @see BasicNavigator
 * @see BasicNavigator.saver
 */
@Composable
fun rememberBasicNavigator(vararg startDestinations: TtcNavKey): BasicNavigator {
    return rememberSaveable(saver = BasicNavigator.saver) {
        BasicNavigator(*startDestinations)
    }
}


/**
 * A [Navigator] with a single back stack — a flow with no tab bar.
 *
 * The auth flow uses this. It is the whole of [Navigator] and nothing more, so a feature written
 * against the interface runs here or under [NavBarNavigator] unchanged.
 *
 * Seeding more than one destination gives a flow somewhere to go back *to* when it is entered part
 * way through. The auth flow does exactly that when a half-finished sign-up is restored: the stack
 * starts at the credentials screen with the profile step above it, so abandoning sign-up has a
 * destination rather than a dead end.
 *
 * @constructor Prefer [rememberBasicNavigator]; a directly constructed navigator is not saved.
 * @param startDestinations The initial stack, bottom first.
 *
 * @see rememberBasicNavigator
 * @see Navigator
 */
class BasicNavigator private constructor(
    override val backStack: SnapshotStateList<NavKey>
) : Navigator {

    constructor(vararg startDestinations: TtcNavKey) :
        this(mutableStateListOf<NavKey>(*startDestinations))

    init {
        require(backStack.isNotEmpty()) { "A BasicNavigator needs at least one start destination." }
    }

    override val currentDestination: NavKey?
        get() = backStack.lastOrNull()

    override fun navigate(destination: TtcNavKey, clearBackStack: Boolean) {
        if (!clearBackStack && backStack.lastOrNull() == destination) return

        if (clearBackStack) {
            backStack.clear()
        }
        backStack.add(destination)
    }

    /**
     * Pops the stack, refusing to empty it.
     *
     * [NavDisplay] renders the last entry, so an empty stack is a crash rather than a sensible
     * "nothing left". The bottom of the stack is the floor of the flow; going back from there is
     * the system's business, not the navigator's — and nav3 only intercepts the back gesture while
     * there is more than one entry, so the app closes as it should.
     */
    override fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    companion object {
        /**
         * A [Saver] for [BasicNavigator].
         *
         * The stack is the entire state, so it is all that gets written. Entries are serialized
         * polymorphically by concrete class, via the same machinery [NavBarNavigator] uses for each
         * of its tabs.
         */
        val saver = Saver<BasicNavigator, SavedState>(
            save = { value -> encodeToSavedState(NavBackStackSerializer<NavKey>(), value.backStack) },
            restore = { saveable ->
                BasicNavigator(decodeFromSavedState(NavBackStackSerializer<NavKey>(), saveable))
            }
        )
    }
}
