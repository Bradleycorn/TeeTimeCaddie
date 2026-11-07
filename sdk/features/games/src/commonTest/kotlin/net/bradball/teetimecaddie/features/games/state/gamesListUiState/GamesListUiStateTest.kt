package net.bradball.teetimecaddie.features.games.state.gamesListUiState

import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
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
                        dateAndTime = LocalizedText(GMR.strings.games_row_date_time, listOf("Sat, Jun 13", "9:00 AM")),
                        status = GameFillStatus.OPEN,
                        playerCount = LocalizedText(GMR.strings.games_row_player_count_many, listOf("2")),
                        players = listOf(
                            PlayerAvatarUiState("B", AvatarTone.SECONDARY),
                            PlayerAvatarUiState("D", AvatarTone.forName("Dana Park"))
                        ),
                        overflow = PlayerStackOverflow.Add
                    )
                )
            ),
            listOf(game).toGamesListUiState(bradViewer)
        )
    }

    // MARK: Row status, count and avatar stack

    private val matt = GamePlayer("matt", "Matt Chen")
    private val jeff = GamePlayer("jeff", "Jeff Morrison")

    private fun Game.row(viewer: Player = bradViewer) =
        (listOf(this).toGamesListUiState(viewer) as GamesListUiState.Content).games.single()

    @Test
    fun aGameWithASlotNobodyHoldsOrIsInvitedToIsOpen() {
        // Two open slots, one unanswered invitee: one slot is still free to claim.
        val open = game.copy(invitees = listOf(invitee("Matt Chen")))

        val row = open.row()

        assertEquals(GameFillStatus.OPEN, row.status)
        assertEquals(LocalizedText(GMR.strings.games_status_open), row.status.label)
        assertEquals(PlayerStackOverflow.Add, row.overflow)
    }

    @Test
    fun aGameWhoseFreeSlotsCouldAllGoToUnansweredInviteesIsPending() {
        val pending = game.copy(invitees = listOf(invitee("Matt Chen"), invitee("Jeff Morrison"), invitee("Sam Ortiz", phone = samViewer.phone)))

        val row = pending.row()

        assertEquals(GameFillStatus.PENDING, row.status)
        assertEquals(PlayerStackOverflow.More(3), row.overflow)
        assertEquals("+3", (row.overflow as PlayerStackOverflow.More).label)
    }

    @Test
    fun aGameConfirmedPlayersFillIsFullEvenWithInviteesStillToAnswer() {
        val full = game.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest))),
            invitees = listOf(invitee("Matt Chen"))
        )

        val row = full.row()

        assertEquals(GameFillStatus.FULL, row.status)
        assertEquals(PlayerStackOverflow.None, row.overflow)
    }

    @Test
    fun thePlayerCountCountsOnlyThoseWhoAreIn() {
        val justTheOrganizer = invitedGame.copy(
            invitees = listOf(invitee("Matt Chen"), invitee("Jeff Morrison", status = InviteStatus.DECLINED))
        )

        assertEquals(LocalizedText(GMR.strings.games_row_player_count_one), justTheOrganizer.row().playerCount)
        assertEquals(LocalizedText(GMR.strings.games_row_player_count_many, listOf("2")), game.row().playerCount)
    }

    @Test
    fun theStackListsTheOrganizerFirstThenTheOthersInTheOrderTheySaidYes() {
        val answered = game.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(jeff, organizer, matt, guest))),
            invitees = listOf(
                invitee("Matt Chen", status = InviteStatus.ACCEPTED, playerId = "matt"),
                invitee("Jeff Morrison", status = InviteStatus.ACCEPTED, playerId = "jeff")
            )
        )

        val row = answered.row()

        assertEquals(GameFillStatus.FULL, row.status)
        assertEquals(listOf("B", "J", "M", "D"), row.players.map { it.initial })
        assertEquals(AvatarTone.SECONDARY, row.players.first().tone)
    }

    @Test
    fun threeAvatarsAndTheExtraCircleMakeFour() {
        val threeIn = GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest, matt))
        val open = game.copy(teeTimes = listOf(threeIn))
        val pending = game.copy(teeTimes = listOf(threeIn), invitees = listOf(invitee("Sam Ortiz", phone = samViewer.phone)))

        assertEquals(3, open.row().players.size)
        assertEquals(PlayerStackOverflow.Add, open.row().overflow)
        assertEquals(3, pending.row().players.size)
        assertEquals(PlayerStackOverflow.More(1), pending.row().overflow)
    }

    @Test
    fun declinedInviteesNeverJoinTheStack() {
        val declined = game.copy(invitees = listOf(invitee("Matt Chen", status = InviteStatus.DECLINED)))

        assertEquals(listOf("B", "D"), declined.row().players.map { it.initial })
        assertEquals(GameFillStatus.OPEN, declined.row().status)
    }

    @Test
    fun theInviteRowAndTheGamesRowAgreeOnFull() {
        val full = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest)))
        )

        assertTrue((listOf(full).toGamesListUiState(samViewer) as GamesListUiState.Content).invites.single().isFull)
        assertEquals(GameFillStatus.FULL, full.row(bradViewer).status)
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
    fun aYesListsTheGameWithTheOtherGames() {
        val accepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, GamePlayer("sam", "Sam Ortiz")))),
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.ACCEPTED, playerId = "sam"))
        )

        val state = listOf(accepted).toGamesListUiState(samViewer) as GamesListUiState.Content

        assertEquals(emptyList(), state.invites)
        assertEquals(listOf("game1"), state.games.map { it.id })
    }

    @Test
    fun aDeclinedGameIsNotListed() {
        val declined = invitedGame.copy(invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED)))

        assertEquals(GamesListUiState.Empty, listOf(declined).toGamesListUiState(samViewer))
        assertEquals(
            GamesListUiState.Content(games = emptyList(), invites = listOf(invitedGame.copy(id = "game2").toInviteRowUiState())),
            listOf(declined, invitedGame.copy(id = "game2")).toGamesListUiState(samViewer)
        )
    }

    @Test
    fun theOrganizerStillSeesAGameSomeoneDeclined() {
        val declined = invitedGame.copy(invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED)))

        val state = listOf(declined).toGamesListUiState(bradViewer) as GamesListUiState.Content

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

    @Test
    fun theAnswerConfirmationNamesTheCourseOnlyForAYes() {
        assertEquals(
            LocalizedText(GMR.strings.game_invite_accepted_message, listOf("Pebble Beach")),
            inviteAnsweredMessage("Pebble Beach", accepted = true)
        )
        assertEquals(
            LocalizedText(GMR.strings.game_invite_declined_message),
            inviteAnsweredMessage("Pebble Beach", accepted = false)
        )
    }
}
