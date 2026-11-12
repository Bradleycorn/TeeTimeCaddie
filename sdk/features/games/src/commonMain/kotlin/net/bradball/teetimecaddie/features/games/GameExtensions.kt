package net.bradball.teetimecaddie.features.games

import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameInviteeDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameRosterDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradbal.teetimecaddie.core.storage.documents.InviteeAnswerDocument
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.extensions.weekdayMonthDay
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.core.models.Player

/** The game's date and first tee time, e.g. "Sat, Jun 13 · 9:00 AM". */
internal fun Game.dateAndTime(): LocalizedText = LocalizedText(
    GMR.strings.games_row_date_time,
    listOf(date.weekdayMonthDay, teeTimes.firstOrNull()?.time?.formattedTime.orEmpty())
)


// Every mapper here is `internal` on purpose: `:sdk:core:storage` is an implementation-only
// dependency of `:sdk`, so a document type in any public signature would drag storage types across
// the iOS framework boundary.

private val whitespace = Regex("\\s+")

/**
 * The key two course names must share to count as the same course.
 *
 * Case-insensitive, and blind to leading, trailing and repeated spaces, so "pebble beach",
 * " Pebble  Beach " and "PEBBLE BEACH" all name one course. Also the course's document id, which
 * cannot contain `/`, so a slash becomes a dash.
 */
internal fun courseKey(name: String): String =
    name.trim().replace(whitespace, " ").lowercase().replace('/', '-')

/** The course name as it will be stored: trimmed, with runs of spaces collapsed. */
internal fun cleanCourseName(name: String): String = name.trim().replace(whitespace, " ")

internal fun CourseDocument.toModel(): Course = Course(id = nameKey, name = name)

internal fun GamePlayerDocument.toModel(): GamePlayer =
    GamePlayer(playerId = playerId, name = name, phone = phone)

internal fun GamePlayer.toDocument(): GamePlayerDocument =
    GamePlayerDocument(playerId = playerId, name = name, phone = phone)

internal fun GameTeeTimeDocument.toModel(): GameTeeTime = GameTeeTime(
    time = time,
    slots = slots,
    players = players.map { it.toModel() }
)

internal fun GameTeeTime.toDocument(): GameTeeTimeDocument = GameTeeTimeDocument(
    time = time,
    slots = slots,
    players = players.map { it.toDocument() }
)

// An unrecognised status — written by a newer app version — reads as PENDING rather than failing
// the whole game.
internal fun GameInviteeDocument.toModel(phone: String): GameInvitee = GameInvitee(
    name = name,
    phone = phone,
    playerId = playerId,
    status = InviteStatus.entries.firstOrNull { it.name == status } ?: InviteStatus.PENDING
)

/** @param order Where the invitee falls in the order the organizer added people. */
internal fun GameInvitee.toDocument(order: Int): GameInviteeDocument = GameInviteeDocument(
    name = name,
    playerId = playerId,
    status = status.name,
    order = order
)

/** The invitee documents for these invitees: keyed by phone, each carrying its position. */
internal fun List<GameInvitee>.toInviteesDocument(): Map<String, GameInviteeDocument> =
    mapIndexed { order, invitee -> invitee.phone to invitee.toDocument(order) }.toMap()

internal fun GameDocument.toModel(): Game = Game(
    id = requireNotNull(id) {
        "GameDocument.id was not populated. Read games through GameStorage, which sets it."
    },
    organizerId = organizerId,
    course = Course(id = courseId, name = courseName),
    date = date,
    teeTimes = teeTimes.map { it.toModel() }.sortedBy { it.time },
    invitees = invitees.entries
        .sortedBy { (_, invitee) -> invitee.order }
        .map { (phone, invitee) -> invitee.toModel(phone) }
)

/** What answering an invitation to a game would do to it. */
internal sealed interface InviteAnswer {
    /** The answer is valid: write [answer]. */
    data class Write(val answer: InviteeAnswerDocument) : InviteAnswer

    /** A yes, but every slot is taken. Nothing is written; the invitation stays pending. */
    data object Full : InviteAnswer

    /** The invitation was already answered, perhaps from another device. Nothing is written. */
    data object AlreadyAnswered : InviteAnswer

    /** Nobody was invited by that number. Nothing is written. */
    data object NotInvited : InviteAnswer
}

/**
 * Answer the invitation [phone] received to this game, as [player].
 *
 * Mirrors the invitee and game update rules in firebase/firestore.rules: only the invitee's own
 * document changes, taking their answer and their player id, and a yes also takes the tee time's
 * next free slot under their profile name and number.
 *
 * @param phone Digits only, as [GameDocument.invitees] is keyed. The player's own number.
 */
internal fun GameDocument.answer(phone: String, player: Player, accept: Boolean): InviteAnswer {
    val invitee = invitees[phone] ?: return InviteAnswer.NotInvited
    if (invitee.status != InviteStatus.PENDING.name) return InviteAnswer.AlreadyAnswered

    val answered = invitee.copy(status = statusFor(accept).name, playerId = player.id)
    return answerWith(answered, accept, GamePlayerDocument(player.id, player.name, phone))
}

/**
 * Record the organizer's answer for the invitation [phone] received to this game.
 *
 * Mirrors the organizer's paths in the invitee and game update rules in firebase/firestore.rules:
 * only the invitee's status changes, keeping the player id the organizer named at publish, and a
 * yes also takes the tee time's next free slot under the name they were invited by. The seat
 * carries that player id, and adds it to [GameDocument.playerIds], only when [numberOwnerId] shows
 * the account really owns [phone] and it doesn't hold a slot already; otherwise the seat has none.
 *
 * @param phone Digits only, as [GameDocument.invitees] is keyed.
 * @param numberOwnerId The id of the player whose profile has [phone] now, or null if nobody's does.
 */
internal fun GameDocument.recordAnswer(phone: String, numberOwnerId: String?, accept: Boolean): InviteAnswer {
    val invitee = invitees[phone] ?: return InviteAnswer.NotInvited
    if (invitee.status != InviteStatus.PENDING.name) return InviteAnswer.AlreadyAnswered

    val seatPlayerId = invitee.playerId
        ?.takeIf { it == numberOwnerId && it !in playerIds }
    val answered = invitee.copy(status = statusFor(accept).name)
    return answerWith(answered, accept, GamePlayerDocument(seatPlayerId, invitee.name, phone))
}

private fun statusFor(accept: Boolean): InviteStatus =
    if (accept) InviteStatus.ACCEPTED else InviteStatus.DECLINED

/**
 * Write [answered] and, for a yes, give [seat] the tee time's next free slot, adding its player
 * id, if it has one, to [GameDocument.playerIds].
 */
private fun GameDocument.answerWith(
    answered: GameInviteeDocument,
    accept: Boolean,
    seat: GamePlayerDocument
): InviteAnswer {
    if (!accept) return InviteAnswer.Write(InviteeAnswerDocument(answered, seat = null))

    // Every game has exactly one tee time for now, and it is the one an invitee joins.
    val teeTime = teeTimes.firstOrNull() ?: return InviteAnswer.Full
    if (teeTime.players.size >= teeTime.slots) return InviteAnswer.Full
    val seated = teeTime.copy(players = teeTime.players + seat)

    return InviteAnswer.Write(
        InviteeAnswerDocument(
            invitee = answered,
            seat = GameRosterDocument(
                teeTimes = listOf(seated) + teeTimes.drop(1),
                playerIds = playerIds + listOfNotNull(seat.playerId)
            )
        )
    )
}
