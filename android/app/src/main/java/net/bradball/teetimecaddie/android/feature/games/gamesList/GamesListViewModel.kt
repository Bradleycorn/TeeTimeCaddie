package net.bradball.teetimecaddie.android.feature.games.gamesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.games.common.showInviteAnswer
import net.bradball.teetimecaddie.android.feature.games.common.signedInViewer
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GamesListUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.InviteRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.toGamesListUiState
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

@HiltViewModel
class GamesListViewModel @Inject constructor(
    private val sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
    private val eventManager: EventManager,
    private val messenger: TtcMessenger,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GamesListUiState> = sessionState
        .signedInViewer()
        .flatMapLatest { player ->
            if (player == null) {
                flowOf(GamesListUiState.Loading)
            } else {
                gamesRepository.observeGames(player).map { it.toGamesListUiState(player) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GamesListUiState.Loading
        )

    // Games whose answer is on its way, so a second tap doesn't send another.
    private val answering = mutableSetOf<String>()

    /** Records a tap on an add control (the FAB or "Create a game"). Navigating is the screen's job. */
    fun onAddGameClick(source: AnalyticsEvent.AddGameClick.Source) {
        eventManager.logEvent(AnalyticsEvent.AddGameClick(source))
    }

    /** Records a tap on a game. Navigating to it is the screen's job. */
    fun onGameClick() {
        eventManager.logEvent(AnalyticsEvent.GameListItemClick)
    }

    /**
     * Answers [invite] at once, with no confirmation step. The invitation leaves the card when the
     * games update; a message confirms the answer, or explains why it didn't go through.
     */
    fun onAnswerInvite(invite: InviteRowUiState, accept: Boolean) {
        eventManager.logEvent(AnalyticsEvent.AnswerInviteClick(accept, AnalyticsEvent.AnswerInviteClick.Source.GAMES_LIST))
        val player = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        if (!answering.add(invite.id)) return

        viewModelScope.launch {
            val result = gamesRepository.respondToInvite(invite.id, player, accept)
            messenger.showInviteAnswer(result, accept)
            answering.remove(invite.id)
        }
    }
}
