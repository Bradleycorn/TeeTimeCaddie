package net.bradball.teetimecaddie.core.extensions

import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Converts a LocalDate to a timestamp.
 * This method creates a timestamp using the "start of day" (i.e. midnight) in the passed in
 * timezone as the time for the LocalDate.
 *
 * @param timeZone the TimeZone to use for calculating the "start of day" (midnight) used
 *   when creating the timestamp. Defaults the current system timezone.
 *
 * @return A timestamp (number of milliseconds from the unix epoch).
 */
@OptIn(ExperimentalTime::class)
fun LocalDate.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
    return this
        .atStartOfDayIn(timeZone)
        .toEpochMilliseconds()
}

/**
 * Formats a LocalTime as a 12-hour time string with AM/PM indicator.
 *
 * Examples:
 * - LocalTime(9, 0) -> "9:00 AM"
 * - LocalTime(14, 30) -> "2:30 PM"
 * - LocalTime(0, 15) -> "12:15 AM"
 * - LocalTime(12, 0) -> "12:00 PM"
 *
 * @return A formatted time string in "h:mm AM/PM" format.
 */
val LocalTime.formattedTime: String
    get() {
        val displayHour = when {
            hour == 0 || hour == 12 -> 12
            else -> hour % 12
        }
        val displayMinute = minute.toString().padStart(2, '0')
        val amPm = if (hour < 12) "AM" else "PM"
        return "$displayHour:$displayMinute $amPm"
    }

/**
 * Converts a timestamp to a LocalDate.
 * This method interprets the timestamp in the passed in timezone, and returns the calendar
 * date that the timestamp falls on in that timezone.
 *
 * This is the inverse of [toEpochMilliseconds]. Note that the two must be called with the *same*
 * timezone to round trip: a timestamp created at midnight in one zone can land on the previous or
 * next calendar day when read in another.
 *
 * @param timeZone the TimeZone to use when determining which calendar date the timestamp falls on.
 *   Defaults to the current system timezone.
 *
 * @return The LocalDate that this timestamp (a number of milliseconds from the unix epoch) falls on.
 */
@OptIn(ExperimentalTime::class)
fun Long.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    return Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(timeZone)
        .date
}
