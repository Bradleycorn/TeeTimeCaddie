package net.bradball.teetimecaddie.android.ui.common.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * The color role a [TtcButton] or [TtcOutlinedButton] renders with.
 *
 * For a filled [TtcButton], [Primary] is the solid green CTA while [Secondary] and [Tertiary]
 * produce the "tonal" look (a container fill). For a [TtcOutlinedButton], [Neutral] is the default
 * design treatment (on-surface text, outline border) and the other roles tint the text and border.
 *
 * Maps to the Fairway Morning design system roles via the app's Material 3 [MaterialTheme].
 */
enum class TtcButtonColor {
    Primary,
    Secondary,
    Tertiary,
    Neutral
}

/**
 * Shared color, padding and sizing values for [TtcButton] and [TtcOutlinedButton], derived from the
 * Fairway Morning "Buttons & actions" spec. Colors always resolve to semantic Material 3 roles.
 */
internal object TtcButtonDefaults {

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
    fun filledColors(color: TtcButtonColor): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        val container: Color
        val content: Color
        when (color) {
            TtcButtonColor.Primary -> {
                container = scheme.primary
                content = scheme.onPrimary
            }
            TtcButtonColor.Secondary -> {
                container = scheme.secondaryContainer
                content = scheme.onSecondaryContainer
            }
            TtcButtonColor.Tertiary -> {
                container = scheme.tertiaryContainer
                content = scheme.onTertiaryContainer
            }
            TtcButtonColor.Neutral -> {
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
    fun outlinedContentColor(color: TtcButtonColor): Color {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcButtonColor.Neutral -> scheme.onSurface
            TtcButtonColor.Primary -> scheme.primary
            TtcButtonColor.Secondary -> scheme.secondary
            TtcButtonColor.Tertiary -> scheme.tertiary
        }
    }

    /** Outlined ([TtcOutlinedButton]) content colors for [color] (transparent container). */
    @Composable
    fun outlinedColors(color: TtcButtonColor): ButtonColors {
        val scheme = MaterialTheme.colorScheme
        return ButtonDefaults.outlinedButtonColors(
            contentColor = outlinedContentColor(color),
            disabledContentColor = scheme.onSurfaceVariant,
        )
    }

    /**
     * The 1dp border for an outlined button. Uses `outline` for the [TtcButtonColor.Neutral]
     * default, the role color for tinted variants, and `outlineVariant` when disabled.
     */
    @Composable
    fun outlinedBorder(color: TtcButtonColor, enabled: Boolean): BorderStroke {
        val scheme = MaterialTheme.colorScheme
        val borderColor = when {
            !enabled -> scheme.outlineVariant
            color == TtcButtonColor.Neutral -> scheme.outline
            else -> outlinedContentColor(color)
        }
        return BorderStroke(1.dp, borderColor)
    }
}
