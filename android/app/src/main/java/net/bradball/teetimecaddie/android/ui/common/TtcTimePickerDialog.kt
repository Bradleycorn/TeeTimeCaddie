package net.bradball.teetimecaddie.android.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.core.models.GR

/**
 * A reusable time picker dialog that allows users to select a time.
 *
 * @param onDismiss Callback when the user dismisses the dialog.
 * @param onTimeSelected Callback when the user confirms a time selection.
 * @param initialHour The initial hour to display (default 9).
 * @param initialMinute The initial minute to display (default 0).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtcTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialHour: Int = 9,
    initialMinute: Int = 0
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text(stringResource(GR.strings.ok.resourceId))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(GR.strings.cancel.resourceId))
            }
        },
        text = {
            TimeInput(state = timePickerState)
        }
    )
}