package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.flow.MutableStateFlow
import net.bradball.teetimecaddie.android.feature.games.gameDetails.GameDetailsUiState
import net.bradball.teetimecaddie.android.feature.games.gameDetails.GameDetailsViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.core.models.previewPlayerList
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.session.SessionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<SessionState>(SessionState.SignedIn(previewPlayer))

    private fun viewModel(gameId: String, repository: FakeGamesRepository = FakeGamesRepository(previewGame)) =
        GameDetailsViewModel(gameId, session, repository)

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
        assertEquals(0, repository.getGameCalls)
    }

    @Test
    fun showsTheGame() = runTest {
        val state = loaded(viewModel(previewGame.id))

        assertEquals(GameDetailsUiState.Content(previewGame, isOrganizer = true), state)
    }

    @Test
    fun theOrganizerFillsOneSlot() = runTest {
        val state = loaded(viewModel(previewGame.id)) as GameDetailsUiState.Content
        val teeTime = state.game.teeTimes.single()

        assertEquals(previewPlayer.id, teeTime.players.first().playerId)
        assertEquals(1, teeTime.filled)
        assertEquals(teeTime.slots - 1, teeTime.openSlots)
    }

    @Test
    fun aOneSlotGameHasNoOpenSlots() = runTest {
        val oneSlot = previewGame.copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) })

        val state = loaded(viewModel(oneSlot.id, FakeGamesRepository(oneSlot))) as GameDetailsUiState.Content

        assertEquals(0, state.game.teeTimes.single().openSlots)
    }

    @Test
    fun someoneElseIsNotTheOrganizer() = runTest {
        session.value = SessionState.SignedIn(previewPlayerList[1])

        val state = loaded(viewModel(previewGame.id)) as GameDetailsUiState.Content

        assertFalse(state.isOrganizer)
    }

    @Test
    fun aMissingGameIsAnError() = runTest {
        val state = loaded(viewModel("nope"))

        assertTrue(state is GameDetailsUiState.Error)
        assertEquals(GameErrors.NOT_FOUND.message, (state as GameDetailsUiState.Error).message)
    }
}
