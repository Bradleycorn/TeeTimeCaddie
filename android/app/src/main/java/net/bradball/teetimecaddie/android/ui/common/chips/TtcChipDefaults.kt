package net.bradball.teetimecaddie.android.ui.common.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole

/**
 * Shared color, padding and sizing values for [TtcChip], derived from the Fairway Morning
 * "Chips & status" spec. Colors always resolve to semantic Material 3 roles.
 *
 * Every filled role uses its *container* tone, never the solid one — a chip is a low-emphasis
 * label, so unlike `TtcButtonDefaults` even [TtcColorRole.Primary] and [TtcColorRole.Error] stay
 * soft. [TtcColorRole.Neutral] sits at `surfaceContainerHigh`, one rung below the button's
 * `surfaceContainerHighest`, because a chip is usually rendered on top of a card.
 */
internal object TtcChipDefaults {

    /**
     * Chip corner shape — Fairway Morning `radius-sm`, the theme's [Shapes.small] (8dp). A rounded
     * rectangle, not a pill; the pill belongs to `TtcButtonDefaults.Shape`.
     */
    val Shape: Shape @Composable get() = MaterialTheme.shapes.small

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
     * *container* tone; [TtcColorRole.Neutral] uses `surfaceContainerHigh` / `onSurfaceVariant`.
     */
    @Composable
    fun filledColors(color: TtcColorRole): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcColorRole.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
            TtcColorRole.Secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
            TtcColorRole.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
            TtcColorRole.Error -> scheme.errorContainer to scheme.onErrorContainer
            TtcColorRole.Neutral -> scheme.surfaceContainerHigh to scheme.onSurfaceVariant
        }
    }

    /**
     * Outlined ([TtcChipVariant.Outlined]) content + border colors for [color] (transparent container).
     * [TtcColorRole.Neutral] is the design's "suggestion" chip: `onSurfaceVariant` text with an
     * `outlineVariant` border; tinted roles use their role color for both text and border.
     */
    @Composable
    fun outlinedColors(color: TtcColorRole): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcColorRole.Primary -> scheme.primary to scheme.primary
            TtcColorRole.Secondary -> scheme.secondary to scheme.secondary
            TtcColorRole.Tertiary -> scheme.tertiary to scheme.tertiary
            TtcColorRole.Error -> scheme.error to scheme.error
            TtcColorRole.Neutral -> scheme.onSurfaceVariant to scheme.outlineVariant
        }
    }
}
