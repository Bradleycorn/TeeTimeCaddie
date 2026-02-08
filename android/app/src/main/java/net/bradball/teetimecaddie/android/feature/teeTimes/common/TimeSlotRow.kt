package net.bradball.teetimecaddie.android.feature.teeTimes.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.core.extensions.formattedTime
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.TeeTimeSlot
import net.bradball.teetimecaddie.features.teetimes.TTR

/**
 * A row displaying a single tee time slot with time, player count slider, and remove button.
 *
 * This composable is shared between the Add and Edit Tee Time screens.
 *
 * @param slot The time slot to display.
 * @param onUpdatePlayerCount Callback when the player count slider changes.
 * @param onRemove Callback when the remove button is clicked.
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
