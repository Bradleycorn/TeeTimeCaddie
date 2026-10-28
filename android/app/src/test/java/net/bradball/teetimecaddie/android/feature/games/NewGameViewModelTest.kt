package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.feature.games.contacts.ContactsAccess
import net.bradball.teetimecaddie.android.feature.games.newGame.ContactsSheet
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameEvent
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameUiState
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.android.testing.RecordingEventManager
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.models.InvitableContact
import net.bradball.teetimecaddie.core.models.NewInvitee
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.games.ContactsRepositoryImpl
import net.bradball.teetimecaddie.features.games.DeviceContact
import net.bradball.teetimecaddie.features.games.DeviceContactsReader
import net.bradball.teetimecaddie.features.games.DevicePhone
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.session.SessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NewGameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val date = LocalDate(2026, 6, 13)
    private val time = LocalTime(9, 0)

    private val session = MutableStateFlow<SessionState>(SessionState.SignedIn(previewPlayer))
    private val repository = FakeGamesRepository()
    private val messenger = TtcMessenger()
    private val analytics = RecordingEventManager()

    // The real repository over a fake reader, so these tests cover what the picker really shows.
    private var deviceContacts = listOf(
        DeviceContact("c1", "Matt Chen", listOf(DevicePhone("(415) 555-0188", "mobile"))),
        DeviceContact("c2", "Jeff Morrison", listOf(DevicePhone("(415) 555-0142", "mobile"), DevicePhone("502-555-0199", "work"))),
        DeviceContact("c3", "Dana Park", listOf(DevicePhone("415.555.0117", "home"))),
    )
    private var contactsFailure: Exception? = null
    private val contactsRepository = ContactsRepositoryImpl(
        analytics.eventManager,
        object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> = contactsFailure?.let { throw it } ?: deviceContacts
        },
    )
    private val permission = FakeContactsPermission()

    private val viewModel = NewGameViewModel(
        session, repository, contactsRepository, permission, messenger, analytics.eventManager
    )

    private fun fillIn(course: String = "Pebble Beach") {
        viewModel.onCourseChange(course)
        viewModel.onDateSelected(date)
        viewModel.onTimeSelected(time)
    }

    private fun TestScope.messages(): List<TtcMessage> = mutableListOf<TtcMessage>().also { list ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages.toList(list) }
    }

    private fun TestScope.events(): List<NewGameEvent> = mutableListOf<NewGameEvent>().also { list ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(list) }
    }

    // MARK: the form

    @Test
    fun slotsDefaultToFour() {
        assertEquals(4, viewModel.uiState.value.slots)
    }

    @Test
    fun canSubmit_needsACourseADateAndATime() {
        assertFalse(NewGameUiState().canSubmit)
        assertFalse(NewGameUiState(course = "Pebble Beach", date = date).canSubmit)
        assertFalse(NewGameUiState(course = "Pebble Beach", time = time).canSubmit)
        assertFalse(NewGameUiState(date = date, time = time).canSubmit)
        assertTrue(NewGameUiState(course = "Pebble Beach", date = date, time = time).canSubmit)
    }

    @Test
    fun canSubmit_ignoresACourseOfOnlySpaces() {
        assertFalse(NewGameUiState(course = "   ", date = date, time = time).canSubmit)
    }

    @Test
    fun canSubmit_isFalseWhilePublishing() {
        assertFalse(
            NewGameUiState(course = "Pebble Beach", date = date, time = time, isSubmitting = true).canSubmit
        )
    }

    @Test
    fun selectingSlotsReplacesTheSelection() {
        viewModel.onSlotsChange(2)

        assertEquals(2, viewModel.uiState.value.slots)
    }

    // MARK: publishing

    // MARK: invitees

    // The picker's rows for the device contacts above, sorted by name, and the invitees they become.
    private val danaContact = InvitableContact("c3|4155550117", "Dana Park", "4155550117", "home")
    private val jeffContact = InvitableContact("c2|4155550142", "Jeff Morrison", "4155550142", "mobile")
    private val jeffWorkContact = InvitableContact("c2|5025550199", "Jeff Morrison", "5025550199", "work")
    private val mattContact = InvitableContact("c1|4155550188", "Matt Chen", "4155550188", "mobile")
    private val matt = mattContact.toNewInvitee()
    private val jeff = jeffContact.toNewInvitee()
    private val dana = danaContact.toNewInvitee()

    private fun picker(): ContactsSheet.Picker = viewModel.uiState.value.contactsSheet as ContactsSheet.Picker

    /** Pick exactly [invitees] in the contacts picker, in that order, and tap Done. */
    private fun invite(vararg invitees: NewInvitee) {
        permission.access = ContactsAccess.GRANTED
        viewModel.onAddFromContactsClick()
        val wanted = invitees.map { invitee -> picker().contacts.first { it.toNewInvitee() == invitee } }
        picker().contacts.filter { picker().isChecked(it) && it !in wanted }.forEach(viewModel::onToggleContact)
        wanted.filterNot { picker().isChecked(it) }.forEach(viewModel::onToggleContact)
        viewModel.onContactsDone()
        viewModel.onDismissContactsSheet()
    }

    @Test
    fun uncheckingSomeoneInThePickerRemovesThem() {
        invite(matt, jeff)
        invite(jeff)

        assertEquals(listOf(jeff), viewModel.uiState.value.invitees)
    }

    @Test
    fun removingAnInviteeKeepsTheRestInOrder() {
        invite(matt, jeff, dana)

        viewModel.onRemoveInvitee(jeff)

        assertEquals(listOf(matt, dana), viewModel.uiState.value.invitees)
    }

    @Test
    fun addFromContactsLabel_readsAddMoreOnceAnyoneIsAdded() {
        assertEquals(GMR.strings.new_game_add_from_contacts, viewModel.uiState.value.addFromContactsLabel)

        invite(matt)
        assertEquals(GMR.strings.new_game_add_more_from_contacts, viewModel.uiState.value.addFromContactsLabel)

        viewModel.onRemoveInvitee(matt)
        assertEquals(GMR.strings.new_game_add_from_contacts, viewModel.uiState.value.addFromContactsLabel)
    }

    @Test
    fun inviteesNeverAffectWhetherPublishIsEnabled() {
        invite(matt)
        assertFalse(viewModel.uiState.value.canSubmit)

        fillIn()
        viewModel.onRemoveInvitee(matt)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun publishClick_withInviteesPublishesThemWithoutAsking() = runTest {
        val events = events()
        fillIn()
        invite(matt, jeff)

        viewModel.onPublishClick()

        assertFalse(viewModel.uiState.value.showNoInviteeConfirm)
        assertEquals(listOf(matt, jeff), repository.publishRequests.single().invitees)
        assertEquals(listOf<NewGameEvent>(NewGameEvent.Published), events)
    }

    @Test
    fun publishClick_asksAgainOnceEveryInviteeIsRemoved() {
        fillIn()
        invite(matt)
        viewModel.onRemoveInvitee(matt)

        viewModel.onPublishClick()

        assertTrue(viewModel.uiState.value.showNoInviteeConfirm)
        assertTrue(repository.publishRequests.isEmpty())
    }

    // MARK: publishing

    @Test
    fun publishClick_asksFirstAndPublishesNothing() {
        fillIn()

        viewModel.onPublishClick()

        assertTrue(viewModel.uiState.value.showNoInviteeConfirm)
        assertTrue(repository.publishRequests.isEmpty())
    }

    @Test
    fun publishClick_doesNothingWhenTheFormIsIncomplete() {
        viewModel.onCourseChange("Pebble Beach")

        viewModel.onPublishClick()

        assertFalse(viewModel.uiState.value.showNoInviteeConfirm)
    }

    @Test
    fun keepEditing_closesTheAlertAndLeavesTheForm() {
        fillIn()
        viewModel.onSlotsChange(3)
        viewModel.onPublishClick()

        viewModel.onKeepEditing()

        assertEquals(
            NewGameUiState(course = "Pebble Beach", date = date, time = time, slots = 3),
            viewModel.uiState.value
        )
        assertTrue(repository.publishRequests.isEmpty())
    }

    @Test
    fun publishAnyway_publishesWhatWasEntered() = runTest {
        fillIn(course = " pebble beach ")
        viewModel.onSlotsChange(2)
        viewModel.onPublishClick()

        viewModel.onPublishAnyway()

        assertEquals(
            listOf(FakeGamesRepository.PublishRequest(previewPlayer, " pebble beach ", date, time, 2)),
            repository.publishRequests
        )
    }

    @Test
    fun publishAnyway_confirmsWithTheStoredSpellingAndLeaves() = runTest {
        val messages = messages()
        val events = events()
        repository.storedCourseName = { "Pebble Beach" }
        fillIn(course = "pebble beach")
        viewModel.onPublishClick()

        viewModel.onPublishAnyway()

        assertEquals(listOf(TtcMessage(GMR.strings.new_game_published_message, listOf("Pebble Beach"))), messages)
        assertEquals(listOf<NewGameEvent>(NewGameEvent.Published), events)
    }

    @Test
    fun publishAnyway_showsProgressWhilePublishing() = runTest {
        repository.publishGate = CompletableDeferred()
        fillIn()
        viewModel.onPublishClick()

        viewModel.onPublishAnyway()

        assertTrue(viewModel.uiState.value.isSubmitting)
        assertFalse(viewModel.uiState.value.showNoInviteeConfirm)
        repository.publishGate?.complete(Unit)
    }

    @Test
    fun publishFailure_reportsTheErrorAndStays() = runTest {
        val messages = messages()
        val events = events()
        repository.publishFailure = GameErrors.PUBLISH_FAILED
        fillIn()
        viewModel.onPublishClick()

        viewModel.onPublishAnyway()

        assertEquals(listOf(TtcMessage(GameErrors.PUBLISH_FAILED.message)), messages)
        assertTrue(events.isEmpty())
        assertFalse(viewModel.uiState.value.isSubmitting)
        assertEquals("Pebble Beach", viewModel.uiState.value.course)
    }

    // MARK: contacts access

    @Test
    fun theFirstTapExplainsBeforeTheSystemAsks() {
        viewModel.onAddFromContactsClick()

        assertEquals(ContactsSheet.Explanation, viewModel.uiState.value.contactsSheet)
    }

    @Test
    fun continueAsksTheSystem() = runTest {
        val events = events()
        viewModel.onAddFromContactsClick()

        viewModel.onContactsContinue()

        assertEquals(listOf<NewGameEvent>(NewGameEvent.RequestContactsAccess), events)
    }

    @Test
    fun notNowLeavesTheFormAsItWasAndTheNextTapExplainsAgain() {
        viewModel.onAddFromContactsClick()
        viewModel.onDismissContactsSheet()

        assertNull(viewModel.uiState.value.contactsSheet)
        assertFalse(permission.requested)

        viewModel.onAddFromContactsClick()
        assertEquals(ContactsSheet.Explanation, viewModel.uiState.value.contactsSheet)
    }

    @Test
    fun allowingAccessOpensThePickerStraightAway() {
        viewModel.onAddFromContactsClick()
        viewModel.onContactsContinue()
        viewModel.onDismissContactsSheet()

        viewModel.onContactsAccessResult(granted = true)

        assertEquals(listOf(danaContact, jeffContact, jeffWorkContact, mattContact), picker().contacts)
        assertTrue(permission.requested)
    }

    @Test
    fun denyingAccessAddsNobody() {
        viewModel.onAddFromContactsClick()
        viewModel.onContactsContinue()
        viewModel.onDismissContactsSheet()

        viewModel.onContactsAccessResult(granted = false)

        assertNull(viewModel.uiState.value.contactsSheet)
        assertTrue(viewModel.uiState.value.invitees.isEmpty())
        assertTrue(permission.requested)
    }

    @Test
    fun onceAccessIsOffTheTapSaysSoAndOffersSettings() = runTest {
        val events = events()
        permission.access = ContactsAccess.DENIED

        viewModel.onAddFromContactsClick()
        assertEquals(ContactsSheet.AccessOff, viewModel.uiState.value.contactsSheet)

        viewModel.onOpenAppSettings()
        assertEquals(listOf<NewGameEvent>(NewGameEvent.OpenAppSettings), events)
    }

    @Test
    fun withAccessOnTheTapGoesStraightToThePicker() {
        permission.access = ContactsAccess.GRANTED

        viewModel.onAddFromContactsClick()

        assertTrue(picker().checkedIds.isEmpty())
    }

    @Test
    fun aFailedReadSaysSoAndOpensNothing() = runTest {
        val messages = messages()
        permission.access = ContactsAccess.GRANTED
        contactsFailure = IllegalStateException("no contacts provider")

        viewModel.onAddFromContactsClick()

        assertNull(viewModel.uiState.value.contactsSheet)
        assertEquals(listOf(TtcMessage(GameErrors.CONTACTS_LOAD_FAILED.message)), messages)
    }

    // MARK: contacts picker

    @Test
    fun thePickerReopensWithEveryoneInvitedChecked() {
        invite(jeff, matt)

        viewModel.onAddFromContactsClick()

        assertEquals(listOf(jeffContact.id, mattContact.id), picker().checkedIds)
    }

    @Test
    fun doneAddsNewlyCheckedPeopleAfterTheOthersInTheOrderChecked() {
        invite(matt)
        viewModel.onAddFromContactsClick()

        viewModel.onToggleContact(jeffWorkContact)
        viewModel.onToggleContact(danaContact)
        assertEquals(3, picker().checkedIds.size)
        viewModel.onContactsDone()

        assertEquals(listOf(matt, jeffWorkContact.toNewInvitee(), dana), viewModel.uiState.value.invitees)
    }

    @Test
    fun closingThePickerWithoutDoneDiscardsTheChecks() {
        invite(matt)
        viewModel.onAddFromContactsClick()
        viewModel.onToggleContact(mattContact)
        viewModel.onToggleContact(danaContact)

        viewModel.onDismissContactsSheet()

        assertEquals(listOf(matt), viewModel.uiState.value.invitees)
        viewModel.onAddFromContactsClick()
        assertEquals(listOf(mattContact.id), picker().checkedIds)
    }

    @Test
    fun doneWithNobodyCheckedClearsTheList() {
        invite(matt, jeff)
        viewModel.onAddFromContactsClick()
        viewModel.onToggleContact(mattContact)
        viewModel.onToggleContact(jeffContact)

        viewModel.onContactsDone()

        assertTrue(viewModel.uiState.value.invitees.isEmpty())
    }

    @Test
    fun searchNarrowsTheListAndKeepsHiddenPeopleChecked() {
        permission.access = ContactsAccess.GRANTED
        viewModel.onAddFromContactsClick()
        viewModel.onToggleContact(mattContact)

        viewModel.onContactsQueryChange("MORR")

        assertEquals(listOf(jeffContact, jeffWorkContact), picker().shown)
        assertEquals(listOf(mattContact.id), picker().checkedIds)

        viewModel.onContactsQueryChange("")
        assertEquals(picker().contacts, picker().shown)
    }

    @Test
    fun theEmptyMessageSaysWhyTheListIsEmpty() {
        permission.access = ContactsAccess.GRANTED
        viewModel.onAddFromContactsClick()
        assertNull(picker().emptyMessage)

        viewModel.onContactsQueryChange("zz")
        assertEquals(GMR.strings.contacts_picker_no_match, picker().emptyMessage)

        deviceContacts = emptyList()
        viewModel.onDismissContactsSheet()
        viewModel.onAddFromContactsClick()
        assertEquals(GMR.strings.contacts_picker_no_contacts, picker().emptyMessage)
    }

    // MARK: analytics

    @Test
    fun selectingSlotsLogsTheCountTapped() {
        viewModel.onSlotsChange(3)

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.PlayerSlotsClick(playerCount = 3)), analytics.events)
    }

    @Test
    fun pickerModeTogglesAreLogged() {
        viewModel.onDateInputToggle()
        viewModel.onTimeInputToggle()

        assertEquals(listOf(AnalyticsEvent.DateInputToggle, AnalyticsEvent.TimeInputToggle), analytics.events)
    }

    @Test
    fun cancelIsLogged() {
        viewModel.onCancel()

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.NewGameCancel), analytics.events)
    }

    @Test
    fun alertButtonsLogTheResponse() = runTest {
        fillIn()
        viewModel.onPublishClick()
        viewModel.onKeepEditing()
        viewModel.onPublishClick()
        viewModel.onPublishAnyway()

        assertEquals(
            listOf(
                AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.KEEP_EDITING),
                AnalyticsEvent.NoInvites(AnalyticsEvent.NoInvites.Response.PUBLISH_ANYWAY),
            ),
            analytics.events
        )
    }

    @Test
    fun dismissingTheAlertWithoutAButtonClosesItButLogsNothing() {
        fillIn()
        viewModel.onPublishClick()

        viewModel.onDismissNoInviteeConfirm()

        assertFalse(viewModel.uiState.value.showNoInviteeConfirm)
        assertTrue(analytics.events.isEmpty())
    }
}
