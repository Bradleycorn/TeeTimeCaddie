package net.bradball.teetimecaddie.features.games.state

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
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

    // The players as signed in, for the viewer-dependent state.
    private val bradViewer = Player(id = "brad", name = "brad Ball", email = "brad@golf.app", phone = "5025551234")
    private val danaViewer = Player(id = "dana", name = "Dana Park", email = "dana@golf.app", phone = "5025552345")
    private val samViewer = Player(id = "sam", name = "Sam Ortiz", email = "sam@golf.app", phone = "5025553456")

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

    // MARK: Game details

    @Test
    fun detailsAreFormattedForDisplay() {
        val details = game.toUiState(viewer = bradViewer)

        assertEquals("Pebble Beach", details.course)
        assertEquals("Sat, Jun 13", details.date)
        assertEquals(
            GameTeeTimeUiState(
                time = "9:00 AM",
                filledCount = LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")),
                players = listOf(
                    TeeTimePlayerRowUiState(initial = "B", name = "brad Ball", tone = AvatarTone.SECONDARY, status = TeeTimePlayerStatus.ORGANIZER),
                    TeeTimePlayerRowUiState(initial = "D", name = "Dana Park", tone = AvatarTone.forName("Dana Park"), status = TeeTimePlayerStatus.CONFIRMED),
                ),
                openSlots = 2
            ),
            details.teeTimes.single()
        )
    }

    @Test
    fun inviteesFollowTheSlotHoldersInTheOrderAddedWithoutFillingSlots() {
        val withInvitees = game.copy(invitees = listOf(invitee("matt Chen"), invitee("Jeff Morrison")))

        val teeTime = withInvitees.toUiState(viewer = bradViewer).teeTimes.single()

        assertEquals(
            listOf(
                TeeTimePlayerRowUiState(initial = "B", name = "brad Ball", tone = AvatarTone.SECONDARY, status = TeeTimePlayerStatus.ORGANIZER),
                TeeTimePlayerRowUiState(initial = "D", name = "Dana Park", tone = AvatarTone.forName("Dana Park"), status = TeeTimePlayerStatus.CONFIRMED),
                TeeTimePlayerRowUiState(initial = "M", name = "matt Chen", tone = AvatarTone.forName("matt Chen"), status = TeeTimePlayerStatus.INVITED),
                TeeTimePlayerRowUiState(initial = "J", name = "Jeff Morrison", tone = AvatarTone.forName("Jeff Morrison"), status = TeeTimePlayerStatus.INVITED),
            ),
            teeTime.players
        )
        // Two players and two invitees use all four rows; the count still says two are filled.
        assertEquals(0, teeTime.openSlots)
        assertEquals(LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")), teeTime.filledCount)
    }

    @Test
    fun everyInviteeIsListedEvenBeyondTheSlotCount() {
        val crowded = game.copy(invitees = (1..5).map { invitee("Player $it") })

        val teeTime = crowded.toUiState(viewer = bradViewer).teeTimes.single()

        // Two slot holders and all five invitees: more rows than the tee time's four slots.
        assertEquals(7, teeTime.players.size)
        assertEquals(0, teeTime.openSlots)
    }

    @Test
    fun inviteesAreListedOnlyUnderTheFirstTeeTime() {
        val twoTeeTimes = game.copy(
            teeTimes = game.teeTimes + GameTeeTime(time = LocalTime(9, 10), slots = 4, players = emptyList()),
            invitees = listOf(invitee("Matt Chen"))
        )

        val teeTimes = twoTeeTimes.toUiState(viewer = bradViewer).teeTimes

        assertEquals(1, teeTimes[0].players.count { it.status == TeeTimePlayerStatus.INVITED })
        assertEquals(emptyList(), teeTimes[1].players)
        assertEquals(4, teeTimes[1].openSlots)
    }

    private fun invitee(name: String, phone: String = "4155550188", status: InviteStatus = InviteStatus.PENDING, playerId: String? = null) =
        GameInvitee(name = name, phone = phone, playerId = playerId, status = status)

    @Test
    fun onlyTheOrganizerSeesThemselvesAsOrganizer() {
        assertTrue(game.toUiState(viewer = bradViewer).isOrganizer)
        assertFalse(game.toUiState(viewer = danaViewer).isOrganizer)
        assertFalse(game.toUiState(viewer = null).isOrganizer)
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

    // MARK: Invitations on the Games list

    // Brad organized it; Dana holds no slot yet. Sam was invited and hasn't answered.
    private val invitedGame = game.copy(
        teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer))),
        invitees = listOf(invitee("Sam", phone = samViewer.phone))
    )

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

    // MARK: Game details as an invitee

    @Test
    fun theOrganizerHasNoInvitationToAnswer() {
        assertEquals(null, invitedGame.toUiState(viewer = bradViewer).rsvp)
    }

    @Test
    fun anUnansweredInvitationAsksWithYesEnabled() {
        assertEquals(
            RsvpUiState.Pending(LocalizedText(GMR.strings.game_details_rsvp_prompt), canAccept = true),
            invitedGame.toUiState(viewer = samViewer).rsvp
        )
    }

    @Test
    fun aFullGameSaysSoAndDisablesYes() {
        val full = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest)))
        )

        assertEquals(
            RsvpUiState.Pending(LocalizedText(GMR.strings.game_details_rsvp_full, listOf("2")), canAccept = false),
            full.toUiState(viewer = samViewer).rsvp
        )
    }

    @Test
    fun anAnsweredInvitationCollapses() {
        val accepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, GamePlayer("sam", "Sam Ortiz")))),
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.ACCEPTED, playerId = "sam"))
        )
        val declined = invitedGame.copy(
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED, playerId = "sam"))
        )

        assertEquals(RsvpUiState.Accepted, accepted.toUiState(viewer = samViewer).rsvp)
        assertEquals(RsvpUiState.Declined, declined.toUiState(viewer = samViewer).rsvp)
    }

    @Test
    fun theViewersOwnRowIsMarkedWithTheirFirstName() {
        val rows = invitedGame.toUiState(viewer = samViewer).teeTimes.single().players

        assertEquals(LocalizedText(GMR.strings.game_details_viewer_name, listOf("Sam")), rows.last().viewerName)
        assertEquals(TeeTimePlayerStatus.INVITED, rows.last().status)
        assertEquals(null, rows.first().viewerName)
    }

    @Test
    fun aYesMovesTheInviteeIntoTheSlotsAsAcceptedAndCountsTowardFilled() {
        val accepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, GamePlayer("sam", "Sam Ortiz")))),
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.ACCEPTED, playerId = "sam"))
        )

        val teeTime = accepted.toUiState(viewer = samViewer).teeTimes.single()

        assertEquals(listOf(TeeTimePlayerStatus.ORGANIZER, TeeTimePlayerStatus.ACCEPTED), teeTime.players.map { it.status })
        assertEquals(LocalizedText(GMR.strings.game_details_viewer_name, listOf("Sam")), teeTime.players.last().viewerName)
        assertEquals(LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")), teeTime.filledCount)
        assertEquals(2, teeTime.openSlots)
    }

    @Test
    fun anotherPlayersYesIsAlsoLabelledAccepted() {
        val danaAccepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest))),
            invitees = listOf(
                invitee("Dana", phone = danaViewer.phone, status = InviteStatus.ACCEPTED, playerId = "dana"),
                invitee("Sam", phone = samViewer.phone)
            )
        )

        val rows = danaAccepted.toUiState(viewer = samViewer).teeTimes.single().players

        assertEquals(
            listOf(TeeTimePlayerStatus.ORGANIZER, TeeTimePlayerStatus.ACCEPTED, TeeTimePlayerStatus.INVITED),
            rows.map { it.status }
        )
    }

    @Test
    fun aNoTakesTheInviteeOffTheRoster() {
        val declined = invitedGame.copy(
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED, playerId = "sam"))
        )

        val teeTime = declined.toUiState(viewer = samViewer).teeTimes.single()

        assertEquals(listOf(TeeTimePlayerStatus.ORGANIZER), teeTime.players.map { it.status })
        assertEquals(3, teeTime.openSlots)
    }
}
