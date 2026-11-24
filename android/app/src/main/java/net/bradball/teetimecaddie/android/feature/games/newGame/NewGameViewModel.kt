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
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent.ContactPickerDismiss.Selection
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.extensions.PHONE_NUMBER_LENGTH
import net.bradball.teetimecaddie.core.extensions.toPhoneDigits
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.ContactsRepository
import net.bradball.teetimecaddie.features.games.DeviceContactsReader
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GamesRepository
import net.bradball.teetimecaddie.features.games.state.NewGameUiState
import net.bradball.teetimecaddie.features.games.state.addPlayersUiState.AddPlayersUiState
import net.bradball.teetimecaddie.session.SessionState
import javax.inject.Inject

sealed interface NewGameEvent {
    /** The game is published; leave the form. */
    data object Published : NewGameEvent

    /**
     * Open the system contact picker over the Add players sheet, and report how it closed to
     * `onContactPickerDismissed`.
     */
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
    private val contactsRepository: ContactsRepository,
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

    /** "Add players" was tapped: the Add players sheet opens with nothing selected. */
    fun onAddPlayersClick() {
        eventManager.logEvent(AnalyticsEvent.AddFromContactsClick)
        val organizer = (sessionState.value as? SessionState.SignedIn)?.player ?: return
        _uiState.update { it.copy(addPlayers = AddPlayersUiState.open(organizer.phone, it.invitees)) }
    }

    /** "Choose from contacts" was tapped in the sheet: the system picker opens. No permission is needed. */
    fun onChooseFromContactsClick() {
        viewModelScope.launch { _events.send(NewGameEvent.PickContacts) }
    }

    /**
     * The system picker closed. [picked] reads the contacts it returned, or is null when nothing was
     * picked. Picked contacts join the sheet's selection; anyone already invited or selected, or at
     * the organizer's number, is skipped without a word.
     */
    fun onContactPickerDismissed(picked: DeviceContactsReader?) {
        val selection = if (picked == null) Selection.NONE else Selection.SOME
        eventManager.logEvent(AnalyticsEvent.ContactPickerDismiss(selection))
        if (picked == null) return
        val addPlayersState = _uiState.value.addPlayers ?: return
        viewModelScope.launch {
            when (val result = contactsRepository.choosePickedContacts(addPlayersState.list, picked)) {
                is TtcResult.Success -> updateAddPlayersState { it.afterContactsPicked(from = addPlayersState.list, updated = result.data) }
                is TtcResult.Failure -> messenger.show(result.error)
            }
        }
    }

    fun onPlayerNameChange(name: String) {
        updateAddPlayersState { it.withName(name) }
    }

    /** Digits only, capped at a full number, as the create-account phone field does. */
    fun onPlayerPhoneChange(phone: String) {
        updateAddPlayersState { it.withPhone(phone.toPhoneDigits().take(PHONE_NUMBER_LENGTH)) }
    }

    /**
     * The sheet's Add was tapped. A valid name and number join the selection and the form clears;
     * otherwise the fields show what's wrong.
     */
    fun onAddEnteredPlayer() {
        val addPlayersState = _uiState.value.addPlayers ?: return
        val result = contactsRepository.chooseEnteredPerson(addPlayersState.list, addPlayersState.name, addPlayersState.phone)
        updateAddPlayersState { it.afterEntry(result) }
    }

    /** A selected person's chip was tapped: they're taken out of the selection. */
    fun onUnchoosePlayer(person: NewInvitee) {
        updateAddPlayersState { it.unchoose(person) }
    }

    /** "Add N players" was tapped: everyone selected joins the invitees, and the sheet closes. */
    fun onAddChosenPlayers() {
        _uiState.update { state ->
            val addPlayersState = state.addPlayers ?: return@update state
            state.copy(invitees = addPlayersState.list.inviteChosen().invited, addPlayers = null)
        }
    }

    /** The sheet was dragged down or the scrim tapped: the selection is discarded, without asking. */
    fun onAddPlayersDismissed() {
        _uiState.update { it.copy(addPlayers = null) }
    }

    /** Apply [change] to the Add players state, if the sheet is open. */
    private fun updateAddPlayersState(change: (AddPlayersUiState) -> AddPlayersUiState) {
        _uiState.update { state -> state.addPlayers?.let { state.copy(addPlayers = change(it)) } ?: state }
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
