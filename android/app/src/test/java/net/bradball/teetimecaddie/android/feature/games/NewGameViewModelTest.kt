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
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import net.bradball.teetimecaddie.features.games.state.addPlayersUiState.AddPlayersUiState
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.state.NewGameUiState
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

    // The real SDK repository; each test's picker result is a fake reader.
    private val viewModel = NewGameViewModel(
        session,
        repository,
        ContactsRepositoryImpl(analytics.eventManager),
        messenger,
        analytics.eventManager,
    )

    /** The system picker returned [contacts]. */
    private fun pick(vararg contacts: DeviceContact) {
        viewModel.onContactPickerDismissed(object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> = contacts.toList()
        })
    }

    /** Choose [person] in the open sheet by picking them from the system picker. */
    private fun pick(person: NewInvitee) {
        pick(DeviceContact(person.name, person.name, listOf(DevicePhone(person.phone, "mobile"))))
    }

    /** Open the sheet, pick each of [invitees] one pick at a time, in that order, and add them. */
    private fun invite(vararg invitees: NewInvitee) {
        viewModel.onAddPlayersClick()
        invitees.forEach { pick(it) }
        viewModel.onAddChosenPlayers()
    }

    private val addPlayersState: AddPlayersUiState
        get() = checkNotNull(viewModel.uiState.value.addPlayers)

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
    fun selectingSlotsReplacesTheSelection() {
        viewModel.onSlotsChange(2)

        assertEquals(2, viewModel.uiState.value.slots)
    }

    // MARK: publishing

    // MARK: invitees

    private val matt = NewInvitee(name = "Matt Chen", phone = "(415) 555-0188")
    private val jeff = NewInvitee(name = "Jeff Morrison", phone = "(415) 555-0142")
    private val dana = NewInvitee(name = "Dana Park", phone = "415.555.0117")

    @Test
    fun addPlayersOpensTheSheetWithNothingSelected() {
        invite(matt)

        viewModel.onAddPlayersClick()

        assertEquals(AddPlayersUiState.open(previewPlayer.phone, listOf(matt)), addPlayersState)
    }

    @Test
    fun chooseFromContactsOpensTheSystemPicker() = runTest {
        val events = events()
        viewModel.onAddPlayersClick()

        viewModel.onChooseFromContactsClick()

        assertEquals(listOf<NewGameEvent>(NewGameEvent.PickContacts), events)
    }

    @Test
    fun pickedContactsAreSelectedNotInvitedUntilAdded() {
        viewModel.onAddPlayersClick()
        pick(matt)
        pick(jeff)

        assertEquals(listOf(matt, jeff), addPlayersState.chosen)
        assertTrue(viewModel.uiState.value.invitees.isEmpty())

        viewModel.onAddChosenPlayers()

        assertEquals(listOf(matt, jeff), viewModel.uiState.value.invitees)
        assertNull(viewModel.uiState.value.addPlayers)
    }

    @Test
    fun addedPlayersJoinTheEndOfTheList() {
        invite(matt)
        invite(jeff, dana)

        assertEquals(listOf(matt, jeff, dana), viewModel.uiState.value.invitees)
    }

    @Test
    fun peoplePickedTogetherAreSelectedSortedByName() {
        viewModel.onAddPlayersClick()

        pick(
            DeviceContact("c2", "Jeff Morrison", listOf(DevicePhone("(415) 555-0142", "Mobile"))),
            DeviceContact("c3", "Dana Park", listOf(DevicePhone("415.555.0117", "Home"))),
        )

        assertEquals(listOf(dana, jeff), addPlayersState.chosen)
    }

    @Test
    fun pickingSomeoneAlreadyInvitedOrSelectedSkipsThem() {
        invite(matt)
        viewModel.onAddPlayersClick()
        pick(jeff)

        pick(NewInvitee("Matthew Chen", "+1 415 555 0188"))
        pick(NewInvitee("Jeff M", "4155550142"))

        assertEquals(listOf(jeff), addPlayersState.chosen)
    }

    @Test
    fun aPickedContactIsSelectedAtItsMobileNumber() {
        viewModel.onAddPlayersClick()

        pick(
            DeviceContact(
                "c1",
                "Jeff Morrison",
                listOf(DevicePhone("502-555-0199", "Work"), DevicePhone("(415) 555-0142", "Mobile", isMobile = true))
            )
        )

        assertEquals(listOf(jeff), addPlayersState.chosen)
    }

    @Test
    fun pickedNumbersAGameCantBePublishedWithAreLeftOut() {
        viewModel.onAddPlayersClick()

        pick(
            DeviceContact("c1", "Pizza Place", listOf(DevicePhone("611", "Other"))),
            DeviceContact("c2", "  ", listOf(DevicePhone("4155550101", "Mobile"))),
        )

        assertTrue(addPlayersState.chosen.isEmpty())
    }

    @Test
    fun aFailedReadSaysSoAndLeavesTheSelection() = runTest {
        val messages = messages()
        viewModel.onAddPlayersClick()
        pick(matt)

        viewModel.onContactPickerDismissed(object : DeviceContactsReader {
            override suspend fun readContacts(): List<DeviceContact> = throw SecurityException("grant expired")
        })

        assertEquals(listOf(matt), addPlayersState.chosen)
        assertEquals(listOf(TtcMessage(GameErrors.CONTACTS_LOAD_FAILED.message)), messages)
    }

    @Test
    fun anEnteredPlayerIsSelectedAndTheFormClears() {
        viewModel.onAddPlayersClick()
        viewModel.onPlayerNameChange(" Sam  Tanaka ")
        viewModel.onPlayerPhoneChange("(415) 555-0123")

        viewModel.onAddEnteredPlayer()

        assertEquals(listOf(NewInvitee("Sam Tanaka", "4155550123")), addPlayersState.chosen)
        assertEquals("", addPlayersState.name)
        assertEquals("", addPlayersState.phone)
        assertNotNull(addPlayersState.chosenMessage)
    }

    @Test
    fun anInvalidEntryShowsItsErrorsAndSelectsNobody() {
        viewModel.onAddPlayersClick()

        viewModel.onAddEnteredPlayer()

        assertTrue(addPlayersState.chosen.isEmpty())
        assertEquals(GMR.strings.add_players_error_no_name, addPlayersState.nameError)
        assertEquals(GMR.strings.add_players_error_no_phone, addPlayersState.phoneError)
    }

    @Test
    fun theEnteredNumberKeepsOnlyItsFirstTenDigits() {
        viewModel.onAddPlayersClick()

        viewModel.onPlayerPhoneChange("(415) 555-01234")

        assertEquals("4155550123", addPlayersState.phone)
    }

    @Test
    fun tappingAChipUnselectsThem() {
        viewModel.onAddPlayersClick()
        pick(matt)
        pick(jeff)

        viewModel.onUnchoosePlayer(matt)

        assertEquals(listOf(jeff), addPlayersState.chosen)
    }

    @Test
    fun dismissingTheSheetDiscardsTheSelection() {
        invite(matt)
        viewModel.onAddPlayersClick()
        pick(jeff)

        viewModel.onAddPlayersDismissed()

        assertNull(viewModel.uiState.value.addPlayers)
        assertEquals(listOf(matt), viewModel.uiState.value.invitees)
    }

    @Test
    fun aPickWithTheSheetClosedChangesNothing() {
        pick(matt)

        assertNull(viewModel.uiState.value.addPlayers)
        assertTrue(viewModel.uiState.value.invitees.isEmpty())
    }

    @Test
    fun removingAnInviteeKeepsTheRestInOrder() {
        invite(matt, jeff, dana)

        viewModel.onRemoveInvitee(jeff)

        assertEquals(listOf(matt, dana), viewModel.uiState.value.invitees)
    }

    @Test
    fun addPlayersLabel_readsAddMoreOnceAnyoneIsAdded() {
        assertEquals(GMR.strings.new_game_add_players, viewModel.uiState.value.addPlayersLabel)

        invite(matt)
        assertEquals(GMR.strings.new_game_add_more_players, viewModel.uiState.value.addPlayersLabel)

        viewModel.onRemoveInvitee(matt)
        assertEquals(GMR.strings.new_game_add_players, viewModel.uiState.value.addPlayersLabel)
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

        assertEquals(listOf(matt, jeff), repository.publishRequests.single().invitees)
        assertEquals(listOf<NewGameEvent>(NewGameEvent.Published), events)
    }

    @Test
    fun publishClick_asksAgainOnceEveryInviteeIsRemoved() = runTest {
        val events = events()
        fillIn()
        invite(matt)
        viewModel.onRemoveInvitee(matt)

        viewModel.onPublishClick()

        assertEquals(listOf<NewGameEvent>(NewGameEvent.ConfirmNoInvitees), events)
        assertTrue(repository.publishRequests.isEmpty())
    }

    // MARK: publishing

    @Test
    fun publishClick_asksFirstAndPublishesNothing() = runTest {
        val events = events()
        fillIn()

        viewModel.onPublishClick()

        assertEquals(listOf<NewGameEvent>(NewGameEvent.ConfirmNoInvitees), events)
        assertTrue(repository.publishRequests.isEmpty())
    }

    @Test
    fun publishClick_doesNothingWhenTheFormIsIncomplete() = runTest {
        val events = events()
        viewModel.onCourseChange("Pebble Beach")

        viewModel.onPublishClick()

        assertTrue(events.isEmpty())
        assertTrue(repository.publishRequests.isEmpty())
    }

    @Test
    fun keepEditing_leavesTheFormAsItWas() {
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
        assertEquals(listOf(NewGameEvent.ConfirmNoInvitees, NewGameEvent.Published), events)
    }

    @Test
    fun publishAnyway_showsProgressWhilePublishing() = runTest {
        repository.publishGate = CompletableDeferred()
        fillIn()
        viewModel.onPublishClick()

        viewModel.onPublishAnyway()

        assertTrue(viewModel.uiState.value.isSubmitting)
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
        // Asked first, but never left the form.
        assertEquals(listOf<NewGameEvent>(NewGameEvent.ConfirmNoInvitees), events)
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
    fun addPlayersIsLogged() {
        viewModel.onAddPlayersClick()

        assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.AddFromContactsClick), analytics.events)
    }

    @Test
    fun aPickLogsTheDismissWithASelectionAndHowManyWereAdded() {
        invite(matt)
        viewModel.onAddPlayersClick()
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
}
