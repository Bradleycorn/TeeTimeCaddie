package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import net.bradball.teetimecaddie.android.feature.games.gameDetails.GameDetailsViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.android.testing.RecordingEventManager
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.core.models.previewPlayerList
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.RsvpUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameDetailsUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.TeeTimePlayerStatus
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.GameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.toDetailsUiState
import net.bradball.teetimecaddie.session.SessionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<SessionState>(SessionState.SignedIn(previewPlayer))

    private val analytics = RecordingEventManager()

    private val messenger = TtcMessenger()

    private fun viewModel(gameId: String, repository: FakeGamesRepository = FakeGamesRepository(previewGame)) =
        GameDetailsViewModel(gameId, session, repository, analytics.eventManager, messenger)

    /** Collects every message the ViewModel shows, from now on. */
    private fun TestScope.messages(): List<TtcMessage> = mutableListOf<TtcMessage>().also { list ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages.toList(list) }
    }

    // Sam's game at Pebble Beach, inviting the signed-in player by their number.
    private val invitation = previewGame.copy(
        id = "invitation",
        organizerId = "sam",
        teeTimes = listOf(previewGame.teeTimes.single().copy(players = listOf(GamePlayer("sam", "Sam Pruitt", "5025559001")))),
        invitees = listOf(GameInvitee("Brad", previewPlayer.phone, playerId = null, status = InviteStatus.PENDING)),
    )

    /** Subscribes, as the screen does, then returns the loaded state. Loading starts on subscription. */
    private fun TestScope.loaded(viewModel: GameDetailsViewModel): GameDetailsUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        return viewModel.uiState.value
    }

    @Test
    fun loadsNothingUntilObserved() {
        val repository = FakeGamesRepository(previewGame)

        val viewModel = viewModel(previewGame.id, repository)

        assertEquals(GameDetailsUiState.Loading, viewModel.uiState.value)
        assertEquals(emptyList<String>(), repository.observedGameIds)
    }

    @Test
    fun showsTheGame() = runTest {
        val state = loaded(viewModel(previewGame.id))

        assertEquals(previewGame.toDetailsUiState(viewer = previewPlayer), state)
    }

    @Test
    fun theOrganizerFillsOneSlot() = runTest {
        val state = loaded(viewModel(previewGame.id)) as GameDetailsUiState.Content
        val teeTime = state.game.teeTimes.single()

        assertEquals(previewPlayer.name, teeTime.players.single().name)
        assertEquals(TeeTimePlayerStatus.ORGANIZER, teeTime.players.single().status)
        assertEquals(previewGame.teeTimes.single().slots - 1, teeTime.openSlots)
    }

    @Test
    fun aOneSlotGameHasNoOpenSlots() = runTest {
        val oneSlot = previewGame.copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) })

        val state = loaded(viewModel(oneSlot.id, FakeGamesRepository(oneSlot))) as GameDetailsUiState.Content

        assertEquals(0, state.game.teeTimes.single().openSlots)
    }

    @Test
    fun someoneWithNoPartInTheGameSeesTheNotFoundError() = runTest {
        // Neither the organizer nor invited.
        session.value = SessionState.SignedIn(previewPlayerList[1])

        val state = loaded(viewModel(previewGame.id))

        assertEquals(LocalizedText(GameErrors.NOT_FOUND.message), (state as GameDetailsUiState.Error).message)
    }

    @Test
    fun signingOutKeepsTheGameOnScreen() = runTest {
        val viewModel = viewModel(previewGame.id)
        val before = loaded(viewModel)

        session.value = SessionState.SignedOut

        assertEquals(before, viewModel.uiState.value)
    }

    @Test
    fun aMissingGameIsAnError() = runTest {
        val state = loaded(viewModel("nope"))

        assertTrue(state is GameDetailsUiState.Error)
        assertEquals(LocalizedText(GameErrors.NOT_FOUND.message), (state as GameDetailsUiState.Error).message)
    }

    @Test
    fun anInviteeSeesTheRsvpCard() = runTest {
        val state = loaded(viewModel(invitation.id, FakeGamesRepository(invitation))) as GameDetailsUiState.Content

        val invitee = state.game as GameUiState.Invitee
        assertTrue((invitee.rsvp as RsvpUiState.Pending).canAccept)
    }

    @Test
    fun theDetailsFollowTheGameAsItChanges() = runTest {
        val repository = FakeGamesRepository(invitation)
        val viewModel = viewModel(invitation.id, repository)
        loaded(viewModel)

        // Someone else takes the last open slots while the screen is up.
        val full = invitation.copy(
            teeTimes = listOf(invitation.teeTimes.single().copy(slots = 1))
        )
        repository.setGames(full)

        val rsvp = ((viewModel.uiState.value as GameDetailsUiState.Content).game as GameUiState.Invitee).rsvp as RsvpUiState.Pending
        assertFalse(rsvp.canAccept)
    }

    @Test
    fun answeringSendsTheAnswerAndConfirmsIt() = runTest {
        val repository = FakeGamesRepository(invitation)
        val viewModel = viewModel(invitation.id, repository)
        loaded(viewModel)
        val messages = messages()

        viewModel.onAnswer(accept = true)

        assertEquals(listOf("invitation" to true), repository.respondRequests)
        assertEquals(listOf(TtcMessage(GMR.strings.game_invite_accepted_message, listOf(invitation.course.name))), messages)
    }

    @Test
    fun aYesThatLosesTheLastSlotSaysTheGameJustFilledUp() = runTest {
        val repository = FakeGamesRepository(invitation).apply { respondFailure = GameErrors.GAME_FULL }
        val viewModel = viewModel(invitation.id, repository)
        loaded(viewModel)
        val messages = messages()

        viewModel.onAnswer(accept = true)

        assertEquals(listOf(TtcMessage(GameErrors.GAME_FULL.title)), messages)
    }

    @Test
    fun eachTapOnYesOrNoIsLoggedOnceWithItsResponseAndSource() = runTest {
        val viewModel = viewModel(invitation.id, FakeGamesRepository(invitation))
        loaded(viewModel)

        viewModel.onAnswer(accept = false)

        assertEquals(
            listOf(
                AnalyticsEvent.AnswerInviteClick(
                    AnalyticsEvent.AnswerInviteClick.Response.NO,
                    AnalyticsEvent.AnswerInviteClick.Source.GAME_DETAILS
                )
            ),
            analytics.events.filterIsInstance<AnalyticsEvent.AnswerInviteClick>()
        )
    }

    @Test
    fun aYesIsLoggedEvenWhenTheAnswerFails() = runTest {
        val repository = FakeGamesRepository(invitation).apply { respondFailure = GameErrors.GAME_FULL }
        val viewModel = viewModel(invitation.id, repository)
        loaded(viewModel)

        viewModel.onAnswer(accept = true)

        assertEquals(
            listOf(
                AnalyticsEvent.AnswerInviteClick(
                    AnalyticsEvent.AnswerInviteClick.Response.YES,
                    AnalyticsEvent.AnswerInviteClick.Source.GAME_DETAILS
                )
            ),
            analytics.events.filterIsInstance<AnalyticsEvent.AnswerInviteClick>()
        )
    }

    @Test
    fun onlyProfileChangesTheScreenUsesRestartTheListener() = runTest {
        val repository = FakeGamesRepository(invitation)
        loaded(viewModel(invitation.id, repository))

        session.value = SessionState.SignedIn(previewPlayer.copy(photoUrl = "https://example.test/new.jpg"))
        assertEquals(listOf("invitation"), repository.observedGameIds)

        session.value = SessionState.SignedIn(previewPlayer.copy(phone = "5025550000"))
        assertEquals(listOf("invitation", "invitation"), repository.observedGameIds)
    }

    // MARK: the organizer recording a response

    // The signed-in player's own game, with Matt invited and not yet answering.
    private val mattPhone = "4155550188"
    private val organized = previewGame.copy(
        id = "organized",
        invitees = listOf(GameInvitee("Matt Chen", mattPhone, playerId = null, status = InviteStatus.PENDING)),
    )

    private fun GameDetailsViewModel.organizerState() =
        (uiState.value as GameDetailsUiState.Content).game as GameUiState.Organizer

    @Test
    fun tappingAnInviteeOpensTheirResponseSheetAndIsLogged() = runTest {
        val viewModel = viewModel(organized.id, FakeGamesRepository(organized))
        loaded(viewModel)
        assertNull(viewModel.organizerState().responseSheet)

        viewModel.onInviteeClick(mattPhone)

        assertEquals(mattPhone, viewModel.organizerState().responseSheet?.inviteePhone)
        assertEquals(listOf(AnalyticsEvent.InviteeRowClick), analytics.events.filterIsInstance<AnalyticsEvent.InviteeRowClick>())
    }

    @Test
    fun theSheetClosesOnItsOwnOnceTheInviteeHasAnswered() = runTest {
        val repository = FakeGamesRepository(organized)
        val viewModel = viewModel(organized.id, repository)
        loaded(viewModel)
        viewModel.onInviteeClick(mattPhone)

        repository.setGames(organized.copy(invitees = organized.invitees.map { it.copy(status = InviteStatus.DECLINED) }))

        assertNull(viewModel.organizerState().responseSheet)
    }

    @Test
    fun recordingAnAnswerClosesTheSheetSendsItAndConfirmsIt() = runTest {
        val repository = FakeGamesRepository(organized)
        val viewModel = viewModel(organized.id, repository)
        loaded(viewModel)
        val messages = messages()
        viewModel.onInviteeClick(mattPhone)

        viewModel.onRecordResponse(accept = true)

        assertNull(viewModel.organizerState().responseSheet)
        assertEquals(listOf(Triple("organized", mattPhone, true)), repository.recordRequests)
        assertEquals(listOf(TtcMessage(GMR.strings.game_details_response_recorded_yes, listOf("Matt Chen"))), messages)
        assertEquals(
            listOf(AnalyticsEvent.OrganizerResponseClick(AnalyticsEvent.OrganizerResponseClick.Response.YES)),
            analytics.events.filterIsInstance<AnalyticsEvent.OrganizerResponseClick>()
        )
    }

    @Test
    fun aRecordedYesThatLosesTheLastSlotSaysTheGameJustFilledUp() = runTest {
        val repository = FakeGamesRepository(organized).apply { recordFailure = GameErrors.GAME_FULL }
        val viewModel = viewModel(organized.id, repository)
        loaded(viewModel)
        val messages = messages()
        viewModel.onInviteeClick(mattPhone)

        viewModel.onRecordResponse(accept = true)

        assertEquals(listOf(TtcMessage(GameErrors.GAME_FULL.title)), messages)
    }

    @Test
    fun dismissingTheSheetClosesItAndIsLoggedOnlyWhenNothingWasPicked() = runTest {
        val viewModel = viewModel(organized.id, FakeGamesRepository(organized))
        loaded(viewModel)

        viewModel.onInviteeClick(mattPhone)
        viewModel.onResponseSheetDismiss()
        assertNull(viewModel.organizerState().responseSheet)

        // Picking an answer closes the sheet too; the dismiss that follows its exit isn't another.
        viewModel.onInviteeClick(mattPhone)
        viewModel.onRecordResponse(accept = false)
        viewModel.onResponseSheetDismiss()

        assertEquals(
            listOf(AnalyticsEvent.OrganizerResponseSheetDismiss),
            analytics.events.filterIsInstance<AnalyticsEvent.OrganizerResponseSheetDismiss>()
        )
    }

    @Test
    fun noAnswerIsSentWhileNoSheetIsOpen() = runTest {
        val repository = FakeGamesRepository(organized)
        val viewModel = viewModel(organized.id, repository)
        loaded(viewModel)

        viewModel.onRecordResponse(accept = true)

        assertTrue(repository.recordRequests.isEmpty())
    }
}
