package net.bradball.teetimecaddie.android.ui.common.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared color, padding and sizing values for [TtcChip], derived from the Fairway Morning
 * "Chips & status" spec. Colors always resolve to semantic Material 3 roles.
 *
 * Note the deliberate divergences from `TtcButtonDefaults`: chips use the *container* tone for a
 * filled [TtcChipColor.Primary] (not the solid `primary`), and [TtcChipColor.Neutral] uses
 * `surfaceContainerHigh` (one step below the button's `surfaceContainerHighest`).
 */
internal object TtcChipDefaults {

    /** Chip corner shape — Fairway Morning `radius-sm` (8dp), a rounded rectangle, not a pill. */
    val Shape: Shape = RoundedCornerShape(8.dp)

    /** Leading icon size. Constant across sizes per the design. */
    val IconSize = 14.dp

    /** Gap between a leading icon and the label. */
    val IconSpacing = 6.dp

    private val MediumContentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    private val SmallContentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

    fun contentPadding(size: TtcChipSize): PaddingValues = when (size) {
        TtcChipSize.Medium -> MediumContentPadding
        TtcChipSize.Small -> SmallContentPadding
    }

    /**
     * Filled ([TtcChip]) container + content colors for [color]. Filled chips use the role's
     * *container* tone; [TtcChipColor.Neutral] uses `surfaceContainerHigh` / `onSurfaceVariant`.
     */
    @Composable
    fun filledColors(color: TtcChipColor): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcChipColor.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
            TtcChipColor.Secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
            TtcChipColor.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
            TtcChipColor.Neutral -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
        }
    }

    /**
     * Outlined ([TtcChipVariant.Outlined]) content + border colors for [color] (transparent container).
     * [TtcChipColor.Neutral] is the design's "suggestion" chip: `onSurfaceVariant` text with an
     * `outlineVariant` border; tinted roles use their role color for both text and border.
     */
    @Composable
    fun outlinedColors(color: TtcChipColor): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcChipColor.Primary -> scheme.primary to scheme.primary
            TtcChipColor.Secondary -> scheme.secondary to scheme.secondary
            TtcChipColor.Tertiary -> scheme.tertiary to scheme.tertiary
            TtcChipColor.Neutral -> scheme.onSurfaceVariant to scheme.outlineVariant
        }
    }
}
