package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
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
        teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest))),
        invitees = emptyList()
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
                invitees = emptyList(),
                openSlots = 2
            ),
            details.teeTimes.single()
        )
    }

    @Test
    fun inviteesFollowThePlayersInTheOrderAddedWithoutFillingSlots() {
        val withInvitees = game.copy(invitees = listOf(invitee("matt Chen"), invitee("Jeff Morrison")))

        val teeTime = withInvitees.toUiState(viewerId = organizer.playerId).teeTimes.single()

        assertEquals(
            listOf(
                GameInviteeSlotUiState(initial = "M", name = "matt Chen"),
                GameInviteeSlotUiState(initial = "J", name = "Jeff Morrison"),
            ),
            teeTime.invitees
        )
        // Two players and two invitees use all four rows; the count still says two are filled.
        assertEquals(0, teeTime.openSlots)
        assertEquals(LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")), teeTime.filledCount)
    }

    @Test
    fun everyInviteeIsListedEvenBeyondTheSlotCount() {
        val crowded = game.copy(invitees = (1..5).map { invitee("Player $it") })

        val teeTime = crowded.toUiState(viewerId = organizer.playerId).teeTimes.single()

        assertEquals(5, teeTime.invitees.size)
        assertEquals(0, teeTime.openSlots)
    }

    @Test
    fun inviteesAreListedOnlyUnderTheFirstTeeTime() {
        val twoTeeTimes = game.copy(
            teeTimes = game.teeTimes + GameTeeTime(time = LocalTime(9, 10), slots = 4, players = emptyList()),
            invitees = listOf(invitee("Matt Chen"))
        )

        val teeTimes = twoTeeTimes.toUiState(viewerId = organizer.playerId).teeTimes

        assertEquals(1, teeTimes[0].invitees.size)
        assertEquals(emptyList(), teeTimes[1].invitees)
        assertEquals(4, teeTimes[1].openSlots)
    }

    private fun invitee(name: String) =
        GameInvitee(name = name, phone = "4155550188", playerId = null, status = InviteStatus.PENDING)

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
