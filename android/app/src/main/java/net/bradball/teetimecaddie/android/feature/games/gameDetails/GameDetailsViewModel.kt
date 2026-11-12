package net.bradball.teetimecaddie.android.feature.games.gameDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.bradball.teetimecaddie.android.feature.games.common.showInviteAnswer
import net.bradball.teetimecaddie.android.feature.games.common.signedInViewer
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.toDetailsUiState
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
    private val eventManager: EventManager,
    private val messenger: TtcMessenger,
) : ViewModel() {

    // The number of the invitee whose response sheet the organizer has open, or null while it's
    // closed. Ephemeral: it lives as long as this screen.
    private val respondingFor = MutableStateFlow<String?>(null)

    /**
     * The game as the signed-in player sees it, kept current while the screen is up: someone
     * answering, or the game filling, shows here without a refresh. The organizer's response
     * sheet is part of it while it's open, and keeps up the same way.
     *
     * Observed once something starts collecting, rather than from `init`, so creating the
     * ViewModel has no side effects.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GameDetailsUiState> = sessionState
        .signedInViewer()
        // Signing out tears this screen down; until it does, keep showing the game as it was.
        .filterNotNull()
        .flatMapLatest { viewer ->
            combine(gamesRepository.observeGame(gameId), respondingFor) { result, phone ->
                when (result) {
                    is TtcResult.Success -> result.data.toDetailsUiState(viewer, respondingFor = phone)
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
        eventManager.logEvent(AnalyticsEvent.AnswerInviteClick(accept, AnalyticsEvent.AnswerInviteClick.Source.GAME_DETAILS))
        val player = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        if (answering) return
        answering = true

        viewModelScope.launch {
            val result = gamesRepository.respondToInvite(gameId, player, accept)
            messenger.showInviteAnswer(result, accept)
            answering = false
        }
    }

    /**
     * The organizer tapped an invitee who hasn't answered, or their ⋮: opens the sheet for
     * recording their answer.
     *
     * @param inviteePhone The row's `inviteePhone`.
     */
    fun onInviteeClick(inviteePhone: String) {
        eventManager.logEvent(AnalyticsEvent.InviteeRowClick)
        respondingFor.value = inviteePhone
    }

    /** The response sheet closed without Yes or No being picked. */
    fun onResponseSheetDismiss() {
        if (respondingFor.value == null) return
        eventManager.logEvent(AnalyticsEvent.OrganizerResponseSheetDismiss)
        respondingFor.value = null
    }

    // Whether the organizer's answer for an invitee is on its way, so a second pick doesn't send
    // another.
    private var recordingResponse = false

    /**
     * Records the organizer's Yes ([accept]) or No for the invitee whose response sheet is open, and
     * closes it. The roster moves when the game updates; a message confirms the answer, or explains
     * why it didn't go through.
     */
    fun onRecordResponse(accept: Boolean) {
        eventManager.logEvent(AnalyticsEvent.OrganizerResponseClick(accept))
        val responseSheetUiState = ((uiState.value as? GameDetailsUiState.Content)?.game as? GameUiState.Organizer)
            ?.responseSheet ?: return
        respondingFor.value = null
        val organizer = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        if (recordingResponse) return
        recordingResponse = true

        viewModelScope.launch {
            when (val result = gamesRepository.recordResponse(gameId, organizer, responseSheetUiState.inviteePhone, accept)) {
                is TtcResult.Success -> responseSheetUiState.recordedMessage(accept)
                    .let { messenger.show(TtcMessage(it.resource, it.args)) }
                is TtcResult.Failure -> {
                    val error = result.error
                    // The invitation stays pending, and the screen shows the tee time as full once
                    // the game updates.
                    if (error is GameException && error.error == GameErrors.GAME_FULL) {
                        messenger.show(error.title)
                    } else {
                        messenger.show(error)
                    }
                }
            }
            recordingResponse = false
        }
    }
}
