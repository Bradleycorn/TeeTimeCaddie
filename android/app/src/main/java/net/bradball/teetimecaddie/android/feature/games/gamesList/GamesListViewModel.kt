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
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

/**
 * State for the Games tab.
 *
 * A sealed hierarchy rather than a data class, because the three cases never occur together: the
 * tab shows a spinner, the empty state, or the list.
 */
sealed interface GamesListUiState {
    data object Loading : GamesListUiState
    data object Empty : GamesListUiState
    data class Content(val games: List<Game>) : GamesListUiState
}

@HiltViewModel
class GamesListViewModel @Inject constructor(
    sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
    private val eventManager: EventManager,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GamesListUiState> = sessionState
        .map { (it as? SessionState.SignedIn)?.player?.id }
        .distinctUntilChanged()
        .flatMapLatest { playerId ->
            if (playerId == null) {
                flowOf(GamesListUiState.Loading)
            } else {
                gamesRepository.upcomingGames(playerId).map { games ->
                    if (games.isEmpty()) GamesListUiState.Empty else GamesListUiState.Content(games)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GamesListUiState.Loading
        )

    /** Records a tap on an add control (the FAB or "Create a game"). Navigating is the screen's job. */
    fun onAddGameClick() {
        eventManager.logEvent(AnalyticsEvent.AddGameClick)
    }

    /** Records a tap on a game. Navigating to it is the screen's job. */
    fun onGameClick() {
        eventManager.logEvent(AnalyticsEvent.GameListItemClick)
    }
}
