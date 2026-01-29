package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.ui.common.forms.TimeTextField
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.features.teetimes.TTR


/**
 * Renders a Time Picker input field that can be toggled between an OutlinedTextField
 * in which the user types a time in HH:MM format (24-hour), and a dialog that displays a time picker.
 * The default implementation will display the text field, with an icon that can be used to
 * show the dialog.
 *
 * The selected time is made available via the passed in TimePickerState.
 *
 * Note that the TimePickerState controls the initial display of the field. It is recommended
 * to pass a TimePickerState with `showDialog = false`. If showDialog is set to true,
 * then the dialog will be displayed when entering the composition.
 *
 * @param pickerState A TtcTimePickerState to control the display of the picker as well as the selected time.
 * @param pickerDialogTitle A title to be displayed at the top of the picker Dialog when it is displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtcTimePicker(
    pickerState: TtcTimePickerState = rememberTtcTimePickerState(),
    pickerDialogTitle: String = "Select Time"
) {
    val focusManager = LocalFocusManager.current

    TimeTextField(
        label = stringResource(TTR.strings.field_Label_Time.resourceId),
        time = pickerState.selectedTime,
        onValueChange = { newTime ->
            pickerState.selectedTime = newTime
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        trailingContent = {
            IconButton(onClick = { pickerState.showDialog = true }) {
                Icon(TtcIcons.CALENDAR_CLOCK.painter, contentDescription = "Select Time")
            }
        }
    )

    if (pickerState.showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = pickerState.selectedTime?.hour ?: 9,
            initialMinute = pickerState.selectedTime?.minute ?: 0,
            is24Hour = false  // Use 12-hour format
        )

        val confirmEnabled = true // Time picker always has a valid selection

        TimePickerDialog(
            onDismissRequest = { pickerState.showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedTime = LocalTime(timePickerState.hour, timePickerState.minute)
                        pickerState.showDialog = false
                    },
                    enabled = confirmEnabled
                ) {
                    Text("OK")
                }
            }
        ) {
            TimeInput(state = timePickerState)
        }
    }
}

/**
 * State holder for TtcTimePicker.
 *
 * @param selectedTime The currently selected time, or null if no time is selected.
 * @param showDialog Whether to show the time picker dialog.
 */
class TtcTimePickerState(
    selectedTime: LocalTime? = null,
    showDialog: Boolean = false
) {
    var selectedTime by mutableStateOf(selectedTime)
    var showDialog by mutableStateOf(showDialog)
}

/**
 * Creates and remembers a TtcTimePickerState.
 *
 * @param selectedTime The initially selected time, or null if no time is initially selected.
 * @param showDialog Whether to initially show the time picker dialog.
 */
@Composable
fun rememberTtcTimePickerState(
    selectedTime: LocalTime? = null,
    showDialog: Boolean = false
): TtcTimePickerState {
    return remember {
        TtcTimePickerState(selectedTime = selectedTime, showDialog = showDialog)
    }
}

/**
 * A dialog wrapper for the Material3 TimePicker.
 *
 * @param onDismissRequest Callback when the user dismisses the dialog.
 * @param confirmButton The confirm button composable.
 * @param content The content of the dialog, typically a TimePicker.
 */
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = content
    )
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TimePickerPreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val state = rememberTtcTimePickerState()
        Column(modifier = Modifier.padding(16.dp)) {
            TtcTimePicker(pickerState = state)

            Text("Selected Time: ${state.selectedTime}")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun TimePickerWithValuePreview() {
    Surface(modifier = Modifier.fillMaxSize()) {
        val state = rememberTtcTimePickerState(selectedTime = LocalTime(14, 30))
        Column(modifier = Modifier.padding(16.dp)) {
            TtcTimePicker(pickerState = state)

            Text("Selected Time: ${state.selectedTime}")
        }
    }
}