package net.bradball.teetimecaddie.android.feature.teeTimes.editTeeTime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.feature.teeTimes.common.TeeTimesSection
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.TtcDatePicker
import net.bradball.teetimecaddie.android.ui.common.TtcTimePickerDialog
import net.bradball.teetimecaddie.android.ui.common.buttons.LoadingButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.rememberTtcDatePickerState
import net.bradball.teetimecaddie.android.ui.common.selectedDate
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.core.models.previewTeeTimeSlotList
import net.bradball.teetimecaddie.features.teetimes.TTR

@Composable
fun EditTeeTimeScreen(
    viewModel: EditTeeTimeViewModel,
    onBack: () -> Unit,
    onTeeTimeUpdated: () -> Unit
) {
    // Handle save success
    LaunchedEffect(viewModel.saveSuccess) {
        if (viewModel.saveSuccess) {
            onTeeTimeUpdated()
        }
    }

    Screen(AnalyticsScreen.EditTeeTime("EditTeeTimeScreen")) {
        if (viewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ContentLoadingIndicator()
            }
        } else {
            EditTeeTimeContent(
                courseName = viewModel.courseName,
                date = viewModel.date,
                showSavingProgress = viewModel.showSavingProgress,
                timeSlots = viewModel.timeSlots,
                hasChanges = viewModel.hasChanges,
                onCourseNameChange = viewModel::updateCourseName,
                onDateChange = viewModel::updateDate,
                onAddTimeClick = viewModel::onAddTimeClick,
                onAddTimeSlot = viewModel::addTimeSlot,
                onUpdatePlayerCount = viewModel::updatePlayerCount,
                onRemoveTimeSlot = viewModel::removeTimeSlot,
                onSaveClick = viewModel::saveTeeTime,
                onBackClick = onBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTeeTimeContent(
    courseName: String,
    date: LocalDate?,
    showSavingProgress: Boolean,
    timeSlots: List<TeeTimeSlot>,
    hasChanges: Boolean,
    onCourseNameChange: (String) -> Unit,
    onDateChange: (LocalDate?) -> Unit,
    onAddTimeClick: () -> Unit,
    onAddTimeSlot: (LocalTime) -> Boolean,
    onUpdatePlayerCount: (LocalTime, Int) -> Unit,
    onRemoveTimeSlot: (LocalTime) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberTtcDatePickerState(initialDate = date)

    // Sync date picker state changes back to viewmodel
    LaunchedEffect(datePickerState.selectedDate) {
        if (datePickerState.selectedDate != date) {
            onDateChange(datePickerState.selectedDate)
        }
    }

    val isValid = courseName.isNotBlank() && datePickerState.selectedDate != null && timeSlots.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(TTR.strings.edit_tee_time.resourceId)) },
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
                onValueChange = onCourseNameChange,
                label = { Text(stringResource(TTR.strings.field_label_course_name.resourceId)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date Picker Field
            TtcDatePicker(pickerState = datePickerState)

            Spacer(modifier = Modifier.height(24.dp))

            // Tee Times Section
            TeeTimesSection(
                timeSlots = timeSlots,
                onAddTimeClick = {
                    onAddTimeClick()
                    showTimePickerDialog = true
                },
                onUpdatePlayerCount = onUpdatePlayerCount,
                onRemoveTimeSlot = onRemoveTimeSlot
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            LoadingButton(
                text = stringResource(GR.strings.save.resourceId),
                onClick = onSaveClick,
                enabled = isValid && hasChanges,
                modifier = Modifier.fillMaxWidth(),
                isLoading = showSavingProgress
            )
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TtcTimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { time ->
                onAddTimeSlot(time)
                showTimePickerDialog = false
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTeeTimeContentPreview() {
    MyApplicationTheme {
        EditTeeTimeContent(
            courseName = "Persimmon Ridge",
            date = null,
            showSavingProgress = false,
            timeSlots = previewTeeTimeSlotList,
            hasChanges = true,
            onCourseNameChange = {},
            onDateChange = {},
            onAddTimeClick = {},
            onAddTimeSlot = { true },
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EditTeeTimeContentNoChangesPreview() {
    MyApplicationTheme {
        EditTeeTimeContent(
            courseName = "Persimmon Ridge",
            date = null,
            showSavingProgress = false,
            timeSlots = previewTeeTimeSlotList,
            hasChanges = false,
            onCourseNameChange = {},
            onDateChange = {},
            onAddTimeClick = {},
            onAddTimeSlot = { true },
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = {},
            onSaveClick = {},
            onBackClick = {}
        )
    }
}
