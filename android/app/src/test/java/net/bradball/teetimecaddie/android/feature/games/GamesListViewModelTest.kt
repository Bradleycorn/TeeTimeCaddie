package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.android.feature.games.gamesList.GamesListViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.android.testing.RecordingEventManager
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessage
import net.bradball.teetimecaddie.android.ui.common.feedback.TtcMessenger
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.GamesListUiState
import net.bradball.teetimecaddie.features.games.state.gamesListUiState.toGamesListUiState
import net.bradball.teetimecaddie.session.SessionState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GamesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<SessionState>(SessionState.SignedIn(previewPlayer))

    private val analytics = RecordingEventManager()

    private val messenger = TtcMessenger()

    private fun viewModel(repository: FakeGamesRepository) =
        GamesListViewModel(session, repository, analytics.eventManager, messenger)

    /** Collects every message the ViewModel shows, from now on. */
    private fun TestScope.messages(): List<TtcMessage> = mutableListOf<TtcMessage>().also { list ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { messenger.messages.toList(list) }
    }

    // Someone else's game at Pebble Beach, inviting the signed-in player by their number.
    private val invitation = previewGame.copy(
        id = "invitation",
        organizerId = "sam",
        teeTimes = listOf(previewGame.teeTimes.single().copy(players = listOf(GamePlayer("sam", "Sam Pruitt")))),
        invitees = listOf(GameInvitee("Brad", previewPlayer.phone, playerId = null, status = InviteStatus.PENDING)),
    )

    /** Subscribes, as the screen does, so the WhileSubscribed state starts flowing. */
    private fun TestScope.collect(viewModel: GamesListViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun startsLoading() {
        assertEquals(GamesListUiState.Loading, viewModel(FakeGamesRepository()).uiState.value)
    }

    @Test
    fun showsTheEmptyStateWhenThePlayerHasNoGames() = runTest {
        val viewModel = viewModel(FakeGamesRepository())
        collect(viewModel)

        assertEquals(GamesListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun showsThePlayersGames() = runTest {
        val viewModel = viewModel(FakeGamesRepository(*previewGameList.toTypedArray()))
        collect(viewModel)

        assertEquals(previewGameList.toGamesListUiState(previewPlayer), viewModel.uiState.value)
    }

    @Test
    fun replacesTheEmptyStateWhenAGameArrives() = runTest {
        val repository = FakeGamesRepository()
        val viewModel = viewModel(repository)
        collect(viewModel)

        repository.setGames(previewGame)

        assertEquals(listOf(previewGame).toGamesListUiState(previewPlayer), viewModel.uiState.value)
    }

    @Test
    fun staysLoadingWhileNobodyIsSignedIn() = runTest {
        session.value = SessionState.Loading
        val viewModel = viewModel(FakeGamesRepository(previewGame))
        collect(viewModel)

        assertEquals(GamesListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun addGameClickIsLoggedWithItsSource() {
        val viewModel = viewModel(FakeGamesRepository())

        viewModel.onAddGameClick(AnalyticsEvent.AddGameClick.Source.ADD_BUTTON)
        viewModel.onAddGameClick(AnalyticsEvent.AddGameClick.Source.EMPTY_LIST)

        assertEquals(
            listOf(
                AnalyticsEvent.AddGameClick(AnalyticsEvent.AddGameClick.Source.ADD_BUTTON),
                AnalyticsEvent.AddGameClick(AnalyticsEvent.AddGameClick.Source.EMPTY_LIST),
            ),
            analytics.events
        )
    }

    @Test
    fun listsAnInvitationApartFromThePlayersGames() = runTest {
        val viewModel = viewModel(FakeGamesRepository(previewGame, invitation))
        collect(viewModel)

        val state = viewModel.uiState.value as GamesListUiState.Content
        assertEquals(listOf("invitation"), state.invites.map { it.id })
        assertEquals(listOf(previewGame.id), state.games.map { it.id })
    }

    @Test
    fun answeringSendsTheAnswerAndConfirmsIt() = runTest {
        val repository = FakeGamesRepository(invitation)
        val viewModel = viewModel(repository)
        collect(viewModel)
        val messages = messages()
        val invite = (viewModel.uiState.value as GamesListUiState.Content).invites.single()

        viewModel.onAnswerInvite(invite, accept = true)
        viewModel.onAnswerInvite(invite.copy(id = "other"), accept = false)

        assertEquals(listOf("invitation" to true, "other" to false), repository.respondRequests)
        assertEquals(
            listOf(
                TtcMessage(GMR.strings.game_invite_accepted_message, listOf(invite.course)),
                // No game has the id "other", so the fake reports it missing.
                TtcMessage(GameErrors.NOT_FOUND.message),
            ),
            messages
        )
    }

    @Test
    fun aDeclineIsConfirmedWithoutTheCourse() = runTest {
        val viewModel = viewModel(FakeGamesRepository(invitation))
        collect(viewModel)
        val messages = messages()

        viewModel.onAnswerInvite((viewModel.uiState.value as GamesListUiState.Content).invites.single(), accept = false)

        assertEquals(listOf(TtcMessage(GMR.strings.game_invite_declined_message)), messages)
    }

    @Test
    fun aYesThatLosesTheLastSlotSaysTheGameJustFilledUp() = runTest {
        val repository = FakeGamesRepository(invitation).apply { respondFailure = GameErrors.GAME_FULL }
        val viewModel = viewModel(repository)
        collect(viewModel)
        val messages = messages()

        viewModel.onAnswerInvite((viewModel.uiState.value as GamesListUiState.Content).invites.single(), accept = true)

        assertEquals(listOf(TtcMessage(GameErrors.GAME_FULL.title)), messages)
    }

    @Test
    fun eachTapOnYesOrNoIsLoggedOnceWithItsResponseAndSource() = runTest {
        val viewModel = viewModel(FakeGamesRepository(invitation))
        collect(viewModel)
        val invite = (viewModel.uiState.value as GamesListUiState.Content).invites.single()

        viewModel.onAnswerInvite(invite, accept = true)
        viewModel.onAnswerInvite(invite, accept = false)

        assertEquals(
            listOf(
                AnalyticsEvent.AnswerInviteClick(
                    AnalyticsEvent.AnswerInviteClick.Response.YES,
                    AnalyticsEvent.AnswerInviteClick.Source.GAMES_LIST
                ),
                AnalyticsEvent.AnswerInviteClick(
                    AnalyticsEvent.AnswerInviteClick.Response.NO,
                    AnalyticsEvent.AnswerInviteClick.Source.GAMES_LIST
                ),
            ),
            analytics.events.filterIsInstance<AnalyticsEvent.AnswerInviteClick>()
        )
    }

    @Test
    fun theClickIsLoggedEvenWhenTheAnswerFails() = runTest {
        val repository = FakeGamesRepository(invitation).apply { respondFailure = GameErrors.GAME_FULL }
        val viewModel = viewModel(repository)
        collect(viewModel)

        viewModel.onAnswerInvite((viewModel.uiState.value as GamesListUiState.Content).invites.single(), accept = true)

        assertEquals(1, analytics.events.filterIsInstance<AnalyticsEvent.AnswerInviteClick>().size)
    }
}
