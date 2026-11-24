package net.bradball.teetimecaddie.android.ui.common.chips

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shared color, padding and sizing values for [TtcChip], derived from the Fairway Morning
 * "Chips & status" spec. Colors always resolve to semantic Material 3 roles.
 *
 * Every filled tinted token uses its role's *container* tone, never the solid one — a chip is a
 * low-emphasis label, so unlike `TtcButtonDefaults` even [TtcChipColorToken.Primary] and
 * [TtcChipColorToken.Error] stay soft.
 */
internal object TtcChipDefaults {

    /**
     * Chip corner shape — Fairway Morning `radius-sm`, the theme's [Shapes.small] (8dp). A rounded
     * rectangle, not a pill; the pill belongs to `TtcButtonDefaults.Shape`.
     */
    val Shape: Shape @Composable get() = MaterialTheme.shapes.small

    /** Leading icon size. Constant across sizes per the design. */
    val IconSize = 14.dp

    /** Gap between elements in the chip. */
    val Spacing = 6.dp

    private val MediumContentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    private val SmallContentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

    fun contentPadding(size: TtcChipSize): PaddingValues = when (size) {
        TtcChipSize.Medium -> MediumContentPadding
        TtcChipSize.Small -> SmallContentPadding
    }

    /**
     * Filled ([TtcChipVariant.Filled]) container + content colors for [color]. Tinted tokens use
     * their role's *container* tone; surface tokens use their `surfaceContainer*` rung with
     * `onSurface` content.
     */
    @Composable
    fun filledColors(color: TtcChipColorToken): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcChipColorToken.Primary -> scheme.primaryContainer to scheme.onPrimaryContainer
            TtcChipColorToken.Secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
            TtcChipColorToken.Tertiary -> scheme.tertiaryContainer to scheme.onTertiaryContainer
            TtcChipColorToken.Error -> scheme.errorContainer to scheme.onErrorContainer
            TtcChipColorToken.Surface -> scheme.surfaceContainer to scheme.onSurface
            TtcChipColorToken.High -> scheme.surfaceContainerHigh to scheme.onSurface
            TtcChipColorToken.Highest -> scheme.surfaceContainerHighest to scheme.onSurface
            TtcChipColorToken.Low -> scheme.surfaceContainerLow to scheme.onSurface
            TtcChipColorToken.Lowest -> scheme.surfaceContainerLowest to scheme.onSurface
        }
    }

    /**
     * Outlined ([TtcChipVariant.Outlined]) content + border colors for [color] (transparent
     * container). Tinted tokens use their role color for both text and border; every surface token
     * is the design's "suggestion" chip: `onSurfaceVariant` text with an `outlineVariant` border.
     */
    @Composable
    fun outlinedColors(color: TtcChipColorToken): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcChipColorToken.Primary -> scheme.primary to scheme.primary
            TtcChipColorToken.Secondary -> scheme.secondary to scheme.secondary
            TtcChipColorToken.Tertiary -> scheme.tertiary to scheme.tertiary
            TtcChipColorToken.Error -> scheme.error to scheme.error
            // An outlined chip has no fill to step, so every surface token looks the same.
            TtcChipColorToken.Surface,
            TtcChipColorToken.High,
            TtcChipColorToken.Highest,
            TtcChipColorToken.Low,
            TtcChipColorToken.Lowest -> scheme.onSurfaceVariant to scheme.outlineVariant
        }
    }
}
