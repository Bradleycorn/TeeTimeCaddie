package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.toUiState
import net.bradball.teetimecaddie.session.SessionState

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
                GameDetailsUiState.Content(result.data.toUiState(viewerId))
            }

            is TtcResult.Failure -> GameDetailsUiState.Error(result.error)
        }
}
