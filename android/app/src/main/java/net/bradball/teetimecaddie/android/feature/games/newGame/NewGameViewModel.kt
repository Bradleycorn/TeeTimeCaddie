package net.bradball.teetimecaddie.android.feature.games.newGame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsRepositoryFactory
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent.ContactPickerDismiss.Selection
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.DeviceContactsReader
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.NewGameUiState
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

sealed interface NewGameEvent {
    /** The game is published; leave the form. */
    data object Published : NewGameEvent

    /** Open the system contact picker, and report how it closed to `onContactPickerDismissed`. */
    data object PickContacts : NewGameEvent

    /**
     * Ask "Publish with no invitees?", and report the answer to `onKeepEditing` or
     * `onPublishAnyway`. Dismissing it any other way leaves the form as it was.
     */
    data object ConfirmNoInvitees : NewGameEvent
}

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
    private val contactsRepositoryFactory: ContactsRepositoryFactory,
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

    /** "Add from contacts" was tapped: the system contact picker opens. No permission is needed. */
    fun onAddFromContactsClick() {
        eventManager.logEvent(AnalyticsEvent.AddFromContactsClick)
        viewModelScope.launch { _events.send(NewGameEvent.PickContacts) }
    }

    /**
     * The system picker closed. [picked] reads the contacts it returned, or is null when nothing was
     * picked. Picked contacts join the end of the list, sorted by name among themselves; anyone
     * already invited at that number isn't added twice. The picker can't show who's already
     * invited, so it only ever adds; the ✕ on a row removes.
     */
    fun onContactPickerDismissed(picked: DeviceContactsReader?) {
        val selection = if (picked == null) Selection.NONE else Selection.SOME
        eventManager.logEvent(AnalyticsEvent.ContactPickerDismiss(selection))
        if (picked == null) return
        viewModelScope.launch {
            val repository = contactsRepositoryFactory.create(picked)
            when (val result = repository.addPickedContacts(_uiState.value.invitees)) {
                is TtcResult.Success -> _uiState.update { it.copy(invitees = result.data) }
                is TtcResult.Failure -> messenger.show(result.error)
            }
        }
    }

    /** An invitee's ✕ was tapped. They're removed at once, without asking. */
    fun onRemoveInvitee(invitee: NewInvitee) {
        eventManager.logEvent(AnalyticsEvent.RemoveInviteeClick)
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
            viewModelScope.launch { _events.send(NewGameEvent.ConfirmNoInvitees) }
        } else {
            submit()
        }
    }

    /** "Keep editing": the form stays exactly as it was. */
    fun onKeepEditing() {
        eventManager.logEvent(AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.KEEP_EDITING))
    }

    /** "Publish anyway". */
    fun onPublishAnyway() {
        eventManager.logEvent(AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.PUBLISH_ANYWAY))
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
