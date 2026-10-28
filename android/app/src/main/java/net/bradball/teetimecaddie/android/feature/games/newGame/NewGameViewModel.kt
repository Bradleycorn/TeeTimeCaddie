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
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsAccess
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsPermission
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.EventManager
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.TtcResult
import net.bradball.teetimecaddie.features.games.ContactsRepository
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
 * @property contactsSheet The contacts sheet that's open, if any.
 */
data class NewGameUiState(
    val course: String = "",
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val slots: Int = MAX_TEE_TIME_SLOTS,
    val invitees: List<NewInvitee> = emptyList(),
    val showNoInviteeConfirm: Boolean = false,
    val contactsSheet: ContactsSheet? = null,
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

/** The sheets that adding from contacts can open, one at a time. */
sealed interface ContactsSheet {

    /** Why the app wants contacts, before the system asks. */
    data object Explanation : ContactsSheet

    /** Contacts access was turned down, and only Settings can turn it back on. */
    data object AccessOff : ContactsSheet

    /**
     * The contacts picker.
     *
     * @property contacts Everyone who can be invited.
     * @property query What's typed in the search field.
     * @property shown The [contacts] that match [query].
     * @property checkedIds The checked contacts' ids, in the order they were checked. Checked
     *   contacts stay checked while the search hides them.
     */
    data class Picker(
        val contacts: List<InvitableContact>,
        val query: String = "",
        val shown: List<InvitableContact> = contacts,
        val checkedIds: List<String> = emptyList(),
    ) : ContactsSheet {

        fun isChecked(contact: InvitableContact): Boolean = contact.id in checkedIds

        /**
         * Why the list is empty, when it is: the device has no contacts that can be invited, or
         * none match the search. Formatted with the trimmed [query].
         */
        val emptyMessage: StringResource?
            get() = when {
                contacts.isEmpty() -> GMR.strings.contacts_picker_no_contacts
                shown.isEmpty() -> GMR.strings.contacts_picker_no_match
                else -> null
            }
    }
}

sealed interface NewGameEvent {
    /** The game is published; leave the form. */
    data object Published : NewGameEvent

    /** Show the system's contacts access prompt, and report the answer to `onContactsAccessResult`. */
    data object RequestContactsAccess : NewGameEvent

    /** Open TeeTimeCaddie's page in the device Settings, where contacts access can be turned on. */
    data object OpenAppSettings : NewGameEvent
}

@HiltViewModel
class NewGameViewModel @Inject constructor(
    private val sessionState: StateFlow<@JvmSuppressWildcards SessionState>,
    private val gamesRepository: GamesRepository,
    private val contactsRepository: ContactsRepository,
    private val contactsPermission: ContactsPermission,
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
     * "Add from contacts" was tapped. With access on, open the picker. Otherwise explain first, or,
     * once access has been turned down, say how to turn it back on.
     */
    fun onAddFromContactsClick() {
        when (contactsPermission.access) {
            ContactsAccess.GRANTED -> openPicker()
            ContactsAccess.NOT_ASKED -> _uiState.update { it.copy(contactsSheet = ContactsSheet.Explanation) }
            ContactsAccess.DENIED -> _uiState.update { it.copy(contactsSheet = ContactsSheet.AccessOff) }
        }
    }

    /** "Continue" on the explanation sheet: the system asks next. The sheet closes itself. */
    fun onContactsContinue() {
        viewModelScope.launch { _events.send(NewGameEvent.RequestContactsAccess) }
    }

    /** The system prompt was answered. Allowing opens the picker straight away. */
    fun onContactsAccessResult(granted: Boolean) {
        contactsPermission.markRequested()
        if (granted) openPicker()
    }

    /** "Open Settings" on the access-off sheet. The sheet closes itself. */
    fun onOpenAppSettings() {
        viewModelScope.launch { _events.send(NewGameEvent.OpenAppSettings) }
    }

    /**
     * The open contacts sheet closed, by any route. Closing the picker this way discards that
     * visit's checks; only Done applies them.
     */
    fun onDismissContactsSheet() {
        _uiState.update { it.copy(contactsSheet = null) }
    }

    fun onContactsQueryChange(query: String) {
        updatePicker { it.copy(query = query, shown = contactsRepository.search(it.contacts, query)) }
    }

    /** A picker row was tapped: check it, or uncheck it. */
    fun onToggleContact(contact: InvitableContact) {
        updatePicker { picker ->
            val checkedIds = if (picker.isChecked(contact)) {
                picker.checkedIds - contact.id
            } else {
                picker.checkedIds + contact.id
            }
            picker.copy(checkedIds = checkedIds)
        }
    }

    /**
     * Done: the checked contacts become the invitees. Unchecked people are removed, and the rest
     * keep their order. The sheet closes itself, through [onDismissContactsSheet].
     */
    fun onContactsDone() {
        val picker = _uiState.value.contactsSheet as? ContactsSheet.Picker ?: return
        _uiState.update {
            it.copy(invitees = contactsRepository.pickInvitees(it.invitees, picker.contacts, picker.checkedIds))
        }
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

    /** Read the contacts and open the picker, with everyone already invited checked. */
    private fun openPicker() {
        viewModelScope.launch {
            when (val result = contactsRepository.invitableContacts()) {
                is TtcResult.Success -> _uiState.update { state ->
                    val invited = contactsRepository.invitedContactIds(result.data, state.invitees)
                    val checkedIds = result.data.map { it.id }.filter { it in invited }
                    state.copy(contactsSheet = ContactsSheet.Picker(result.data, checkedIds = checkedIds))
                }

                is TtcResult.Failure -> messenger.show(result.error)
            }
        }
    }

    private fun updatePicker(transform: (ContactsSheet.Picker) -> ContactsSheet.Picker) {
        _uiState.update { state ->
            val picker = state.contactsSheet as? ContactsSheet.Picker ?: return@update state
            state.copy(contactsSheet = transform(picker))
        }
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
