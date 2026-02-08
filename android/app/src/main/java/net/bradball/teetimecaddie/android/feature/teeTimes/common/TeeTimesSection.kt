package net.bradball.teetimecaddie.android.feature.teeTimes.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.features.teetimes.TTR

/**
 * A section displaying a list of tee time slots with an "Add Time" button.
 *
 * This composable is shared between the Add and Edit Tee Time screens.
 *
 * @param timeSlots The list of time slots to display.
 * @param onAddTimeClick Callback when the "Add Time" button is clicked.
 * @param onUpdatePlayerCount Callback when a player count slider changes.
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
