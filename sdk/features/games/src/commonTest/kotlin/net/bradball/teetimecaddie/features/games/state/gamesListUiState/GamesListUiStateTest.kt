package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.state.bradViewer
import net.bradball.teetimecaddie.features.games.state.game
import net.bradball.teetimecaddie.features.games.state.guest
import net.bradball.teetimecaddie.features.games.state.invitedGame
import net.bradball.teetimecaddie.features.games.state.invitee
import net.bradball.teetimecaddie.features.games.state.organizer
import net.bradball.teetimecaddie.features.games.state.samViewer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GamesListUiStateTest {

    // MARK: Games list

    @Test
    fun noGamesIsTheEmptyState() {
        assertEquals(GamesListUiState.Empty, emptyList<Game>().toGamesListUiState(bradViewer))
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
            listOf(game).toGamesListUiState(bradViewer)
        )
    }

    // MARK: Invitations

    @Test
    fun aPendingInvitationIsListedApartFromTheOtherGames() {
        val state = listOf(invitedGame).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertEquals(
            listOf(
                InviteRowUiState(
                    id = "game1",
                    course = "Pebble Beach",
                    dateAndTime = LocalizedText(GMR.strings.games_row_date_time, listOf("Sat, Jun 13", "9:00 AM")),
                    players = listOf(PlayerAvatarUiState("B", AvatarTone.SECONDARY)),
                    isFull = false
                )
            ),
            state.invites
        )
        assertEquals(emptyList(), state.games)
    }

    @Test
    fun onlyInvitationsHidesTheUpcomingHeader() {
        val state = listOf(invitedGame).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertFalse(state.showsUpcomingHeader)
        assertTrue((listOf(game).toGamesListUiState(bradViewer) as GamesListUiState.Content).showsUpcomingHeader)
    }

    @Test
    fun theInviteHeaderCountsTheInvitations() {
        val one = listOf(invitedGame).toGamesListUiState(samViewer) as GamesListUiState.Content
        val two = listOf(invitedGame, invitedGame.copy(id = "game2")).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertEquals(LocalizedText(GMR.strings.games_invites_header_one), one.invitesHeader)
        assertEquals(LocalizedText(GMR.strings.games_invites_header_many, listOf("2")), two.invitesHeader)
    }

    @Test
    fun answeredInvitationsAreListedWithTheOtherGames() {
        val declined = invitedGame.copy(invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED)))

        val state = listOf(declined).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertEquals(emptyList(), state.invites)
        assertEquals(listOf("game1"), state.games.map { it.id })
    }

    @Test
    fun aFullGamesInvitationIsMarkedFull() {
        val full = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest)))
        )

        val state = listOf(full).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertTrue(state.invites.single().isFull)
    }
}
