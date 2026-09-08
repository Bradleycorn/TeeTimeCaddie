package net.bradball.teetimecaddie.android.ui.common.pickers

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TimePickerDialogDefaults
import androidx.compose.material3.TimePickerDisplayMode
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.core.models.GR

/**
 * A modal time picker from the Fairway Morning design system.
 *
 * Renders the Material 3 *modal* time picker in a dialog: a `surfaceContainerHigh` container with a
 * title, tappable hour and minute boxes beside an AM/PM toggle, a dial with a `primary` hand, a
 * dial ⇄ keyboard-input toggle in the bottom-left, and Cancel / OK text actions. Selection is a
 * draft until OK is pressed — Cancel discards it and [onTimeSelected] never fires.
 *
 * The caller owns visibility:
 * ```kotlin
 * var showPicker by remember { mutableStateOf(false) }
 * if (showPicker) {
 *     TtcTimePickerDialog(
 *         selectedTime = teeTimeTime,
 *         onTimeSelected = { teeTimeTime = it },
 *         onDismiss = { showPicker = false },
 *     )
 * }
 * ```
 *
 * There is deliberately no `initialDisplayMode` parameter — the design always opens on the dial, and
 * the toggle already gets the user to keyboard entry. (As with `TtcCard`'s absent `enabled`, the
 * omission is intentional.) There are deliberately **no time bounds** either: Material 3 offers no
 * time-side equivalent of `SelectableDates`, so a `minTime`/`maxTime` would mean replacing the dial
 * with a hand-built one. The design specifies none.
 *
 *
 * @param selectedTime The currently selected time, or null when nothing is selected yet — in which
 *   case the picker opens on [TtcTimePickerDefaults.SeedTime].
 * @param onTimeSelected Invoked with the chosen time when OK is pressed. Not called on Cancel. Note
 *   that OK is always enabled: `TimePickerState` exposes non-nullable `hour`/`minute` with no "unset"
 *   value (unlike `DatePickerState.selectedDateMillis`), so there is no condition to gate on. OK on
 *   an untouched dialog therefore commits the seeded time.
 * @param onDismiss Invoked when the dialog should close — on Cancel, on OK, and on scrim/back dismissal.
 * @param modifier Modifier for the dialog's content.
 */
@Composable
fun TtcTimePickerDialog(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val seed = selectedTime ?: TtcTimePickerDefaults.SeedTime

    // `is24Hour` is omitted so the picker follows the device's own 12/24-hour setting. It can't be
    // inspected from app code either way — material3 declares `is24HourFormat` as `internal expect`.
    //
    // Note that `rememberTimePickerState` wraps `rememberSaveable` with no keys, so the seed is read
    // exactly once. That's correct *because* the caller owns visibility and composes the dialog
    // fresh; a caller that kept it composed and mutated `selectedTime` would find it doesn't follow.
    val state = rememberTimePickerState(initialHour = seed.hour, initialMinute = seed.minute)

    val colors = TtcTimePickerDefaults.colors()

    // Plain `remember`, not `rememberSaveable`: TimePickerDisplayMode is a @JvmInline value class, so
    // it boxes to a type that is neither Parcelable nor Serializable and would throw when saved.
    // Rotation therefore keeps the chosen hour/minute (TimePickerState has its own Saver) but reopens
    // on the dial.
    var displayMode by remember { mutableStateOf(TimePickerDisplayMode.Picker) }

    val configuration = LocalConfiguration.current
    val dialFits = configuration.screenHeightDp.dp > TimePickerDialogDefaults.MinHeightForTimePicker

    // The mode the dialog can actually render, as opposed to the mode the user asked for. Keeping the
    // two apart means a short window falls back to keyboard entry *without* destroying the user's
    // choice, so the dial and its toggle return on their own when the window grows — rotation,
    // unfolding, leaving split-screen. All of those recompose LocalConfiguration.
    val effectiveMode = if (dialFits) displayMode else TimePickerDisplayMode.Input

    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime(state.hour, state.minute))
                    onDismiss()
                },
            ) {
                Text(stringResource(GR.strings.ok.resourceId))
            }
        },
        // The title tracks `effectiveMode`, not `displayMode`, so a short screen showing keyboard
        // entry reads "Enter time". M3's own TimePickerSwitchableSample gets this wrong.
        title = { TimePickerDialogDefaults.Title(displayMode = effectiveMode) },
        modifier = modifier,
        // Null rather than a lambda that conditionally emits nothing: the dialog then lays out the
        // actions row as spacer + Cancel + OK, with no empty slot to measure.
        modeToggleButton = if (dialFits) {
            {
                TimePickerDialogDefaults.DisplayModeToggle(
                    onDisplayModeChange = {
                        displayMode = if (displayMode == TimePickerDisplayMode.Picker) {
                            TimePickerDisplayMode.Input
                        } else {
                            TimePickerDisplayMode.Picker
                        }
                    },
                    displayMode = displayMode,
                )
            }
        } else {
            null
        },
        // `onDismissRequest` is documented as *not* firing for the dismiss button, so Cancel has to
        // call `onDismiss` itself. This is the only path that must never touch `onTimeSelected`.
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(GR.strings.cancel.resourceId))
            }
        },
        // `shape` and `containerColor` are left at their defaults — already the design's `extraLarge`
        // and `surfaceContainerHigh`.
    ) {
        if (effectiveMode == TimePickerDisplayMode.Picker) {
            TimePicker(state = state, colors = colors)
        } else {
            TimeInput(state = state, colors = colors)
        }
    }
}

// ─── Preview-only stand-ins ──────────────────────────────────────────────────
// `TimePickerDialog` puts its content in an `androidx.compose.ui.window.Dialog`, i.e. a real platform
// dialog *window*. Compose's `Dialog` has no `LocalInspectionMode` handling and Studio's preview host
// (`ComposeViewAdapter`) has no dialog/popup handling, so the window is never rasterized: previewing
// `TtcTimePickerDialog` directly renders an empty frame with no error. Wrapping it in a `Surface`
// doesn't help — the dialog isn't hidden behind anything, it simply isn't drawn.
//
// So the scaffold below rebuilds the dialog's own surface and portrait layout around the real
// `TimePicker` / `TimeInput` and the real `TtcTimePickerDefaults.colors()`. The metrics are lifted
// from M3's `TimePickerDialogLayout` / `TimePickerCustomLayout` (which are `internal`, so they can't
// just be called): the same `TimePickerDialogDefaults` shape and container color, `DialogTokens`'
// level-3 tonal elevation, 24dp content padding on all sides (`contentPadding`, `portTitleTopPadding`
// and `portActionsBottomPadding` are all 24dp), M3's own `Title` — which carries its own 20dp bottom
// padding — and the actions row as `toggle | weight(1f) | dismiss | confirm` spanning the content
// width. What it cannot reproduce is the dialog window itself and its scrim, plus M3's landscape
// branch. This is a preview scaffold, not a second code path: `TtcTimePickerDialog` is what ships.

/** `DialogTokens.ContainerElevation` (= `ElevationTokens.Level3`), which is `internal` to material3. */
private val DialogTonalElevation = 6.dp

@Composable
private fun TimePickerDialogPreviewScaffold(
    displayMode: TimePickerDisplayMode,
    content: @Composable () -> Unit,
) {
    Surface(
        shape = TimePickerDialogDefaults.shape,
        color = TimePickerDialogDefaults.containerColor,
        tonalElevation = DialogTonalElevation,
    ) {
        // `IntrinsicSize.Max` reproduces M3's own measurement: it sizes the column to the picker
        // content's natural width, which is what `TimePickerCustomLayout` constrains the actions row
        // to. Without it, `fillMaxWidth` collapses under a preview's unbounded width and the
        // Cancel/OK buttons hug the toggle instead of sitting at the content's right edge.
        Column(modifier = Modifier.width(IntrinsicSize.Max).padding(24.dp)) {
            TimePickerDialogDefaults.Title(displayMode = displayMode)
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimePickerDialogDefaults.DisplayModeToggle(
                    onDisplayModeChange = {},
                    displayMode = displayMode,
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {}) { Text(stringResource(GR.strings.cancel.resourceId)) }
                TextButton(onClick = {}) { Text(stringResource(GR.strings.ok.resourceId)) }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTimePickerDialogPreview() {
    MyApplicationTheme {
        val seed = TtcTimePickerDefaults.SeedTime
        val state = rememberTimePickerState(initialHour = seed.hour, initialMinute = seed.minute)

        TimePickerDialogPreviewScaffold(displayMode = TimePickerDisplayMode.Picker) {
            TimePicker(state = state, colors = TtcTimePickerDefaults.colors())
        }
    }
}

@Preview(name = "Input - Light", showBackground = true)
@Preview(name = "Input - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcTimePickerDialogInputPreview() {
    MyApplicationTheme {
        val seed = TtcTimePickerDefaults.SeedTime
        val state = rememberTimePickerState(initialHour = seed.hour, initialMinute = seed.minute)

        TimePickerDialogPreviewScaffold(displayMode = TimePickerDisplayMode.Input) {
            TimeInput(state = state, colors = TtcTimePickerDefaults.colors())
        }
    }
}
