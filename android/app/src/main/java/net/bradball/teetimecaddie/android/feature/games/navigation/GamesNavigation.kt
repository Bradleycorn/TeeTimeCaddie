package net.bradball.teetimecaddie.android.feature.games.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import net.bradball.teetimecaddie.android.feature.games.gamesList.GamesListScreen
import net.bradball.teetimecaddie.android.ui.navigation.Navigator
import net.bradball.teetimecaddie.android.ui.navigation.TopLevelDestination
import net.bradball.teetimecaddie.android.ui.navigation.TtcNavKey

/**
 * The Games tab: the signed-in player's upcoming games, or the empty state.
 *
 * The root of the [TopLevelDestination.TEE_TIMES] stack.
 */
@Serializable
data object GamesListDestination : TtcNavKey

/**
 * The form for creating and publishing a new game.
 */
@Serializable
data object NewGameDestination : TtcNavKey

/**
 * A single game's details.
 *
 * @property gameId The game to show. Only the id travels; the details screen loads the game itself.
 */
@Serializable
data class GameDetailsDestination(val gameId: String) : TtcNavKey

/**
 * Opens the New game form.
 */
fun Navigator.navigateToNewGame() {
    navigate(NewGameDestination)
}

/**
 * Opens a game's details.
 */
fun Navigator.navigateToGameDetails(gameId: String) {
    navigate(GameDetailsDestination(gameId))
}

/**
 * Registers the navigation entries for the Games feature.
 *
 * Navigation callbacks are built here from the [Navigator]; screens never receive it.
 *
 * @param navigator The Navigator used to build callbacks. Never passed to a screen composable.
 */
fun EntryProviderScope<NavKey>.gamesEntries(navigator: Navigator) {
    entry<GamesListDestination> {
        GamesListScreen(
            onNewGameClick = { navigator.navigateToNewGame() },
            onGameClick = { gameId -> navigator.navigateToGameDetails(gameId) },
        )
    }
}
