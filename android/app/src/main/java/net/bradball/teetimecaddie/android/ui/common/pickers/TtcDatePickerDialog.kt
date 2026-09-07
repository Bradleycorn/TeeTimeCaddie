package net.bradball.teetimecaddie.android.ui.common.pickers

import android.content.res.Configuration
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.core.extensions.toEpochMilliseconds
import net.bradball.teetimecaddie.core.extensions.toLocalDate
import net.bradball.teetimecaddie.core.models.GR

/**
 * A modal date picker from the Fairway Morning design system.
 *
 * Renders the Material 3 *modal* date picker in a dialog: a `surfaceContainerHigh` container with a
 * headline, a month grid with prev/next navigation, a calendar ⇄ keyboard-input toggle, and
 * Cancel / OK text actions. Selection is a draft until OK is pressed — Cancel discards it and
 * [onDateSelected] never fires.
 *
 * Dates outside `[minDate, maxDate]` are dimmed and inert. Both bounds default to the design's
 * behavior for a tee-time date: no earlier than today, no more than a year out.
 *
 * The caller owns visibility:
 * ```kotlin
 * var showPicker by remember { mutableStateOf(false) }
 * if (showPicker) {
 *     TtcDatePickerDialog(
 *         selectedDate = teeTimeDate,
 *         onDateSelected = { teeTimeDate = it },
 *         onDismiss = { showPicker = false },
 *     )
 * }
 * ```
 *
 * There is deliberately no `initialDisplayMode` parameter — the design always opens on the calendar,
 * and the header's toggle already gets the user to keyboard entry, so no caller would need it. (As
 * with `TtcCard`'s absent `enabled`, the omission is intentional.)
 *
 * Four details of the design mock are deliberately *not* reproduced, because Material 3 supplies its
 * own and matching the mock would mean replacing the component with a hand-built calendar:
 * - the headline reads "Sep 15, 2026" rather than the mock's "Mon, Sep 15" (which drops the year),
 * - the month label is the M3 "Sep 2026 ▾" button that opens the year grid; the mock has no year picker,
 * - the `‹` chevron stays enabled at [minDate]'s month (the days behind it are dimmed and inert),
 * - keyboard-input validation shows the localized M3 messages rather than the mock's custom copy.
 *
 * Don't "fix" these without revisiting that trade-off.
 *
 * @param selectedDate The currently selected date, or null when nothing is selected yet. Seeds the
 *   picker and the month it opens on.
 * @param onDateSelected Invoked with the chosen date when OK is pressed. Not called on Cancel.
 * @param onDismiss Invoked when the dialog should close — on Cancel, on OK, and on scrim/back dismissal.
 * @param modifier Modifier for the dialog's content.
 * @param minDate The earliest selectable date, inclusive. Defaults to today.
 * @param maxDate The latest selectable date, inclusive. Defaults to a year from today.
 */
@Composable
fun TtcDatePickerDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate = TtcDatePickerDefaults.MinDate,
    maxDate: LocalDate = TtcDatePickerDefaults.MaxDate,
) {
    require(minDate <= maxDate) {
        "TtcDatePickerDialog: minDate ($minDate) must not be after maxDate ($maxDate)."
    }

    // Every timestamp crossing the DatePickerState boundary is normalized through TimeZone.UTC,
    // because `selectedDateMillis` is defined as UTC midnight — while `minDate`/`maxDate` describe
    // calendar days in the user's own zone. Reading one in the other's zone is an off-by-one-day bug.
    val state = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochMilliseconds(TimeZone.UTC),
        yearRange = TtcDatePickerDefaults.yearRange(minDate, maxDate),
        selectableDates = TtcDatePickerDefaults.selectableDates(minDate, maxDate),
    )

    val colors = TtcDatePickerDefaults.colors()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        colors = colors,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onDateSelected(it.toLocalDate(TimeZone.UTC)) }
                    onDismiss()
                },
                enabled = state.selectedDateMillis != null,
            ) {
                Text(stringResource(GR.strings.ok.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(GR.strings.cancel.resourceId))
            }
        },
    ) {
        // The dialog's "Select date" / "Enter date" title and its headline come from Material 3 and
        // are already localized, so they are left alone rather than overridden with app strings.
        DatePicker(state = state, colors = colors)
    }
}

// ─── Preview-only stand-ins ──────────────────────────────────────────────────
// `DatePickerDialog` puts its content in a `BasicAlertDialog`, i.e. a real platform dialog *window*
// via `androidx.compose.ui.window.Dialog`. Compose's `Dialog` has no `LocalInspectionMode` handling
// and Studio's preview host (`ComposeViewAdapter`) has no dialog/popup handling, so the window is
// never rasterized: previewing `TtcDatePickerDialog` directly renders an empty frame with no error.
// Wrapping it in a `Surface` doesn't help — the dialog isn't hidden behind anything, it simply isn't
// drawn.
//
// So the scaffold below rebuilds the dialog's own surface and layout around the real `DatePicker` and
// the real `TtcDatePickerDefaults.colors()`, with metrics lifted from M3's `DatePickerDialog`: the
// 360x568dp `DatePickerModalTokens` container, `DatePickerDefaults.shape`, the colors' own
// `containerColor`, `DialogTokens`' level-3 tonal elevation, and the buttons in an end-aligned row
// inset by 8dp/6dp with 8dp between them. `DatePicker` draws its own "Select date" title and headline,
// so only the actions row is reconstructed. What this cannot reproduce is the dialog window and its
// scrim. It is a preview scaffold, not a second code path — `TtcDatePickerDialog` is what ships.
//
// `initialDisplayMode` is also deliberately absent from the public API, which is the other reason the
// keyboard-entry face has to be assembled here to be previewable.

/** `DialogTokens.ContainerElevation` (= `ElevationTokens.Level3`), which is `internal` to material3. */
private val DialogTonalElevation = 6.dp

/** `DatePickerModalTokens.ContainerWidth` / `ContainerHeight`, which are `internal` to material3. */
private val DialogContainerWidth = 360.dp
private val DialogContainerMaxHeight = 568.dp

@Composable
private fun DatePickerDialogPreviewScaffold(state: DatePickerState, colors: DatePickerColors) {
    Surface(
        modifier = Modifier
            .requiredWidth(DialogContainerWidth)
            .heightIn(max = DialogContainerMaxHeight),
        shape = DatePickerDefaults.shape,
        color = colors.containerColor,
        tonalElevation = DialogTonalElevation,
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Box(Modifier.weight(1f, fill = false)) {
                DatePicker(state = state, colors = colors)
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 8.dp, end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(onClick = {}) { Text(stringResource(GR.strings.cancel.resourceId)) }
                TextButton(onClick = {}) { Text(stringResource(GR.strings.ok.resourceId)) }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcDatePickerDialogPreview() {
    val minDate = TtcDatePickerDefaults.MinDate
    val maxDate = TtcDatePickerDefaults.MaxDate

    MyApplicationTheme {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = minDate.plus(3, DateTimeUnit.DAY)
                .toEpochMilliseconds(TimeZone.UTC),
            yearRange = TtcDatePickerDefaults.yearRange(minDate, maxDate),
            selectableDates = TtcDatePickerDefaults.selectableDates(minDate, maxDate),
        )

        DatePickerDialogPreviewScaffold(state = state, colors = TtcDatePickerDefaults.colors())
    }
}

@Preview(name = "Input - Light", showBackground = true)
@Preview(name = "Input - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcDatePickerDialogInputPreview() {
    val minDate = TtcDatePickerDefaults.MinDate
    val maxDate = TtcDatePickerDefaults.MaxDate

    MyApplicationTheme {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = minDate.toEpochMilliseconds(TimeZone.UTC),
            yearRange = TtcDatePickerDefaults.yearRange(minDate, maxDate),
            initialDisplayMode = DisplayMode.Input,
            selectableDates = TtcDatePickerDefaults.selectableDates(minDate, maxDate),
        )

        DatePickerDialogPreviewScaffold(state = state, colors = TtcDatePickerDefaults.colors())
    }
}
