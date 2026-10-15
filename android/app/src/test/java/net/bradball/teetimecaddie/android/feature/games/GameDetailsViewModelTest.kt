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

    @Test
    fun showsTheGame() {
        val state = viewModel(previewGame.id).uiState.value

        assertEquals(GameDetailsUiState.Content(previewGame, isOrganizer = true), state)
    }

    @Test
    fun theOrganizerFillsOneSlot() {
        val state = viewModel(previewGame.id).uiState.value as GameDetailsUiState.Content
        val teeTime = state.game.teeTimes.single()

        assertEquals(previewPlayer.id, teeTime.players.first().playerId)
        assertEquals(1, teeTime.filled)
        assertEquals(teeTime.slots - 1, teeTime.openSlots)
    }

    @Test
    fun aOneSlotGameHasNoOpenSlots() {
        val oneSlot = previewGame.copy(teeTimes = previewGame.teeTimes.map { it.copy(slots = 1) })

        val state = viewModel(oneSlot.id, FakeGamesRepository(oneSlot)).uiState.value as GameDetailsUiState.Content

        assertEquals(0, state.game.teeTimes.single().openSlots)
    }

    @Test
    fun someoneElseIsNotTheOrganizer() {
        session.value = SessionState.SignedIn(previewPlayerList[1])

        val state = viewModel(previewGame.id).uiState.value as GameDetailsUiState.Content

        assertFalse(state.isOrganizer)
    }

    @Test
    fun aMissingGameIsAnError() {
        val state = viewModel("nope").uiState.value

        assertTrue(state is GameDetailsUiState.Error)
        assertEquals(GameErrors.NOT_FOUND.message, (state as GameDetailsUiState.Error).message)
    }
}
