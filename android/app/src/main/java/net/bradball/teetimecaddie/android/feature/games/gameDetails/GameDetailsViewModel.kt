package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.games.common.showInviteAnswer
import net.bradball.teetimecaddie.android.feature.games.common.signedInViewer
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.toUiState
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
    private val messenger: TtcMessenger,
) : ViewModel() {

    /**
     * The game as the signed-in player sees it, kept current while the screen is up: someone
     * answering, or the game filling, shows here without a refresh.
     *
     * Observed once something starts collecting, rather than from `init`, so creating the
     * ViewModel has no side effects.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GameDetailsUiState> = sessionState
        .signedInViewer()
        .flatMapLatest { viewer ->
            gamesRepository.observeGame(gameId).map { result ->
                when (result) {
                    is TtcResult.Success -> GameDetailsUiState.Content(result.data.toUiState(viewer))
                    is TtcResult.Failure -> GameDetailsUiState.Error(result.error)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GameDetailsUiState.Loading
        )

    // Whether an answer is on its way, so a second tap doesn't send another.
    private var answering = false

    /**
     * Answers the signed-in player's invitation, staying on this screen. The RSVP card collapses
     * when the game updates; a message confirms the answer, or explains why it didn't go through.
     */
    fun onAnswer(accept: Boolean) {
        val player = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        if (answering) return
        answering = true

        viewModelScope.launch {
            val result = gamesRepository.respondToInvite(gameId, player, accept)
            messenger.showInviteAnswer(result, accept)
            answering = false
        }
    }
}
