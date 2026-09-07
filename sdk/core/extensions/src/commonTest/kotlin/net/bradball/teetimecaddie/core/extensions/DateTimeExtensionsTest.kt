package net.bradball.teetimecaddie.core.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeExtensionsTest {

    @Test
    fun toLocalDate_roundTripsThroughUtc() {
        val date = LocalDate(2026, 9, 15)

        val roundTripped = date.toEpochMilliseconds(TimeZone.UTC).toLocalDate(TimeZone.UTC)

        assertEquals(date, roundTripped)
    }

    @Test
    fun toLocalDate_roundTripsThroughANonUtcZone() {
        val zone = TimeZone.of("America/Los_Angeles")
        val date = LocalDate(2026, 9, 15)

        val roundTripped = date.toEpochMilliseconds(zone).toLocalDate(zone)

        assertEquals(date, roundTripped)
    }

    @Test
    fun toLocalDate_readsTheUnderlyingInstantInTheGivenZone() {
        // Midnight Sept 15 in Los Angeles (UTC-7 in September) is 07:00 Sept 15 UTC, so both zones
        // agree here...
        val millis = LocalDate(2026, 9, 15).toEpochMilliseconds(TimeZone.of("America/Los_Angeles"))
        assertEquals(LocalDate(2026, 9, 15), millis.toLocalDate(TimeZone.UTC))

        // ...but midnight Sept 15 in Tokyo (UTC+9) is 15:00 Sept 14 UTC, which is the previous day.
        val tokyoMillis = LocalDate(2026, 9, 15).toEpochMilliseconds(TimeZone.of("Asia/Tokyo"))
        assertEquals(LocalDate(2026, 9, 14), tokyoMillis.toLocalDate(TimeZone.UTC))
    }

    @Test
    fun toLocalDate_handlesTheUnixEpoch() {
        assertEquals(LocalDate(1970, 1, 1), 0L.toLocalDate(TimeZone.UTC))
    }
}
