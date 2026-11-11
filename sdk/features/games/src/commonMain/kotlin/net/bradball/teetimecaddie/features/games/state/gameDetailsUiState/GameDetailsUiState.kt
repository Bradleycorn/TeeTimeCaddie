package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException

/** State for a game's details. */
sealed interface GameDetailsUiState {
    data object Loading : GameDetailsUiState

    data class Content(val game: GameUiState) : GameDetailsUiState

    /** The game could not be read, or no longer exists. */
    data class Error(val title: LocalizedText, val message: LocalizedText) : GameDetailsUiState {
        /** The failure's own title and message, with the message's placeholders filled. */
        constructor(error: TeeTimeCaddieException) : this(
            title = LocalizedText(error.title),
            message = LocalizedText(error.displayMessage, error.messageArgs)
        )
    }
}

/**
 * This game's details as [viewer] sees them: [GameDetailsUiState.Content], or, for a viewer who
 * neither organized the game nor was invited to it, the same error as a game that doesn't exist.
 */
fun Game.toDetailsUiState(viewer: Player): GameDetailsUiState =
    this.toUiState(viewer)
        ?.let { GameDetailsUiState.Content(it) }
        ?: GameDetailsUiState.Error(GameException(GameErrors.NOT_FOUND))
