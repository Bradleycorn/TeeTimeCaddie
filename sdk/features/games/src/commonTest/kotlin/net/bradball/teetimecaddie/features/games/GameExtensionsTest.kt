package net.bradball.teetimecaddie.features.games

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameInviteeDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameRosterDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.InviteeAnswerDocument
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.previewPlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class GameExtensionsTest {

    // MARK: courseKey

    @Test
    fun courseKey_ignoresCase() {
        assertEquals(courseKey("Pebble Beach"), courseKey("pEBBLE bEACH"))
    }

    @Test
    fun courseKey_ignoresLeadingTrailingAndRepeatedSpaces() {
        assertEquals("pebble beach", courseKey("  Pebble \t  Beach  "))
    }

    @Test
    fun courseKey_replacesSlashesSoItIsAValidDocumentId() {
        assertEquals("torrey pines n-s", courseKey("Torrey Pines N/S"))
    }

    @Test
    fun cleanCourseName_keepsCaseButTidiesSpaces() {
        assertEquals("Pebble Beach", cleanCourseName("  Pebble   Beach "))
    }

    // MARK: mappers

    private val organizer = GamePlayerDocument(playerId = "brad", name = "Brad Ball", phone = "5025551234")

    private fun document(vararg teeTimes: GameTeeTimeDocument) = GameDocument(
        organizerId = "brad",
        courseId = "pebble beach",
        courseName = "Pebble Beach",
        date = LocalDate(2026, 6, 13),
        teeTimes = teeTimes.toList(),
        playerIds = listOf("brad"),
        invitees = emptyMap(),
        invitedPhones = emptyList(),
        createdAt = null
    ).also { it.id = "game-1" }

    @Test
    fun gameDocument_toModel_listsInviteesInTheOrderTheyWereAdded() {
        val invitees = mapOf(
            "4155550188" to GameInviteeDocument("Matt Chen", null, "PENDING", order = 1),
            "4155550142" to GameInviteeDocument("Jeff Morrison", "jeff", "ACCEPTED_LATER", order = 0)
        )

        val game = document().copy(invitees = invitees).also { it.id = "game-1" }.toModel()

        assertEquals(
            listOf(
                // An unrecognised status reads as PENDING.
                GameInvitee("Jeff Morrison", "4155550142", "jeff", InviteStatus.PENDING),
                GameInvitee("Matt Chen", "4155550188", null, InviteStatus.PENDING)
            ),
            game.invitees
        )
    }

    @Test
    fun toInviteesDocument_keysEachInviteeByPhoneAndRecordsTheirPosition() {
        val invitees = listOf(
            GameInvitee("Jeff Morrison", "4155550142", "jeff", InviteStatus.PENDING),
            GameInvitee("Matt Chen", "4155550188", null, InviteStatus.PENDING)
        )

        assertEquals(
            mapOf(
                "4155550142" to GameInviteeDocument("Jeff Morrison", "jeff", "PENDING", order = 0),
                "4155550188" to GameInviteeDocument("Matt Chen", null, "PENDING", order = 1)
            ),
            invitees.toInviteesDocument()
        )
    }

    // MARK: answer

    private val danaPhone = "5025552345"
    private val dana = previewPlayer.copy(id = "dana", name = "Dana Park", phone = danaPhone)
    private val danaInvite = GameInviteeDocument("Dana", playerId = null, status = "PENDING", order = 0)

    private fun invitingGame(slots: Int = 4, invitee: GameInviteeDocument = danaInvite) =
        document(GameTeeTimeDocument(LocalTime(9, 0), slots, listOf(organizer)))
            .copy(invitees = mapOf(danaPhone to invitee), invitedPhones = listOf(danaPhone))

    @Test
    fun answer_noWritesOnlyTheInvitation() {
        assertEquals(
            InviteAnswer.Write(
                InviteeAnswerDocument(danaInvite.copy(status = "DECLINED", playerId = "dana"), seat = null)
            ),
            invitingGame().answer(danaPhone, dana, accept = false)
        )
    }

    @Test
    fun answer_yesWritesTheInvitationAndTheNextSeat() {
        val seated = GameTeeTimeDocument(
            LocalTime(9, 0), 4, listOf(organizer, GamePlayerDocument("dana", "Dana Park", "5025552345"))
        )

        assertEquals(
            InviteAnswer.Write(
                InviteeAnswerDocument(
                    invitee = danaInvite.copy(status = "ACCEPTED", playerId = "dana"),
                    seat = GameRosterDocument(teeTimes = listOf(seated), playerIds = listOf("brad", "dana"))
                )
            ),
            invitingGame().answer(danaPhone, dana, accept = true)
        )
    }

    @Test
    fun answer_yesToAFullGameWritesNothing() {
        assertEquals(InviteAnswer.Full, invitingGame(slots = 1).answer(danaPhone, dana, accept = true))
    }

    @Test
    fun answer_writesNothingForAnAnsweredOrMissingInvitation() {
        val declined = invitingGame(invitee = danaInvite.copy(status = "DECLINED", playerId = "dana"))

        assertEquals(InviteAnswer.AlreadyAnswered, declined.answer(danaPhone, dana, accept = true))
        assertEquals(InviteAnswer.NotInvited, invitingGame().answer("4155550188", dana, accept = true))
    }

    // MARK: the organizer's answers

    private val claimedInvite = danaInvite.copy(playerId = "dana")

    private fun seatedWith(seat: GamePlayerDocument, playerIds: List<String>) = GameRosterDocument(
        teeTimes = listOf(GameTeeTimeDocument(LocalTime(9, 0), 4, listOf(organizer, seat))),
        playerIds = playerIds
    )

    @Test
    fun recordAnswer_noChangesOnlyTheStatus() {
        assertEquals(
            InviteAnswer.Write(InviteeAnswerDocument(claimedInvite.copy(status = "DECLINED"), seat = null)),
            invitingGame(invitee = claimedInvite).recordAnswer(danaPhone, numberOwnerId = "dana", accept = false)
        )
    }

    @Test
    fun recordAnswer_yesSeatsAnAccountThatOwnsTheNumber() {
        assertEquals(
            InviteAnswer.Write(
                InviteeAnswerDocument(
                    invitee = claimedInvite.copy(status = "ACCEPTED"),
                    seat = seatedWith(GamePlayerDocument("dana", "Dana", danaPhone), listOf("brad", "dana"))
                )
            ),
            invitingGame(invitee = claimedInvite).recordAnswer(danaPhone, numberOwnerId = "dana", accept = true)
        )
    }

    @Test
    fun recordAnswer_yesSeatsSomeoneWithoutAnAccountByNameAndNumber() {
        assertEquals(
            InviteAnswer.Write(
                InviteeAnswerDocument(
                    invitee = danaInvite.copy(status = "ACCEPTED"),
                    seat = seatedWith(GamePlayerDocument(null, "Dana", danaPhone), listOf("brad"))
                )
            ),
            invitingGame().recordAnswer(danaPhone, numberOwnerId = null, accept = true)
        )
    }

    @Test
    fun recordAnswer_yesLeavesOutAClaimedAccountThatNoLongerOwnsTheNumber() {
        val answer = invitingGame(invitee = claimedInvite)
            .recordAnswer(danaPhone, numberOwnerId = "someone-else", accept = true)

        assertEquals(
            seatedWith(GamePlayerDocument(null, "Dana", danaPhone), listOf("brad")),
            assertIs<InviteAnswer.Write>(answer).answer.seat
        )
    }

    @Test
    fun recordAnswer_yesToAFullGameWritesNothing() {
        assertEquals(InviteAnswer.Full, invitingGame(slots = 1).recordAnswer(danaPhone, null, accept = true))
    }

    @Test
    fun recordAnswer_writesNothingForAnAnsweredOrMissingInvitation() {
        val declined = invitingGame(invitee = danaInvite.copy(status = "DECLINED", playerId = "dana"))

        assertEquals(InviteAnswer.AlreadyAnswered, declined.recordAnswer(danaPhone, "dana", accept = true))
        assertEquals(InviteAnswer.NotInvited, invitingGame().recordAnswer("4155550188", null, accept = false))
    }

    @Test
    fun gameDocument_toModel_carriesTheCourseAndOrganizer() {
        val game = document(GameTeeTimeDocument(LocalTime(9, 0), 4, listOf(organizer))).toModel()

        assertEquals("game-1", game.id)
        assertEquals(Course(id = "pebble beach", name = "Pebble Beach"), game.course)
        assertEquals("brad", game.organizerId)
        assertEquals("Brad Ball", game.teeTimes.single().players.single().name)
        assertEquals(1, game.teeTimes.single().filled)
        assertEquals(3, game.teeTimes.single().openSlots)
    }

    @Test
    fun gameDocument_toModel_ordersTeeTimesEarliestFirst() {
        val game = document(
            GameTeeTimeDocument(LocalTime(9, 10), 4, emptyList()),
            GameTeeTimeDocument(LocalTime(9, 0), 4, listOf(organizer))
        ).toModel()

        assertEquals(listOf(LocalTime(9, 0), LocalTime(9, 10)), game.teeTimes.map { it.time })
    }

    @Test
    fun gameDocument_toModel_requiresAnId() {
        val withoutId = document().also { it.id = null }

        assertFailsWith<IllegalArgumentException> { withoutId.toModel() }
    }

    @Test
    fun gameTeeTime_roundTripsThroughItsDocument() {
        val teeTimeDocument = GameTeeTimeDocument(LocalTime(7, 45), 2, listOf(organizer))

        assertEquals(teeTimeDocument, teeTimeDocument.toModel().toDocument())
    }
}
