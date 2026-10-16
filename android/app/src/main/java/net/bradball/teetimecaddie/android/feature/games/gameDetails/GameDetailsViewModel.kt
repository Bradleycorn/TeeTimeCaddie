package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.session.SessionState

/**
 * State for a game's details.
 */
sealed interface GameDetailsUiState {
    data object Loading : GameDetailsUiState

    /**
     * @property isOrganizer Whether the signed-in player organized [game], which decides the
     *   "You are the organizer" chip.
     */
    data class Content(val game: Game, val isOrganizer: Boolean) : GameDetailsUiState

    /** The game could not be read, or no longer exists. */
    data class Error(val title: StringResource, val message: StringResource) : GameDetailsUiState
}

@AssistedFactory
interface GameDetailsViewModelFactory {
    fun create(gameId: String): GameDetailsViewModel
}

@HiltViewModel(assistedFactory = GameDetailsViewModelFactory::class)
class GameDetailsViewModel @AssistedInject constructor(
    @Assisted private val gameId: String,
    private val sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
) : ViewModel() {

    /**
     * Loads the game once something starts observing, rather than from `init`, so creating the
     * ViewModel has no side effects.
     */
    val uiState: StateFlow<GameDetailsUiState> = flow { emit(loadGame()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GameDetailsUiState.Loading
        )

    private suspend fun loadGame(): GameDetailsUiState =
        when (val result = gamesRepository.getGame(gameId)) {
            is TtcResult.Success -> {
                val viewerId = (sessionState.value as? SessionState.SignedIn)?.player?.id
                GameDetailsUiState.Content(
                    game = result.data,
                    isOrganizer = viewerId != null && result.data.isOrganizer(viewerId),
                )
            }

            is TtcResult.Failure -> GameDetailsUiState.Error(
                title = result.error.title,
                message = result.error.displayMessage,
            )
        }
}
