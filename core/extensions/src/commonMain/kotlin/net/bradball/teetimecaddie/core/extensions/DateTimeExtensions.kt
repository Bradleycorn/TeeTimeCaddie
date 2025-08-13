package net.bradball.teetimecaddie.core.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

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
fun LocalDate.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long {
    return this.atStartOfDayIn(timeZone)
        .toEpochMilliseconds()
}
