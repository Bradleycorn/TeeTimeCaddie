package net.bradball.teetimecaddie.features.games

import net.bradbal.teetimecaddie.core.storage.documents.CourseDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameDocument
import net.bradbal.teetimecaddie.core.storage.documents.GamePlayerDocument
import net.bradbal.teetimecaddie.core.storage.documents.GameTeeTimeDocument
import net.bradball.teetimecaddie.core.models.Course
import net.bradball.teetimecaddie.core.models.Game
import net.bradball.teetimecaddie.core.models.GamePlayer
import net.bradball.teetimecaddie.core.models.GameTeeTime

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

internal fun GameDocument.toModel(): Game = Game(
    id = requireNotNull(id) {
        "GameDocument.id was not populated. Read games through GameStorage, which sets it."
    },
    organizerId = organizerId,
    course = Course(id = courseId, name = courseName),
    date = date,
    teeTimes = teeTimes.map { it.toModel() }.sortedBy { it.time }
)
