package net.bradball.teetimecaddie.android.ui.common.pickers

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalTime

/**
 * Shared seed and color values for [TtcTimePickerDialog], derived from the Fairway Morning
 * "Time pickers" spec (the Material 3 *modal* time picker: a `surfaceContainerHigh` container, a
 * `surfaceContainerHighest` dial with a `primary` hand, `primaryContainer` hour/minute boxes, and an
 * outlined AM/PM toggle). Colors always resolve to semantic Material 3 roles — never raw hex.
 *
 * Mirrors the structure of `TtcDatePickerDefaults`, with one structural difference worth knowing:
 * **`TimePickerDialog` takes no `colors` parameter.** The date picker threads a single
 * `DatePickerColors` into both the dialog and the `DatePicker`; here the container color comes from
 * `TimePickerDialogDefaults.containerColor` (a default we don't touch, already `surfaceContainerHigh`)
 * while [colors] feeds only the `TimePicker` / `TimeInput` content. Two independent sources.
 */
internal object TtcTimePickerDefaults {

    /**
     * The time the dialog opens on when the caller has no time selected yet.
     *
     * This is a *product* choice, not a design constraint — though the design's mock seeds the same
     * 7:00 AM. Golf is a morning game, so this is usually a tap or two from the real answer, whereas
     * seeding "now" would open on whatever time the user happens to be scheduling at.
     *
     * Unlike `TtcDatePickerDefaults.MinDate` this is a plain `val`, not a `get()`: a fixed 7:00 AM
     * has no clock to re-read and so nothing to freeze.
     */
    val SeedTime = LocalTime(7, 0)

    /**
     * [TimePickerColors] mapping the Fairway Morning time picker onto Material 3 roles:
     * - dial: `surfaceContainerHighest`, with a `primary` hand and center dot
     * - selected dial numeral: `onPrimary`; unselected: `onSurface`
     * - active hour/minute box: `primaryContainer` with `onPrimaryContainer` content
     * - inactive hour/minute box: `surfaceContainerHighest` with `onSurface` content
     * - AM/PM toggle: an `outline` border, transparent when unselected with `onSurfaceVariant` content
     *
     * As with `TtcDatePickerDefaults.colors`, every value here *matches* what
     * [TimePickerDefaults.colors] resolves on its own — the app's `ColorScheme` is the Fairway
     * Morning palette, so the stock M3 tokens already land on the right roles. They are named
     * anyway so the mapping is reviewable against the design and can't drift silently if the theme
     * changes.
     *
     * Note: always build a [TimePickerColors] through [TimePickerDefaults.colors], never through
     * `someColors.copy(...)`. M3 1.4.0's `TimePickerColors.copy` defaults its first parameter to the
     * wrong field (`clockDialColor: Color = this.containerColor`); the `colors(...)` helper above
     * masks that bug by passing all fourteen arguments explicitly.
     */
    @Composable
    fun colors(): TimePickerColors {
        val scheme = MaterialTheme.colorScheme
        return TimePickerDefaults.colors(
            // Dial
            clockDialColor = scheme.surfaceContainerHighest,
            clockDialSelectedContentColor = scheme.onPrimary,
            clockDialUnselectedContentColor = scheme.onSurface,
            selectorColor = scheme.primary,
            // Hour / minute boxes
            timeSelectorSelectedContainerColor = scheme.primaryContainer,
            timeSelectorSelectedContentColor = scheme.onPrimaryContainer,
            timeSelectorUnselectedContainerColor = scheme.surfaceContainerHighest,
            timeSelectorUnselectedContentColor = scheme.onSurface,

            periodSelectorBorderColor = scheme.outline,
            periodSelectorSelectedContainerColor = scheme.primaryContainer,
            periodSelectorSelectedContentColor = scheme.onPrimaryContainer,
            periodSelectorUnselectedContentColor = scheme.onSurfaceVariant,
        )
    }
}
