package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Today's date, then the new date each time midnight passes, for as long as it's collected.
 *
 * The time zone is read again each day, so a device that changes zones picks up the new one at
 * its next midnight.
 */
@OptIn(ExperimentalTime::class)
internal fun localDates(): Flow<LocalDate> = localDates(Clock.System) { TimeZone.currentSystemDefault() }

/** [localDates], read from [clock] in the zone [timeZone] returns. */
@ExperimentalTime
internal fun localDates(clock: Clock, timeZone: () -> TimeZone): Flow<LocalDate> = flow {
    while (true) {
        val zone = timeZone()
        val now = clock.now()
        val today = now.toLocalDateTime(zone).date
        emit(today)
        delay(today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(zone) - now)
    }
}.distinctUntilChanged()
