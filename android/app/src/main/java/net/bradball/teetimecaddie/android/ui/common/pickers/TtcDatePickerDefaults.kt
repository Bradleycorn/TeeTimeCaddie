package net.bradball.teetimecaddie.android.ui.common.pickers

import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import net.bradball.teetimecaddie.core.extensions.toEpochMilliseconds
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Shared bounds and color values for [TtcDatePickerDialog], derived from the Fairway Morning
 * "Date pickers" spec (the Material 3 *modal* date picker: a `surfaceContainerHigh` container, a
 * `primary` disc on the selected day, a `primary` outline on today, and past days dimmed and inert).
 * Colors always resolve to semantic Material 3 roles — never raw hex.
 *
 * Mirrors the structure of `TtcTextFieldDefaults`.
 */
internal object TtcDatePickerDefaults {

    /**
     * The default earliest selectable date — today, in the device's timezone.
     *
     * The design dims and disables every past day on the date field, so "today" is the floor for
     * every current caller. Declared as a `get()` rather than an initialized `val` so it re-reads
     * the clock instead of freezing on whichever day this object was first touched.
     */
    @OptIn(ExperimentalTime::class)
    val MinDate: LocalDate
        get() = Clock.System.todayIn(TimeZone.currentSystemDefault())

    /**
     * The default latest selectable date — one year out from [MinDate].
     *
     * This is a *product* choice, not a design constraint: the design specifies no upper bound, but
     * the picker's year grid needs one, and nobody schedules a golf game more than a season or two
     * ahead. Callers that need a different ceiling pass their own `maxDate`.
     */
    val MaxDate: LocalDate
        get() = MinDate.plus(1, DateTimeUnit.YEAR)

    /**
     * The [SelectableDates] that dims and disables everything outside `[minDate, maxDate]`.
     *
     * Both bounds are converted with [TimeZone.UTC] because `DatePickerState` defines every
     * timestamp it hands out — including the `utcTimeMillis` passed to [SelectableDates] — as
     * *UTC* midnight. See the timezone note in [TtcDatePickerDialog].
     *
     * [SelectableDates.isSelectableYear] is implemented alongside `isSelectableDate` so out-of-range
     * years are dimmed in the year grid too, rather than only revealing themselves as a month full
     * of disabled days.
     */
    fun selectableDates(minDate: LocalDate, maxDate: LocalDate): SelectableDates {
        val minMillis = minDate.toEpochMilliseconds(TimeZone.UTC)
        val maxMillis = maxDate.toEpochMilliseconds(TimeZone.UTC)

        return object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in minMillis..maxMillis
            override fun isSelectableYear(year: Int) = year in minDate.year..maxDate.year
        }
    }

    /** The span of years the picker's year grid offers, derived from the caller's date bounds. */
    fun yearRange(minDate: LocalDate, maxDate: LocalDate): IntRange = minDate.year..maxDate.year

    /**
     * [DatePickerColors] mapping the Fairway Morning date picker onto Material 3 roles:
     * - container: `surfaceContainerHigh`
     * - selected day: a `primary` disc with `onPrimary` content
     * - today: `primary` content inside a `primary` outline
     *
     * Every value here *matches* what [DatePickerDefaults.colors] would resolve on its own — the
     * app's `ColorScheme` is the Fairway Morning palette, so the stock M3 tokens already land on the
     * right roles. They are named explicitly anyway so the mapping is reviewable against the design
     * and can't drift silently if the theme changes. Everything the design doesn't call out (year
     * grid, weekday header, divider, input-mode field) is left to the M3 defaults.
     */
    @Composable
    fun colors(): DatePickerColors {
        val scheme = MaterialTheme.colorScheme
        return DatePickerDefaults.colors(
            containerColor = scheme.surfaceContainerHigh,
            selectedDayContainerColor = scheme.primary,
            selectedDayContentColor = scheme.onPrimary,
            todayContentColor = scheme.primary,
            todayDateBorderColor = scheme.primary,
        )
    }
}
