package net.bradball.teetimecaddie.android.feature.teeTimes.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.core.models.previewTeeTimeSlotList
import net.bradball.teetimecaddie.features.teetimes.TTR

/**
 * A section displaying tee time slots with the ability to add, update, and remove slots.
 *
 * This composable is shared between the Add and Edit tee time screens.
 *
 * @param timeSlots The list of time slots to display.
 * @param onAddTimeClick Callback when the add time button is clicked.
 * @param onUpdatePlayerCount Callback when the player count is changed for a slot.
 * @param onRemoveTimeSlot Callback when a time slot is removed.
 */
@Composable
fun TeeTimesSection(
    timeSlots: List<TeeTimeSlot>,
    onAddTimeClick: () -> Unit,
    onUpdatePlayerCount: (LocalTime, Int) -> Unit,
    onRemoveTimeSlot: (LocalTime) -> Unit
) {
    Column {
        // Section Header
        Text(
            text = stringResource(TTR.strings.tee_times_section_header.resourceId),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of time slots
        if (timeSlots.isNotEmpty()) {
            timeSlots.forEach { slot ->
                TimeSlotRow(
                    slot = slot,
                    onUpdatePlayerCount = { players -> onUpdatePlayerCount(slot.time, players) },
                    onRemove = { onRemoveTimeSlot(slot.time) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        // Add Time Button
        OutlinedButton(
            onClick = onAddTimeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                TtcIcons.ADD.painter,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(stringResource(TTR.strings.add_time_button.resourceId))
        }
    }
}

/**
 * A row displaying a single time slot with player count slider and remove button.
 *
 * This composable is shared between the Add and Edit tee time screens.
 *
 * @param slot The time slot to display.
 * @param onUpdatePlayerCount Callback when the player count is changed.
 * @param onRemove Callback when the slot is removed.
 */
@Composable
fun TimeSlotRow(
    slot: TeeTimeSlot,
    onUpdatePlayerCount: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Column {
        // Time display with trash icon
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = slot.time.formattedTime,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    TtcIcons.TRASH.painter,
                    contentDescription = stringResource(GR.strings.delete.resourceId),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Player count slider
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(TTR.strings.players_label.resourceId),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
                value = slot.numberOfPlayers.toFloat(),
                onValueChange = { onUpdatePlayerCount(it.toInt()) },
                valueRange = 1f..4f,
                steps = 2,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = slot.numberOfPlayers.toString(),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeeTimesSectionEmptyPreview() {
    MyApplicationTheme {
        TeeTimesSection(
            timeSlots = emptyList(),
            onAddTimeClick = {},
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeeTimesSectionWithSlotsPreview() {
    MyApplicationTheme {
        TeeTimesSection(
            timeSlots = previewTeeTimeSlotList,
            onAddTimeClick = {},
            onUpdatePlayerCount = { _, _ -> },
            onRemoveTimeSlot = {}
        )
    }
}