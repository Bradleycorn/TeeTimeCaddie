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
                TeeTimePlayerRowUiState(initial = "M", name = "matt Chen", tone = AvatarTone.forName("matt Chen"), status = TeeTimePlayerStatus.INVITED, inviteePhone = "4155550188"),
                TeeTimePlayerRowUiState(initial = "J", name = "Jeff Morrison", tone = AvatarTone.forName("Jeff Morrison"), status = TeeTimePlayerStatus.INVITED, inviteePhone = "4155550188"),
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
    fun theOrganizerIsListedFirstWhicheverSlotTheyHold() {
        val organizerSecond = game.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(guest, organizer)))
        )

        val rows = organizerSecond.organizerView().teeTimes.single().players

        assertEquals(listOf("brad Ball", "Dana Park"), rows.map { it.name })
        assertEquals(listOf(TeeTimePlayerStatus.ORGANIZER, TeeTimePlayerStatus.CONFIRMED), rows.map { it.status })
    }

    @Test
    fun confirmedPlayersAreInTheOrderTheySaidYesNotInviteOrder() {
        val matt = GamePlayer("matt", "Matt Chen", "4155550188")
        val jeff = GamePlayer("jeff", "Jeff Morrison", "4155550142")
        // Matt was invited first, but Jeff said yes first and took the earlier slot.
        val answered = game.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest, jeff, matt))),
            invitees = listOf(
                invitee("Matt Chen", status = InviteStatus.ACCEPTED, playerId = "matt"),
                invitee("Sam Ortiz", phone = samViewer.phone),
                invitee("Jeff Morrison", status = InviteStatus.ACCEPTED, playerId = "jeff")
            )
        )

        val rows = answered.organizerView().teeTimes.single().players

        assertEquals(listOf("brad Ball", "Dana Park", "Jeff Morrison", "Matt Chen", "Sam Ortiz"), rows.map { it.name })
        assertEquals(
            listOf(
                TeeTimePlayerStatus.ORGANIZER,
                TeeTimePlayerStatus.CONFIRMED,
                TeeTimePlayerStatus.CONFIRMED,
                TeeTimePlayerStatus.CONFIRMED,
                TeeTimePlayerStatus.INVITED
            ),
            rows.map { it.status }
        )
    }

    @Test
    fun aNewYesMovesOnlyThatPersonsRow() {
        val jeff = GamePlayer("jeff", "Jeff Morrison", "4155550142")
        val matt = GamePlayer("matt", "Matt Chen", "4155550188")
        val before = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, jeff))),
            invitees = listOf(
                invitee("Matt Chen"),
                invitee("Sam Ortiz", phone = samViewer.phone),
                invitee("Jeff Morrison", status = InviteStatus.ACCEPTED, playerId = "jeff")
            )
        )
        // Matt, invited before Jeff, says yes after him.
        val after = before.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, jeff, matt))),
            invitees = listOf(
                invitee("Matt Chen", status = InviteStatus.ACCEPTED, playerId = "matt"),
                invitee("Sam Ortiz", phone = samViewer.phone),
                invitee("Jeff Morrison", status = InviteStatus.ACCEPTED, playerId = "jeff")
            )
        )

        assertEquals(
            listOf("brad Ball", "Jeff Morrison", "Matt Chen", "Sam Ortiz"),
            before.organizerView().teeTimes.single().players.map { it.name }
        )
        assertEquals(
            listOf("brad Ball", "Jeff Morrison", "Matt Chen", "Sam Ortiz"),
            after.organizerView().teeTimes.single().players.map { it.name }
        )
    }

    @Test
    fun theOrganizerSeesWhoDeclinedInInviteOrder() {
        val declines = invitedGame.copy(
            invitees = listOf(
                invitee("Matt Chen", status = InviteStatus.DECLINED),
                invitee("Sam Ortiz", phone = samViewer.phone),
                invitee("jeff Morrison", status = InviteStatus.DECLINED)
            )
        )

        val view = declines.organizerView()

        assertEquals(
            listOf(DeclinedInviteeUiState(initial = "M", name = "Matt Chen"), DeclinedInviteeUiState(initial = "J", name = "jeff Morrison")),
            view.declined
        )
        assertEquals(LocalizedText(GMR.strings.game_details_declined_heading, listOf("2")), view.declinedHeading)
        // Nobody who declined is on the tee time, and they leave its slots open.
        assertEquals(listOf("brad Ball", "Sam Ortiz"), view.teeTimes.single().players.map { it.name })
        assertEquals(2, view.teeTimes.single().openSlots)
    }

    @Test
    fun withNoDeclinesTheDeclinedListIsEmpty() {
        assertEquals(emptyList(), invitedGame.organizerView().declined)
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
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, GamePlayer("sam", "Sam Ortiz", "5025553456")))),
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
    fun aYesMovesTheInviteeIntoTheSlotsAsConfirmedAndCountsTowardFilled() {
        val accepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, GamePlayer("sam", "Sam Ortiz", "5025553456")))),
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.ACCEPTED, playerId = "sam"))
        )

        val teeTime = accepted.inviteeView(samViewer).teeTimes.single()

        assertEquals(listOf(TeeTimePlayerStatus.ORGANIZER, TeeTimePlayerStatus.CONFIRMED), teeTime.players.map { it.status })
        assertEquals(LocalizedText(GMR.strings.game_details_viewer_name, listOf("Sam")), teeTime.players.last().viewerName)
        assertEquals(LocalizedText(GMR.strings.game_details_filled_count, listOf("2", "4")), teeTime.filledCount)
        assertEquals(2, teeTime.openSlots)
    }

    @Test
    fun anotherPlayersYesIsAlsoConfirmed() {
        val danaAccepted = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest))),
            invitees = listOf(
                invitee("Dana", phone = danaViewer.phone, status = InviteStatus.ACCEPTED, playerId = "dana"),
                invitee("Sam", phone = samViewer.phone)
            )
        )

        val rows = danaAccepted.inviteeView(samViewer).teeTimes.single().players

        assertEquals(
            listOf(TeeTimePlayerStatus.ORGANIZER, TeeTimePlayerStatus.CONFIRMED, TeeTimePlayerStatus.INVITED),
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

    // MARK: Recording a response for an invitee

    // Sam's number, as the organizer's app might hold it.
    private val samFormatted = "(502) 555-3456"

    @Test
    fun onlyInvitedRowsInTheOrganizersViewCanRecordAResponse() {
        val organizerRows = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 4, players = listOf(organizer, guest)))
        ).organizerView().teeTimes.single().players

        assertEquals(listOf(null, null, samViewer.phone), organizerRows.map { it.inviteePhone })
        assertEquals(listOf(false, false, true), organizerRows.map { it.canRecordResponse })
        assertTrue(invitedGame.inviteeView(samViewer).teeTimes.single().players.none { it.canRecordResponse })
    }

    @Test
    fun theResponseSheetIsClosedUntilAnInviteeIsChosen() {
        assertEquals(null, invitedGame.organizerView().responseSheet)
    }

    @Test
    fun theChosenInviteeGetsTheOnlyResponseSheet() {
        val state = invitedGame.toUiState(bradViewer, respondingFor = samFormatted) as GameUiState.Organizer

        assertEquals(
            ResponseSheetUiState(
                inviteePhone = samViewer.phone,
                title = LocalizedText(GMR.strings.game_details_response_title, listOf("Sam")),
                prompt = LocalizedText(GMR.strings.game_details_response_prompt, listOf("Sam", "9:00 AM")),
                yesDetail = LocalizedText(GMR.strings.game_details_response_yes_detail, listOf("9:00 AM")),
                canAccept = true,
                inviteeName = "Sam"
            ),
            state.responseSheet
        )
    }

    @Test
    fun theResponseSheetTurnsYesOffOnceYesAnswersFillEverySlot() {
        val full = invitedGame.copy(
            teeTimes = listOf(GameTeeTime(time = LocalTime(9, 0), slots = 2, players = listOf(organizer, guest)))
        )

        val sheet = (full.toUiState(bradViewer, respondingFor = samViewer.phone) as GameUiState.Organizer).responseSheet

        assertEquals(false, sheet?.canAccept)
        assertEquals(LocalizedText(GMR.strings.game_details_response_yes_full, listOf("9:00 AM")), sheet?.yesDetail)
    }

    @Test
    fun theResponseSheetClosesOnceTheInviteeIsNoLongerWaiting() {
        val answered = invitedGame.copy(invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.DECLINED)))

        assertEquals(null, (answered.toUiState(bradViewer, respondingFor = samViewer.phone) as GameUiState.Organizer).responseSheet)
        assertEquals(null, (invitedGame.toUiState(bradViewer, respondingFor = danaViewer.phone) as GameUiState.Organizer).responseSheet)
    }

    @Test
    fun onlyTheOrganizerGetsAResponseSheet() {
        val state = invitedGame.toUiState(samViewer, respondingFor = samViewer.phone)

        assertTrue(state is GameUiState.Invitee)
    }

    @Test
    fun theResponseSheetConfirmsTheAnswerByTheInviteesName() {
        val sheet = (invitedGame.toUiState(bradViewer, respondingFor = samViewer.phone) as GameUiState.Organizer).responseSheet!!

        assertEquals(LocalizedText(GMR.strings.game_details_response_recorded_yes, listOf("Sam")), sheet.recordedMessage(accepted = true))
        assertEquals(LocalizedText(GMR.strings.game_details_response_recorded_no, listOf("Sam")), sheet.recordedMessage(accepted = false))
    }

    @Test
    fun aSeatTheOrganizerFilledWithoutAnAccountIsStillTheViewersOwnRow() {
        // Sam's Yes was recorded before Sam had an account: the seat has his number but no id.
        val recorded = invitedGame.copy(
            teeTimes = listOf(
                GameTeeTime(
                    time = LocalTime(9, 0),
                    slots = 4,
                    players = listOf(organizer, GamePlayer(playerId = null, name = "Sam", phone = samViewer.phone))
                )
            ),
            invitees = listOf(invitee("Sam", phone = samViewer.phone, status = InviteStatus.ACCEPTED))
        )

        val samsRow = recorded.inviteeView(samViewer).teeTimes.single().players.last()

        assertEquals(TeeTimePlayerStatus.CONFIRMED, samsRow.status)
        assertEquals(LocalizedText(GMR.strings.game_details_viewer_name, listOf("Sam")), samsRow.viewerName)
    }
}
