package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameUiStateTest {

    private val organizer = GamePlayer(playerId = "brad", name = "brad Ball")
    private val guest = GamePlayer(playerId = "dana", name = "Dana Park")

    private val game = Game(
        id = "game1",
        organizerId = organizer.playerId,
        course = Course(id = "pebble beach", name = "Pebble Beach"),
        date = LocalDate(2026, 6, 13),
        teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest)))
    )

    // MARK: Games list

    @Test
    fun noGamesIsTheEmptyState() {
        assertEquals(GamesListUiState.Empty, emptyList<Game>().toGamesListUiState())
    }

    @Test
    fun aGameBecomesARowWithItsDateAndFirstTeeTime() {
        assertEquals(
            GamesListUiState.Content(
                listOf(
                    GameRowUiState(
                        id = "game1",
                        course = "Pebble Beach",
                        dateAndTime = LocalizedText(GMR.strings.games_row_date_time, listOf("Sat, Jun 13", "9:00 AM"))
                    )
                )
            ),
            listOf(game).toGamesListUiState()
        )
    }

    // MARK: Game details

    @Test
    fun detailsAreFormattedForDisplay() {
        val details = game.toUiState(viewerId = organizer.playerId)

        assertEquals("Pebble Beach", details.course)
        assertEquals("Sat, Jun 13", details.date)
        assertEquals(
            GameTeeTimeUiState(
                time = "9:00 AM",
                filledCount = LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")),
                players = listOf(
                    GameSlotUiState(initial = "B", name = "brad Ball", isOrganizer = true),
                    GameSlotUiState(initial = "D", name = "Dana Park", isOrganizer = false),
                ),
                openSlots = 2
            ),
            details.teeTimes.single()
        )
    }

    @Test
    fun onlyTheOrganizerSeesThemselvesAsOrganizer() {
        assertTrue(game.toUiState(viewerId = organizer.playerId).isOrganizer)
        assertFalse(game.toUiState(viewerId = guest.playerId).isOrganizer)
        assertFalse(game.toUiState(viewerId = null).isOrganizer)
    }

    @Test
    fun anErrorShowsTheFailuresOwnTitleAndMessage() {
        val error = GameException(GameErrors.NOT_FOUND)

        assertEquals(
            GameDetailsUiState.Error(
                title = LocalizedText(error.title),
                message = LocalizedText(error.displayMessage, error.messageArgs)
            ),
            GameDetailsUiState.Error(error)
        )
    }
}
