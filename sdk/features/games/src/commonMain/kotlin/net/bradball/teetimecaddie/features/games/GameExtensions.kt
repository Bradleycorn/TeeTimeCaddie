package net.bradball.teetimecaddie.features.games

import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameInviteeDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameRosterDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GameInvitee
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime
import net.bradball.teetimecaddie.core.models.InviteStatus
import net.bradball.teetimecaddie.core.models.Player

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

internal fun GamePlayerDocument.toModel(): GamePlayer = GamePlayer(playerId = playerId, name = name)

internal fun GamePlayer.toDocument(): GamePlayerDocument = GamePlayerDocument(playerId = playerId, name = name)

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

/** The stored `invitees` map for these invitees: keyed by phone, each carrying its position. */
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
    /** The answer is valid: write [roster]. */
    data class Write(val roster: GameRosterDocument) : InviteAnswer

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
 * Mirrors the `games` update rule in firebase/firestore.rules: only the invitee's own entry
 * changes, taking their answer and their player id, and a yes also takes the tee time's next free
 * slot under their profile name.
 *
 * @param phone Digits only, as the invitees map is keyed.
 */
internal fun GameDocument.answer(phone: String, player: Player, accept: Boolean): InviteAnswer {
    val invitee = invitees[phone] ?: return InviteAnswer.NotInvited
    if (invitee.status != InviteStatus.PENDING.name) return InviteAnswer.AlreadyAnswered

    val status = if (accept) InviteStatus.ACCEPTED else InviteStatus.DECLINED
    val answered = invitees + (phone to invitee.copy(status = status.name, playerId = player.id))
    if (!accept) return InviteAnswer.Write(GameRosterDocument(teeTimes, playerIds, answered))

    // Every game has exactly one tee time for now, and it is the one an invitee joins.
    val teeTime = teeTimes.firstOrNull() ?: return InviteAnswer.Full
    if (teeTime.players.size >= teeTime.slots) return InviteAnswer.Full
    val seated = teeTime.copy(players = teeTime.players + GamePlayerDocument(player.id, player.name))

    return InviteAnswer.Write(
        GameRosterDocument(
            teeTimes = listOf(seated) + teeTimes.drop(1),
            playerIds = playerIds + player.id,
            invitees = answered
        )
    )
}
