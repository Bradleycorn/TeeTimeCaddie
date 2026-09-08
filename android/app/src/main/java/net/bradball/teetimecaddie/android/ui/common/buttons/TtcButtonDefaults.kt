package net.bradball.teetimecaddie.android.ui.common.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole

/**
 * Shared color, padding and sizing values for [TtcButton] and [TtcOutlinedButton], derived from the
 * Fairway Morning "Buttons & actions" spec. Colors always resolve to semantic Material 3 roles.
 *
 * Buttons are the highest-emphasis component, and this is the only place a [TtcColorRole] resolves
 * to a **solid** container tone for a fill: [TtcColorRole.Primary] is the green CTA and
 * [TtcColorRole.Error] the destructive one. The remaining roles use the softer container tones that
 * `TtcChipDefaults` and `TtcCardDefaults` use throughout — the design's "tonal" button.
 *
 * Note [TtcColorRole.Secondary] is a container fill rather than a solid one even though it sits in
 * the high-emphasis group. The gold at full strength cannot carry legible content on top of it, so
 * a solid `secondary` is never paired with a foreground anywhere in the app.
 * `TtcAvatarDefaults.toneColors` makes the same substitution for the same reason.
 */
internal object TtcButtonDefaults {

    /**
     * Button shape — the Fairway Morning pill.
     *
     * Deliberately **not** a `MaterialTheme.shapes` token: a pill is fully round at any height, so
     * [CircleShape] is the value itself rather than a radius on the app's scale (see `theme/Shape.kt`,
     * which has no pill slot). Because it reads nothing from the theme it stays a plain `val`, unlike
     * the `Shape` on `TtcCardDefaults` / `TtcChipDefaults` / `TtcTextFieldDefaults`.
     *
     * The iOS twin is `TtcButtonStyle.shape`, which is `.capsule` for the same reason.
     */
    val Shape: Shape = CircleShape

    /** Default (comfortable) content padding — Fairway Morning pill button: 18dp × 10dp. */
    val ContentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)

    /** Dense content padding — Fairway Morning dense pill button: 14dp × 8dp. */
    val DenseContentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)

    /** Leading icon size for pill buttons. */
    val IconSize = 18.dp

    /** Gap between a leading icon and the label. */
    val IconSpacing = 8.dp

    fun contentPadding(dense: Boolean): PaddingValues =
        if (dense) DenseContentPadding else ContentPadding

    /**
     * Filled ([TtcButton]) container/content colors for [color]. Disabled state uses the design's
     * neutral treatment (`surfaceContainerHighest` / `onSurfaceVariant`) regardless of [color].
     */
    @Composable
    fun filledColors(color: TtcColorRole): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        val container: Color
        val content: Color
        when (color) {
            TtcColorRole.Primary -> {
                container = scheme.primary
                content = scheme.onPrimary
            }
            TtcColorRole.Secondary -> {
                container = scheme.secondaryContainer
                content = scheme.onSecondaryContainer
            }
            TtcColorRole.Tertiary -> {
                container = scheme.tertiaryContainer
                content = scheme.onTertiaryContainer
            }
            TtcColorRole.Error -> {
                container = scheme.error
                content = scheme.onError
            }
            TtcColorRole.Neutral -> {
                container = scheme.surfaceContainerHighest
                content = scheme.onSurfaceVariant
            }
        }
        return ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = scheme.surfaceContainerHighest,
            disabledContentColor = scheme.onSurfaceVariant,
        )
    }

    /** The text/content color for an outlined button in the given [color] role. */
    @Composable
    fun outlinedContentColor(color: TtcColorRole): Color {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcColorRole.Primary -> scheme.primary
            TtcColorRole.Secondary -> scheme.secondary
            TtcColorRole.Tertiary -> scheme.tertiary
            TtcColorRole.Error -> scheme.error
            TtcColorRole.Neutral -> scheme.onSurface
        }
    }

    /** Outlined ([TtcOutlinedButton]) content colors for [color] (transparent container). */
    @Composable
    fun outlinedColors(color: TtcColorRole): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        return ButtonDefaults.outlinedButtonColors(
            contentColor = outlinedContentColor(color),
            disabledContentColor = scheme.onSurfaceVariant,
        )
    }

    /**
     * The 1dp border for an outlined button. Uses `outline` for the [TtcColorRole.Neutral] default,
     * the role color for tinted variants, and `outlineVariant` when disabled.
     */
    @Composable
    fun outlinedBorder(color: TtcColorRole, enabled: Boolean): BorderStroke {
        val scheme = MaterialTheme.colorScheme
        val roleBorder = when (color) {
            TtcColorRole.Primary,
            TtcColorRole.Secondary,
            TtcColorRole.Tertiary,
            TtcColorRole.Error -> outlinedContentColor(color)
            // Distinct from the neutral *text* color (`onSurface`) — the design's default outlined
            // button pairs strong text with a subtle border.
            TtcColorRole.Neutral -> scheme.outline
        }
        return BorderStroke(1.dp, if (enabled) roleBorder else scheme.outlineVariant)
    }
}
