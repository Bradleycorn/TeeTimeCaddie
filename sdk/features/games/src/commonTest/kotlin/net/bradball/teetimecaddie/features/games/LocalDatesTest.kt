package net.bradball.teetimecaddie.features.games

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class LocalDatesTest {

    // A clock that starts at [start] and moves with the test's virtual time.
    private fun TestScope.clockFrom(start: Instant) = object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(start.toEpochMilliseconds() + testScheduler.currentTime)
    }

    @Test
    fun emitsTodayThenEachDateAsMidnightPasses() = runTest {
        val lateEvening = LocalDateTime(2026, 6, 13, 23, 59).toInstant(TimeZone.UTC)

        val dates = localDates(clockFrom(lateEvening)) { TimeZone.UTC }.take(3).toList()

        assertEquals(listOf(LocalDate(2026, 6, 13), LocalDate(2026, 6, 14), LocalDate(2026, 6, 15)), dates)
    }

    @Test
    fun theNextDateArrivesAtMidnightNotBefore() = runTest {
        val lateEvening = LocalDateTime(2026, 6, 13, 23, 59).toInstant(TimeZone.UTC)

        localDates(clockFrom(lateEvening)) { TimeZone.UTC }.take(2).toList()

        // One minute from 11:59 PM to midnight.
        assertEquals(60_000L, testScheduler.currentTime)
    }
}
