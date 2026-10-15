package net.bradbal.teetimecaddie.core.storage.documents

import dev.gitlive.firebase.firestore.BaseTimestamp
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Document model for a course, stored at `courses/{nameKey}`.
 *
 * The document id *is* [nameKey], so Firestore itself keeps course names unique: two organizers
 * typing "Pebble Beach" and "pebble beach" address the same document.
 *
 * @property name The name as the first organizer typed it. Everyone after them sees this spelling.
 * @property nameKey [name] normalised for matching. The security rules check it equals the id.
 */
@Serializable
data class CourseDocument(
    val name: String,
    val nameKey: String
)

/**
 * A player occupying a tee-time slot.
 *
 * @property name Copied from the player's profile when they join, so a game renders without
 *   reading any `players` documents — those hold email and phone numbers.
 */
@Serializable
data class GamePlayerDocument(
    val playerId: String,
    val name: String
)

/**
 * One tee time within a game.
 *
 * @property time Stored as an ISO string ("09:00"), like every other [LocalTime] in storage.
 */
@Serializable
data class GameTeeTimeDocument(
    val time: LocalTime,
    val slots: Int,
    val players: List<GamePlayerDocument>
)

/**
 * Document model for a game, stored at `games/{autoId}`.
 *
 * @property courseId The id of the game's `courses` document.
 * @property courseName A copy of that course's name, so the Games tab needs no second read.
 * @property date Stored as an ISO string ("2026-06-13"). A tee date is a calendar day, not an
 *   instant, and ISO dates still sort and range-compare correctly as strings.
 * @property playerIds Everyone holding a slot, across all tee times. It exists for the
 *   `array-contains` query that finds a player's games, and the read rule checks it.
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
    val createdAt: BaseTimestamp? = Timestamp.ServerTimestamp
) {

    @Transient
    var id: String? = null
}
