package net.bradball.teetimecaddie.android.feature.games.newGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.MAX_TEE_TIME_SLOTS
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

/**
 * State of the New game form.
 *
 * A data class rather than a sealed hierarchy, because its parts are independent: the confirmation
 * opens over a filled-in form, and a failed publish leaves the form as it was.
 *
 * @property invitees The people picked so far, in the order they were added. Never affects
 *   [canSubmit], and may outnumber [slots].
 * @property showNoInviteeConfirm Whether the "Publish with no invitees?" alert is up. Only a
 *   publish with no invitees asks.
 */
data class NewGameUiState(
    val course: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val slots: Int = MAX_TEE_TIME_SLOTS,
    val invitees: List<NewInvitee> = emptyList(),
    val showNoInviteeConfirm: Boolean = false,
    val isSubmitting: Boolean = false,
) {
    /** A course of only spaces doesn't count. Slots never block publishing; they start at 4. */
    val canSubmit: Boolean
        get() = course.isNotBlank() && date != null && time != null && !isSubmitting

    /** "Add from contacts", or "Add more from contacts" once anyone has been added. */
    val addFromContactsLabel: StringResource
        get() = if (invitees.isEmpty()) {
            GMR.strings.new_game_add_from_contacts
        } else {
            GMR.strings.new_game_add_more_from_contacts
        }
}

sealed interface NewGameEvent {
    /** The game is published; leave the form. */
    data object Published : NewGameEvent
}

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
    private val messenger: TtcMessenger,
    private val eventManager: EventManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewGameUiState())
    val uiState: StateFlow<NewGameUiState> = _uiState.asStateFlow()

    private val _events = Channel<NewGameEvent>(Channel.BUFFERED)
    val events: Flow<NewGameEvent> = _events.receiveAsFlow()

    fun onCourseChange(course: String) {
        _uiState.update { it.copy(course = course) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
    }

    fun onSlotsChange(slots: Int) {
        eventManager.logEvent(AnalyticsEvent.PlayerSlotsClick(playerCount = slots))
        _uiState.update { it.copy(slots = slots) }
    }

    /** The date picker was switched between calendar and typed entry. */
    fun onDateInputToggle() {
        eventManager.logEvent(AnalyticsEvent.DateInputToggle)
    }

    /** The time picker was switched between the dial and typed entry. */
    fun onTimeInputToggle() {
        eventManager.logEvent(AnalyticsEvent.TimeInputToggle)
    }

    /**
     * The contacts picker closed with Done: [invitees] replaces the list, because the picker opens
     * with everyone already invited still checked, and unchecking someone removes them.
     */
    fun onInviteesPicked(invitees: List<NewInvitee>) {
        _uiState.update { it.copy(invitees = invitees) }
    }

    /** An invitee's ✕ was tapped. They're removed at once, without asking. */
    fun onRemoveInvitee(invitee: NewInvitee) {
        _uiState.update { it.copy(invitees = it.invitees - invitee) }
    }

    /** The back button was tapped. Leaving is the screen's job; nothing is saved or asked. */
    fun onCancel() {
        eventManager.logEvent(AnalyticsEvent.NewGameCancel)
    }

    /** Publish was tapped. With nobody invited, ask first; otherwise publish. */
    fun onPublishClick() {
        val state = _uiState.value
        if (!state.canSubmit) return
        if (state.invitees.isEmpty()) {
            _uiState.update { it.copy(showNoInviteeConfirm = true) }
        } else {
            submit()
        }
    }

    /** "Keep editing": close the alert and leave the form exactly as it was. */
    fun onKeepEditing() {
        eventManager.logEvent(AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.KEEP_EDITING))
        onDismissNoInviteeConfirm()
    }

    /**
     * The alert was dismissed without a button — a tap outside it, or back. Same outcome as
     * "Keep editing", but it isn't a button press, so it isn't logged as one.
     */
    fun onDismissNoInviteeConfirm() {
        _uiState.update { it.copy(showNoInviteeConfirm = false) }
    }

    /** "Publish anyway". */
    fun onPublishAnyway() {
        eventManager.logEvent(AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.PUBLISH_ANYWAY))
        _uiState.update { it.copy(showNoInviteeConfirm = false) }
        submit()
    }

    private fun submit() {
        val state = _uiState.value
        val organizer = (sessionState.value as? SessionState.SignedIn)?.player
        if (!state.canSubmit || organizer == null) return
        val date = state.date ?: return
        val time = state.time ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            when (
                val result = gamesRepository.publishGame(
                    organizer = organizer,
                    courseName = state.course,
                    date = date,
                    time = time,
                    slots = state.slots,
                    invitees = state.invitees,
                )
            ) {
                is TtcResult.Success -> {
                    // The stored spelling, which may differ from what was typed.
                    messenger.show(GMR.strings.new_game_published_message, result.data.course.name)
                    _events.send(NewGameEvent.Published)
                }

                is TtcResult.Failure -> {
                    messenger.show(result.error)
                    _uiState.update { it.copy(isSubmitting = false) }
                }
            }
        }
    }
}
