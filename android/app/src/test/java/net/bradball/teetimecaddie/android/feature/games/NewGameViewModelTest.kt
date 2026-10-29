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
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameEvent
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameUiState
import net.bradball.teetimecaddie.android.feature.games.newGame.NewGameViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.android.testing.RecordingEventManager
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent.ContactPickerDismiss.Selection
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
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

    // The real SDK repository, over whatever reader the system picker's result is wrapped in.
    private val viewModel = NewGameViewModel(
        session,
        repository,
        { reader -> ContactsRepositoryImpl(analytics.eventManager, reader) },
        messenger,
        analytics.eventManager,
    )

    /** The system picker returned [contacts]. */
    private fun pick(vararg contacts: DeviceContact) {
        viewModel.onContactPickerDismissed(object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> = contacts.toList()
        })
    }

    /** Pick each of [invitees] from the system picker, one pick at a time, in that order. */
    private fun invite(vararg invitees: NewInvitee) {
        invitees.forEach { pick(DeviceContact(it.name, it.name, listOf(DevicePhone(it.phone, "mobile")))) }
    }

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

    private val matt = NewInvitee(name = "Matt Chen", phone = "(415) 555-0188")
    private val jeff = NewInvitee(name = "Jeff Morrison", phone = "(415) 555-0142")

    @Test
    fun addFromContactsOpensTheSystemPicker() = runTest {
        val events = events()

        viewModel.onAddFromContactsClick()

        assertEquals(listOf<NewGameEvent>(NewGameEvent.PickContacts), events)
    }

    @Test
    fun pickedContactsJoinTheEndOfTheListInTheOrderPicked() {
        invite(matt)
        invite(jeff, NewInvitee("Dana Park", "415.555.0117"))

        assertEquals(listOf(matt, jeff, NewInvitee("Dana Park", "415.555.0117")), viewModel.uiState.value.invitees)
    }

    @Test
    fun peoplePickedTogetherJoinSortedByName() {
        invite(matt)

        pick(
            DeviceContact("c2", "Jeff Morrison", listOf(DevicePhone("(415) 555-0142", "Mobile"))),
            DeviceContact("c3", "Dana Park", listOf(DevicePhone("415.555.0117", "Home"))),
        )

        assertEquals(
            listOf(matt, NewInvitee("Dana Park", "415.555.0117"), jeff),
            viewModel.uiState.value.invitees
        )
    }

    @Test
    fun pickingSomeoneAlreadyInvitedDoesNotAddThemTwice() {
        invite(matt)

        invite(NewInvitee("Matt Chen", "+1 415 555 0188"), jeff)

        assertEquals(listOf(matt, jeff), viewModel.uiState.value.invitees)
    }

    @Test
    fun aPickedContactIsInvitedOnceAtItsMobileNumber() {
        pick(
            DeviceContact(
                "c1",
                "Jeff Morrison",
                listOf(DevicePhone("502-555-0199", "Work"), DevicePhone("(415) 555-0142", "Mobile", isMobile = true))
            )
        )

        assertEquals(listOf(NewInvitee("Jeff Morrison", "(415) 555-0142")), viewModel.uiState.value.invitees)
    }

    @Test
    fun pickedNumbersAGameCantBePublishedWithAreLeftOut() {
        pick(
            DeviceContact("c1", "Pizza Place", listOf(DevicePhone("611", "Other"))),
            DeviceContact("c2", "  ", listOf(DevicePhone("4155550101", "Mobile"))),
        )

        assertTrue(viewModel.uiState.value.invitees.isEmpty())
    }

    @Test
    fun aFailedReadSaysSoAndLeavesTheList() = runTest {
        val messages = messages()
        invite(matt)

        viewModel.onContactPickerDismissed(object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> = throw SecurityException("grant expired")
        })

        assertEquals(listOf(matt), viewModel.uiState.value.invitees)
        assertEquals(listOf(TtcMessage(GameErrors.CONTACTS_LOAD_FAILED.message)), messages)
    }

    @Test
    fun removingAnInviteeKeepsTheRestInOrder() {
        invite(matt, jeff, NewInvitee("Dana Park", "4155550117"))

        viewModel.onRemoveInvitee(jeff)

        assertEquals(listOf(matt, NewInvitee("Dana Park", "4155550117")), viewModel.uiState.value.invitees)
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
    fun addFromContactsIsLogged() {
        viewModel.onAddFromContactsClick()

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddFromContactsClick), analytics.events)
    }

    @Test
    fun aPickLogsTheDismissWithASelectionAndHowManyWereAdded() {
        invite(matt)
        analytics.events.clear()

        pick(
            DeviceContact("c1", "Matt Chen", listOf(DevicePhone("4155550188", "Mobile", isMobile = true))),
            DeviceContact("c2", "Jeff Morrison", listOf(DevicePhone("(415) 555-0142", "Mobile", isMobile = true))),
        )

        assertEquals(
            listOf(
                AnalyticsEvent.ContactPickerDismiss(Selection.SOME),
                AnalyticsEvent.AddInvitees(pickedCount = 2, addedCount = 1),
            ),
            analytics.events
        )
    }

    @Test
    fun closingThePickerWithNothingPickedLogsADismissWithNoSelection() {
        invite(matt)
        analytics.events.clear()

        viewModel.onContactPickerDismissed(null)

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.ContactPickerDismiss(Selection.NONE)), analytics.events)
        assertEquals(listOf(matt), viewModel.uiState.value.invitees)
    }

    @Test
    fun removingAnInviteeIsLogged() {
        invite(matt)
        analytics.events.clear()

        viewModel.onRemoveInvitee(matt)

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.RemoveInviteeClick), analytics.events)
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
