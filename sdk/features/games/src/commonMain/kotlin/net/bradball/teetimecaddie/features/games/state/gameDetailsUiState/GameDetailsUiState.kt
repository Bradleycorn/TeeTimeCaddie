package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.exceptions.TeeTimeCaddieException

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
