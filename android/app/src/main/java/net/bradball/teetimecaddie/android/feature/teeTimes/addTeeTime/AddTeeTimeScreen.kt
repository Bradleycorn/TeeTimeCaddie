package net.bradball.teetimecaddie.android.feature.teeTimes.addTeeTime

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.feature.teeTimes.common.TeeTimeFormContent
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.core.models.previewTeeTimeSlotList
import net.bradball.teetimecaddie.features.teetimes.TTR

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
        timeSlots = viewModel.timeSlots,
        onAddTimeClick = viewModel::onAddTimeClick,
        onAddTimeSlot = viewModel::addTimeSlot,
        onUpdatePlayerCount = viewModel::updatePlayerCount,
        onRemoveTimeSlot = viewModel::removeTimeSlot,
        onSaveClick = viewModel::saveTeeTime,
        onBackClick = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTeeTimeContent(
    showLoadingProgress: Boolean,
    timeSlots: List<TeeTimeSlot>,
    onAddTimeClick: () -> Unit,
    onAddTimeSlot: (LocalTime) -> Boolean,
    onUpdatePlayerCount: (LocalTime, Int) -> Unit,
    onRemoveTimeSlot: (LocalTime) -> Unit,
    onSaveClick: (String, LocalDate?) -> Unit,
    onBackClick: () -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

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
        TeeTimeFormContent(
            courseName = courseName,
            initialDate = null,
            timeSlots = timeSlots,
            isLoading = showLoadingProgress,
            buttonText = stringResource(TTR.strings.button_create.resourceId),
            canSave = { name, date, slots -> name.isNotBlank() && date != null && slots.isNotEmpty() },
            onCourseNameChange = { courseName = it },
            onDateChange = { selectedDate = it },
            onAddTimeClick = onAddTimeClick,
            onAddTimeSlot = onAddTimeSlot,
            onUpdatePlayerCount = onUpdatePlayerCount,
            onRemoveTimeSlot = onRemoveTimeSlot,
            onSaveClick = { onSaveClick(courseName, selectedDate) },
            modifier = Modifier.padding(padding)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTeeTimeContentPreview() {
    MyApplicationTheme {
        AddTeeTimeContent(
            showLoadingProgress = false,
            timeSlots = emptyList(),
            onAddTimeClick = { },
            onAddTimeSlot = { true },
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = { },
            onSaveClick = { _, _ -> },
            onBackClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddTeeTimeContentWithTimesPreview() {
    MyApplicationTheme {
        AddTeeTimeContent(
            showLoadingProgress = false,
            timeSlots = previewTeeTimeSlotList,
            onAddTimeClick = { },
            onAddTimeSlot = { true },
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = { },
            onSaveClick = { _, _ -> },
            onBackClick = { }
        )
    }
}
