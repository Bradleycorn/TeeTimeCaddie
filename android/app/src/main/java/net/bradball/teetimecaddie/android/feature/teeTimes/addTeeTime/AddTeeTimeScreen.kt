package net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.bradball.teetimecaddie.features.teetimes.TTR
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.bradball.teetimecaddie.android.ui.common.TtcDatePicker
import net.bradball.teetimecaddie.android.ui.common.TtcTimePicker
import net.bradball.teetimecaddie.android.ui.common.buttons.LoadingButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.rememberTtcDatePickerState
import net.bradball.teetimecaddie.android.ui.common.rememberTtcTimePickerState
import net.bradball.teetimecaddie.android.ui.common.selectedDate
import net.bradball.teetimecaddie.core.models.GR

@Composable
fun AddTeeTimeScreen(
    onBack: () -> Unit,
    onTeeTimeCreated: () -> Unit,
    viewModel: AddTeeTimeViewModel = hiltViewModel()
) {
    // Handle save success
    LaunchedEffect(viewModel.saveSuccess) {
        if (viewModel.saveSuccess) {
            onTeeTimeCreated()
        }
    }

    AddTeeTimeContent(
        showLoadingProgress = viewModel.showLoadingProgress,
        onSaveClick = viewModel::saveTeeTime,
        onBackClick = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeeTimeContent(
    showLoadingProgress: Boolean,
    onSaveClick: (String, LocalDate?, LocalTime?, Int) -> Unit,
    onBackClick: () -> Unit
) {
    var courseName by remember { mutableStateOf("")}
    var date = rememberTtcDatePickerState()
    var time = rememberTtcTimePickerState()
    var players by remember { mutableStateOf(4) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TTR.strings.add_tee_time.resourceId)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(TtcIcons.ARROW_BACK.painter, contentDescription = stringResource(GR.strings.back.resourceId)) 
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Course Name TextField
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text(stringResource(TTR.strings.field_label_course_name.resourceId)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Field
//            DatePickerField(
//                selectedDate = selectedDate,
//                onDateSelected = onDateSelected,
//                label = "Date (MM/DD/YYYY)"
//            )
            TtcDatePicker(pickerState = date)

            Spacer(modifier = Modifier.height(16.dp))

            // Time Picker Field
            TtcTimePicker(pickerState = time)

            Spacer(modifier = Modifier.height(16.dp))

            // Number of Players Slider
            NumberOfPlayersField(
                numberOfPlayers = players,
                onNumberChanged = { players = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            LoadingButton(
                text = stringResource(TTR.strings.button_create.resourceId),
                onClick = { onSaveClick(courseName, date.selectedDate, time.selectedTime, players) },
                enabled = courseName.isNotBlank() && date.selectedDate != null && time != null,
                modifier = Modifier.fillMaxWidth(),
                isLoading = showLoadingProgress
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerField(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime?.let { formatTime(it) } ?: "",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(TtcIcons.CALENDAR.painter, "Select Time")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    )

    if (showDialog) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.hour ?: 9,
            initialMinute = selectedTime?.minute ?: 0
        )
        TimePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime(timePickerState.hour, timePickerState.minute))
                    showDialog = false
                }) {
                    Text("OK")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
private fun NumberOfPlayersField(
    numberOfPlayers: Int,
    onNumberChanged: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(TTR.strings.field_Label_Players.resourceId),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = numberOfPlayers.toFloat(),
                onValueChange = { onNumberChanged(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = numberOfPlayers.toString(),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

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

private fun formatTime(time: LocalTime): String {
    val hour = if (time.hour == 0 || time.hour == 12) 12 else time.hour % 12
    val amPm = if (time.hour < 12) "AM" else "PM"
    return "$hour:${time.minute.toString().padStart(2, '0')} $amPm"
}
