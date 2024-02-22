package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import net.bradball.teetimecaddie.android.ui.common.forms.DateTextField
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.extensions.toEpochMilliseconds
import net.bradball.teetimecaddie.features.teetimes.TTR

/**
 * Renders a Date Picker input field that can be toggled between an OutlinedTextField
 * in which the user types a date in MM/DD/YYYY format, and a dialog that displays a date picker.
 * The default implementation will display the text field, with an icon that can be used to
 * show the dialog.
 *
 * The selected date is made available via the passed in DatePickerState.
 *
 * Note that the DatePickerState controls the initial display of the field. It is recommended
 * to pass a DatePickerState with an `initialDisplayMode = DisplayMode.Input`. If the initialDisplayMode
 * is set to "Picker", then the dialog will be displayed when entering the composition.
 *
 * @param pickerState A DatePickerState to control the display of the picker as well as the selected date.
 * @param pickerDialogTitle A title to be displayed at the top of the picker Dialog when it is displayed.
 * @param validator (Long)->Boolean A validator method that can be used to limit the range of selectable dates.
 *    It is passed a timestamp for the currently selected Date (at midnight, UTC). The validator method
 *    should return true if the date is allowed, or false if it is not.
 */
@Composable
fun TtcDatePicker(
    pickerState: DatePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Input),
    pickerDialogTitle: String = "Select Date",
    validator: (Long) -> Boolean = { selectedDate ->
        selectedDate >= Clock.System.todayIn(TimeZone.UTC).toEpochMilliseconds(TimeZone.UTC)
    }
) {
    val focusManager = LocalFocusManager.current
    val formatter = remember { DatePickerFormatter() }

    DateTextField(
        label = stringResource(TTR.strings.field_label_Date.resourceId),
        date = pickerState.selectedDate,
        onValueChange = { newDate ->
            pickerState.setSelection(newDate)
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        trailingContent = {
            IconButton(onClick = { pickerState.displayMode = DisplayMode.Picker }) {
                Icon(imageVector = TtcIcons.Calendar, contentDescription = "Select Date")
            }
        }
    )

    if (pickerState.displayMode == DisplayMode.Picker) {

        val confirmEnabled = pickerState.selectedDateMillis != null

        DatePickerDialog(
            onDismissRequest = { pickerState.displayMode = DisplayMode.Input },
            confirmButton = {
                TextButton(
                    onClick = { pickerState.displayMode = DisplayMode.Input },
                    enabled = confirmEnabled) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                dateFormatter = formatter,
                showModeToggle = false,
                dateValidator = validator,
                title = { Text(pickerDialogTitle, modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp)))})
        }
    }
}

/**
 * An extension to DatePickerState to get the selectedDateMillis
 * as a LocalDate, or null if there is no selected Date.
 * The date returned is in UTC.
 */
val DatePickerState.selectedDate: LocalDate?
    get() = selectedDateMillis?.let { timestamp ->
        Instant
            .fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.UTC)
            .date
    }

/**
 * An extension to DatePickerState to set the selected date
 * using a LocalDate. DatePickerState tracks it's selected
 * date via a timestamp in UTC. This method will set the
 * timestamp using the passed in LocalDate.
 *
 * @param date A LocalDate to use to set the selected date.
 */
fun DatePickerState.setSelection(date: LocalDate?) {
    setSelection(date?.toEpochMilliseconds(TimeZone.UTC))
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFFFFFFF,)
@Composable
fun Preview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val state = rememberDatePickerState(initialDisplayMode = DisplayMode.Input)
        Column(modifier = Modifier.padding(16.dp)) {
            TtcDatePicker(pickerState = state)

            Text("Selected Date: ${state.selectedDate}")
        }
    }
}
