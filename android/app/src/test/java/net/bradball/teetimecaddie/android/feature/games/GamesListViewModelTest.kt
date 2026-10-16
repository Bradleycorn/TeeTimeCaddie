package net.bradball.teetimecaddie.android.feature.games

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import net.bradball.teetimecaddie.android.feature.games.gamesList.GamesListUiState
import net.bradball.teetimecaddie.android.feature.games.gamesList.GamesListViewModel
import net.bradball.teetimecaddie.android.testing.MainDispatcherRule
import net.bradball.teetimecaddie.android.testing.RecordingEventManager
import net.bradball.teetimecaddie.core.analytics.AnalyticsEvent
import net.bradball.teetimecaddie.core.models.previewGame
import net.bradball.teetimecaddie.core.models.previewGameList
import net.bradball.teetimecaddie.core.models.previewPlayer
import net.bradball.teetimecaddie.session.SessionState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GamesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<SessionState>(SessionState.SignedIn(previewPlayer))

    private val analytics = RecordingEventManager()

    private fun viewModel(repository: FakeGamesRepository) =
        GamesListViewModel(session, repository, analytics.eventManager)

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

        assertEquals(GamesListUiState.Content(previewGameList), viewModel.uiState.value)
    }

    @Test
    fun replacesTheEmptyStateWhenAGameArrives() = runTest {
        val repository = FakeGamesRepository()
        val viewModel = viewModel(repository)
        collect(viewModel)

        repository.setGames(previewGame)

        assertEquals(GamesListUiState.Content(listOf(previewGame)), viewModel.uiState.value)
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
}
