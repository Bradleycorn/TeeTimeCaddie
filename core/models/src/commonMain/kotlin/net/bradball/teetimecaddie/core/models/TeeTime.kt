package net.bradball.teetimecaddie.core.models

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.ExperimentalTime
import net.bradball.teetimecaddie.core.extensions.formattedTime

/**
 * Represents a single tee time slot with a specific time and number of players.
 */
data class TeeTimeSlot(
    val time: LocalTime,
    val numberOfPlayers: Int
)

/**
 * Represents a tee time booking that can contain multiple time slots.
 * This allows groups with multiple foursomes to book consecutive tee times together.
 */
data class TeeTime(
    val id: String?,
    val createdBy: String,
    val course: String,
    val date: LocalDate,
    val times: List<TeeTimeSlot>
) {

    /**
     * Returns the total number of players across all time slots.
     */
    val totalPlayers: Int = times.sumOf { it.numberOfPlayers }

    /**
     * Returns a short date representation (e.g., "JAN\n15")
     */
    val shortDate: String = "${date.month.name.take(3)}\n${date.day}"

    /**
     * Returns a comma-separated string of all times in ascending order.
     * Example: "9:00 AM, 9:10 AM, 9:20 AM"
     */
    val formattedTimes: String = times
        .sortedBy { it.time }
        .joinToString(", ") { it.time.formattedTime }
}
val previewTeeTimeSlot = TeeTimeSlot(
    time = LocalTime(9, 0),
    numberOfPlayers = 4
)

val previewTeeTimeSlotList = listOf(
    previewTeeTimeSlot,
    TeeTimeSlot(time = LocalTime(9, 10), numberOfPlayers = 4),
    TeeTimeSlot(time = LocalTime(9, 20), numberOfPlayers = 3)
)

@OptIn(ExperimentalTime::class)
val previewTeeTime = TeeTime(
    id = "previewTime",
    createdBy = "Brad",
    course = "Persimmon Ridge",
    date = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    times = listOf(previewTeeTimeSlot)
)

@OptIn(ExperimentalTime::class)
val previewTeeTimeList = listOf(
    previewTeeTime,
    previewTeeTime.copy(
        id = "previewTime2",
        times = listOf(
            TeeTimeSlot(time = LocalTime(10, 0), numberOfPlayers = 4),
            TeeTimeSlot(time = LocalTime(10, 10), numberOfPlayers = 4)
        )
    ),
    previewTeeTime.copy(
        id = "previewTime3",
        times = listOf(
            TeeTimeSlot(time = LocalTime(11, 0), numberOfPlayers = 3)
        )
    ),
    previewTeeTime.copy(
        id = "previewTime4",
        times = listOf(
            TeeTimeSlot(time = LocalTime(12, 0), numberOfPlayers = 4),
            TeeTimeSlot(time = LocalTime(12, 10), numberOfPlayers = 4),
            TeeTimeSlot(time = LocalTime(12, 20), numberOfPlayers = 2)
        )
    )
)