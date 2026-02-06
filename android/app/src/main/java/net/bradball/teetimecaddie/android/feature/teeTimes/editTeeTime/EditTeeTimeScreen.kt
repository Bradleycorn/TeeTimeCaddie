package net.bradball.teetimecaddie.android.feature.teeTimes.editTeeTime

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.feature.teeTimes.common.TeeTimeFormContent
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.ContentLoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
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
        TeeTimeFormContent(
            courseName = courseName,
            initialDate = date,
            timeSlots = timeSlots,
            isLoading = showSavingProgress,
            buttonText = stringResource(GR.strings.save.resourceId),
            canSave = { name, selectedDate, slots ->
                name.isNotBlank() && selectedDate != null && slots.isNotEmpty() && hasChanges
            },
            onCourseNameChange = onCourseNameChange,
            onDateChange = onDateChange,
            onAddTimeClick = onAddTimeClick,
            onAddTimeSlot = onAddTimeSlot,
            onUpdatePlayerCount = onUpdatePlayerCount,
            onRemoveTimeSlot = onRemoveTimeSlot,
            onSaveClick = onSaveClick,
            modifier = Modifier.padding(padding)
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
