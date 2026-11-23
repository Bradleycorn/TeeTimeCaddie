package net.bradball.teetimecaddie.android.ui.common.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared color, shape and sizing values for [TtcInputChip], derived from the selected-person chips
 * in the Tee Time Caddie design's Add players tray. Colors always resolve to semantic Material 3
 * roles.
 */
internal object TtcInputChipDefaults {

    /** Corner shape — the same `radius-sm` ([androidx.compose.material3.Shapes.small], 8dp) as [TtcChip]. */
    val Shape: Shape @Composable get() = MaterialTheme.shapes.small

    /** Container — `surfaceContainerHighest`, so the chip stands out on the tray's `surfaceContainerHigh`. */
    val ContainerColor: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest

    /** Label — `onSurface`. */
    val ContentColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface

    /** The ✕ — `onSurfaceVariant`, quieter than the label. */
    val RemoveIconColor: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

    /** The design's chip is 32dp tall. */
    val Height = 32.dp

    /** The avatar sits 4dp from the start edge; the ✕ 6dp from the end. */
    val ContentPadding = PaddingValues(start = 4.dp, end = 6.dp)

    /** Gap between the avatar, the label and the ✕. */
    val Spacing = 6.dp

    val AvatarSize = 24.dp

    val RemoveIconSize = 16.dp
}
