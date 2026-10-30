package net.bradbal.teetimecaddie.core.storage.documents

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Document model for a game, stored at `games/{autoId}`.
 *
 * @property courseId The id of the game's `courses` document.
 * @property courseName A copy of that course's name, so the Games tab needs no second read.
 * @property date Stored as an ISO string ("2026-06-13"). A tee date is a calendar day, not an
 *   instant, and ISO dates still sort and range-compare correctly as strings.
 * @property playerIds Everyone holding a slot, across all tee times. It exists for the
 *   `array-contains` query that finds a player's games, and the read rule checks it.
 * @property invitees Everyone invited, in the order the organizer added them.
 * @property invitedPhones The invitees' numbers, a copy of each [GameInviteeDocument.phone]. It
 *   exists for the `array-contains` query that finds the invitations waiting for a phone number.
 * @property createdAt Set by the server on write. Null when read back from a pending local write,
 *   before the server has assigned it.
 */
@Serializable
data class GameDocument(
    val organizerId: String,
    val courseId: String,
    val courseName: String,
    val date: LocalDate,
    val teeTimes: List<GameTeeTimeDocument>,
    val playerIds: List<String>,
    val invitees: List<GameInviteeDocument>,
    val invitedPhones: List<String>,
    val createdAt: BaseTimestamp? = Timestamp.ServerTimestamp
) {

    @Transient
    var id: String? = null
}
