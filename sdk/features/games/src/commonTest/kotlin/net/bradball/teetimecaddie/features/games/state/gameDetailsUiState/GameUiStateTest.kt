package net.bradball.teetimecaddie.features.games.state.gameDetailsUiState

import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.AvatarTone
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.GameErrors
import net.bradball.teetimecaddie.features.games.GameException
import net.bradball.teetimecaddie.features.games.state.bradViewer
import net.bradball.teetimecaddie.features.games.state.danaViewer
import net.bradball.teetimecaddie.features.games.state.game
import net.bradball.teetimecaddie.features.games.state.guest
import net.bradball.teetimecaddie.features.games.state.invitedGame
import net.bradball.teetimecaddie.features.games.state.invitee
import net.bradball.teetimecaddie.features.games.state.organizer
import net.bradball.teetimecaddie.features.games.state.samViewer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameUiStateTest {

    private fun Game.organizerView() = toUiState(viewer = bradViewer) as GameUiState.Organizer
    private fun Game.inviteeView(viewer: Player) = toUiState(viewer) as GameUiState.Invitee

    // Brad organized every game here; his profile name is "brad Ball".
    private val bradInvitedYou = LocalizedText(GMR.strings.game_details_rsvp_title, listOf("brad"))

    // MARK: Game details

    @Test
    fun detailsAreFormattedForDisplay() {
        val details = game.organizerView()

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

        val teeTime = withInvitees.organizerView().teeTimes.single()

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

        val teeTime = crowded.organizerView().teeTimes.single()

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

        val teeTimes = twoTeeTimes.organizerView().teeTimes

        assertEquals(1, teeTimes[0].players.count { it.status == TeeTimePlayerStatus.INVITED })
        assertEquals(emptyList(), teeTimes[1].players)
        assertEquals(4, teeTimes[1].openSlots)
    }

    @Test
    fun theOrganizerGetsTheOrganizersViewAndAnInviteeGetsTheirs() {
        assertTrue(invitedGame.toUiState(viewer = bradViewer) is GameUiState.Organizer)
        assertTrue(invitedGame.toUiState(viewer = samViewer) is GameUiState.Invitee)
    }

    @Test
    fun aViewerWithNoPartInTheGameGetsNoView() {
        // Dana holds a slot without having been invited, so she is neither organizer nor invitee.
        assertEquals(null, game.toUiState(viewer = danaViewer))
        assertEquals(
            GameDetailsUiState.Error(GameException(GameErrors.NOT_FOUND)),
            game.toDetailsUiState(viewer = danaViewer)
        )
    }

    @Test
    fun aViewerWithAPartInTheGameGetsItsContent() {
        assertEquals(
            GameDetailsUiState.Content(invitedGame.organizerView()),
            invitedGame.toDetailsUiState(viewer = bradViewer)
        )
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

    // MARK: Game details as an invitee

    @Test
    fun anUnansweredInvitationAsksWithYesEnabled() {
        assertEquals(
            RsvpUiState.Pending(LocalizedText(GMR.strings.game_details_rsvp_prompt), canAccept = true, title = bradInvitedYou),
            invitedGame.inviteeView(samViewer).rsvp
        )
    }

    @Test
    fun aFullGameSaysSoAndDisablesYes() {
        val full = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest)))
        )

        assertEquals(
            RsvpUiState.Pending(LocalizedText(GMR.strings.game_details_rsvp_full, listOf("2")), canAccept = false, title = bradInvitedYou),
            full.inviteeView(samViewer).rsvp
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

        assertEquals(RsvpUiState.Accepted, accepted.inviteeView(samViewer).rsvp)
        assertEquals(RsvpUiState.Declined, declined.inviteeView(samViewer).rsvp)
    }

    @Test
    fun theViewersOwnRowIsMarkedWithTheirFirstName() {
        val rows = invitedGame.inviteeView(samViewer).teeTimes.single().players

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

        val teeTime = accepted.inviteeView(samViewer).teeTimes.single()

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

        val rows = danaAccepted.inviteeView(samViewer).teeTimes.single().players

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

        val teeTime = declined.inviteeView(samViewer).teeTimes.single()

        assertEquals(listOf(TeeTimePlayerStatus.ORGANIZER), teeTime.players.map { it.status })
        assertEquals(3, teeTime.openSlots)
    }

    @Test
    fun theInviteeViewSaysWhoOrganizedTheGame() {
        assertEquals(
            LocalizedText(GMR.strings.game_details_organized_by, listOf("Sat, Jun 13", "brad")),
            invitedGame.inviteeView(samViewer).organizedBy
        )
    }

    @Test
    fun theOrganizersViewKeepsThePlainDate() {
        assertEquals("Sat, Jun 13", invitedGame.organizerView().date)
    }

    @Test
    fun anInvitationWithNoOrganizerSlotHasNoTitle() {
        val noOrganizerSlot = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = emptyList()))
        )

        val state = noOrganizerSlot.inviteeView(samViewer)

        assertEquals(null, (state.rsvp as RsvpUiState.Pending).title)
        assertEquals(null, state.organizedBy)
    }
}
