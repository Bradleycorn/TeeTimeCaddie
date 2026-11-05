package net.bradball.teetimecaddie.android.feature.games.gamesList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GamesListUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.InviteRowUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.inviteAnsweredMessage
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
        .map { (it as? SessionState.SignedIn)?.player }
        .distinctUntilChanged()
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
        val player = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        if (!answering.add(invite.id)) return

        viewModelScope.launch {
            when (val result = gamesRepository.respondToInvite(invite.id, player, accept)) {
                is TtcResult.Success -> {
                    val message = inviteAnsweredMessage(invite.course, accept)
                    messenger.show(TtcMessage(message.resource, message.args))
                }

                is TtcResult.Failure -> {
                    val error = result.error
                    // A yes that lost the race for the last slot: the invitation stays, and the
                    // card shows the game as full once the games update.
                    if (error is GameException && error.error == GameErrors.GAME_FULL) {
                        messenger.show(error.title)
                    } else {
                        messenger.show(error)
                    }
                }
            }
            answering.remove(invite.id)
        }
    }
}
