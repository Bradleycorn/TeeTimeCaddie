package net.bradball.teetimecaddie.android.feature.games.newGame

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.Screen
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.forms.TtcTextField
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.pickers.TtcDatePickerDialog
import net.bradball.teetimecaddie.android.ui.common.pickers.TtcTimePickerDialog
import net.bradball.teetimecaddie.android.ui.common.preview.ScreenPreviews
import net.bradball.teetimecaddie.core.analytics.AnalyticsScreen
import net.bradball.teetimecaddie.core.extensions.formattedDate
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.features.games.GMR
import net.bradball.teetimecaddie.features.games.MAX_TEE_TIME_SLOTS

@Composable
fun NewGameScreen(
    onClose: () -> Unit,
    onPublished: () -> Unit,
    viewModel: NewGameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NewGameEvent.Published -> onPublished()
            }
        }
    }

    Screen(AnalyticsScreen.NewGame("NewGameScreen")) {
        NewGameContent(
            uiState = uiState,
            onClose = onClose,
            onCourseChange = viewModel::onCourseChange,
            onDateSelected = viewModel::onDateSelected,
            onTimeSelected = viewModel::onTimeSelected,
            onSlotsChange = viewModel::onSlotsChange,
            onPublishClick = viewModel::onPublishClick,
            onKeepEditing = viewModel::onKeepEditing,
            onPublishAnyway = viewModel::onPublishAnyway,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewGameContent(
    uiState: NewGameUiState,
    onClose: () -> Unit,
    onCourseChange: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onSlotsChange: (Int) -> Unit,
    onPublishClick: () -> Unit,
    onKeepEditing: () -> Unit,
    onPublishAnyway: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Which picker is open is purely presentational: the ViewModel only needs the committed value.
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        // Keeps Publish above the keyboard; the form scrolls beneath it.
        modifier = modifier.imePadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(GMR.strings.new_game_title.resourceId)) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = TtcIcons.CLOSE.painter,
                            contentDescription = stringResource(GMR.strings.new_game_close.resourceId),
                        )
                    }
                },
            )
        },
        bottomBar = {
            TtcButton(
                text = stringResource(GMR.strings.new_game_publish_button.resourceId),
                onClick = onPublishClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(NewGameDefaults.PublishPadding),
                enabled = uiState.canSubmit,
                isLoading = uiState.isSubmitting,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = NewGameDefaults.HorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(NewGameDefaults.FieldSpacing),
        ) {
            SectionLabel(stringResource(GMR.strings.new_game_section_where_when.resourceId))

            TtcTextField(
                value = uiState.course,
                onValueChange = onCourseChange,
                label = stringResource(GMR.strings.new_game_field_course.resourceId),
                leadingIcon = TtcIcons.GOLF_COURSE,
                placeholder = stringResource(GMR.strings.new_game_field_course_placeholder.resourceId),
                enabled = !uiState.isSubmitting,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                modifier = Modifier.fillMaxWidth(),
            )

            TtcTextField(
                value = uiState.date?.formattedDate.orEmpty(),
                onValueChange = {},
                label = stringResource(GMR.strings.new_game_field_date.resourceId),
                leadingIcon = TtcIcons.CALENDAR,
                placeholder = stringResource(GMR.strings.new_game_field_date_placeholder.resourceId),
                trailingIcon = TtcIcons.ARROW_DROP_DOWN,
                enabled = !uiState.isSubmitting,
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
            )

            TtcTextField(
                value = uiState.time?.formattedTime.orEmpty(),
                onValueChange = {},
                label = stringResource(GMR.strings.new_game_field_tee_time.resourceId),
                leadingIcon = TtcIcons.SCHEDULE,
                placeholder = stringResource(GMR.strings.new_game_field_tee_time_placeholder.resourceId),
                trailingIcon = TtcIcons.ARROW_DROP_DOWN,
                enabled = !uiState.isSubmitting,
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth(),
            )

            SectionLabel(stringResource(GMR.strings.new_game_section_players.resourceId))

            SlotSelector(
                selected = uiState.slots,
                onSelect = onSlotsChange,
                enabled = !uiState.isSubmitting,
            )
        }
    }

    if (showDatePicker) {
        TtcDatePickerDialog(
            selectedDate = uiState.date,
            onDateSelected = onDateSelected,
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        TtcTimePickerDialog(
            selectedTime = uiState.time,
            onTimeSelected = onTimeSelected,
            onDismiss = { showTimePicker = false },
        )
    }

    // Not drawn in the design: the platform's own alert, as the story specifies.
    if (uiState.showNoInviteeConfirm) {
        AlertDialog(
            onDismissRequest = onKeepEditing,
            title = { Text(stringResource(GMR.strings.new_game_no_invitees_title.resourceId)) },
            text = { Text(stringResource(GMR.strings.new_game_no_invitees_message.resourceId)) },
            confirmButton = {
                TextButton(onClick = onPublishAnyway) {
                    Text(stringResource(GMR.strings.new_game_no_invitees_publish_anyway.resourceId))
                }
            },
            dismissButton = {
                TextButton(onClick = onKeepEditing) {
                    Text(stringResource(GMR.strings.new_game_no_invitees_keep_editing.resourceId))
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = NewGameDefaults.SectionLabelInset,
            top = NewGameDefaults.SectionLabelTopPadding,
        ),
    )
}

/**
 * How many players the tee time holds: four equal pills, exactly one selected.
 *
 * Built from the shared buttons rather than M3's `SegmentedButton`, which doesn't match the design.
 */
@Composable
private fun SlotSelector(
    selected: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    TtcCard(modifier = modifier) {
        Text(
            text = stringResource(GMR.strings.new_game_player_slots_label.resourceId),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NewGameDefaults.SlotLabelSpacing),
            horizontalArrangement = Arrangement.spacedBy(NewGameDefaults.SlotSpacing),
        ) {
            (1..MAX_TEE_TIME_SLOTS).forEach { slots ->
                val pillModifier = Modifier.weight(1f)
                if (slots == selected) {
                    TtcButton(
                        text = slots.toString(),
                        onClick = { onSelect(slots) },
                        modifier = pillModifier,
                        enabled = enabled,
                    )
                } else {
                    TtcOutlinedButton(
                        text = slots.toString(),
                        onClick = { onSelect(slots) },
                        modifier = pillModifier,
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

private object NewGameDefaults {
    val HorizontalPadding = 16.dp
    val FieldSpacing = 8.dp
    val SectionLabelInset = 4.dp
    val SectionLabelTopPadding = 12.dp
    val SlotLabelSpacing = 10.dp
    val SlotSpacing = 8.dp
    val PublishPadding = 16.dp
}

@ScreenPreviews
@Composable
private fun NewGameEmptyPreview() {
    MyApplicationTheme {
        NewGameContent(
            uiState = NewGameUiState(),
            onClose = {},
            onCourseChange = {},
            onDateSelected = {},
            onTimeSelected = {},
            onSlotsChange = {},
            onPublishClick = {},
            onKeepEditing = {},
            onPublishAnyway = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun NewGameFilledPreview() {
    MyApplicationTheme {
        NewGameContent(
            uiState = NewGameUiState(
                course = "Pebble Beach",
                date = LocalDate(2026, 6, 13),
                time = LocalTime(9, 0),
                slots = 3,
            ),
            onClose = {},
            onCourseChange = {},
            onDateSelected = {},
            onTimeSelected = {},
            onSlotsChange = {},
            onPublishClick = {},
            onKeepEditing = {},
            onPublishAnyway = {},
        )
    }
}
