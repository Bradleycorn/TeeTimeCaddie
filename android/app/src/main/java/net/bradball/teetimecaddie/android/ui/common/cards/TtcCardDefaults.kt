package net.bradball.teetimecaddie.android.ui.common.cards

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole

/**
 * Shared color, shape, padding and elevation values for [TtcCard] and [TtcAccentCard], derived from
 * the Fairway Morning "Cards & lists" spec. Colors always resolve to semantic Material 3 roles.
 *
 * Like `TtcChipDefaults` (and unlike `TtcButtonDefaults`), every tinted role uses its *container*
 * tone, never the solid role color — that is what the design's inline banners are built from, with
 * [TtcColorRole.Error] the "we don't recognize that email" banner and [TtcColorRole.Secondary] the
 * gold "that email is already in use" warning. The [TtcAccentCard] rail is the exception: see
 * [accentColor].
 *
 * Mirrors the structure of `TtcChipDefaults`.
 */
internal object TtcCardDefaults {

    /** Card corner shape — Fairway Morning `radius-md`, the theme's [Shapes.medium] (12dp). */
    val Shape: Shape @Composable get() = MaterialTheme.shapes.medium

    /** Width of the [TtcAccentCard] rail — Fairway Morning's 4px accent bar. */
    val AccentWidth = 4.dp

    /** Default card content padding — Fairway Morning `space-md` (16dp) on all sides. */
    val ContentPadding = PaddingValues(16.dp)

    /**
     * Zero content padding, for "sectioned" cards whose children must run edge to edge — a header
     * strip, a full-bleed divider, or list rows that supply their own padding. Those sections cannot
     * reach the card's edges if the card itself is inset.
     */
    val NoContentPadding = PaddingValues(0.dp)

    /**
     * Card elevation, pinned to 0dp in **every** state.
     *
     * The Fairway Morning spec is explicit that "shadows are not a default hierarchy device" —
     * depth comes from stepping up the tonal surface ramp instead. Material 3's `Card` defaults to a
     * 1dp shadow, so this must be passed explicitly or the card ships a shadow the design does not
     * have (and that iOS would not match).
     */
    @Composable
    fun elevation(): CardElevation = CardDefaults.cardElevation(
        defaultElevation = 0.dp,
        pressedElevation = 0.dp,
        focusedElevation = 0.dp,
        hoveredElevation = 0.dp,
        draggedElevation = 0.dp,
        disabledElevation = 0.dp,
    )

    /**
     * Container + content colors for [color]. [TtcColorRole.Neutral] is the design's base card
     * (`surfaceContainer` / `onSurface` — note `onSurface`, not `onSurfaceVariant`; muted content
     * inside a card picks the variant itself); every tinted role uses its *container* pair.
     */
    @Composable
    fun colors(color: TtcColorRole): CardColors {
        val scheme = MaterialTheme.colorScheme
        val container: Color
        val content: Color
        when (color) {
            TtcColorRole.Primary -> {
                container = scheme.primaryContainer
                content = scheme.onPrimaryContainer
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
                container = scheme.errorContainer
                content = scheme.onErrorContainer
            }
            TtcColorRole.Neutral -> {
                container = scheme.surfaceContainer
                content = scheme.onSurface
            }
        }
        return CardDefaults.cardColors(containerColor = container, contentColor = content)
    }

    /**
     * The rail color for a [TtcAccentCard] of the given [accent].
     *
     * Unlike [colors], these are the **solid** role tones, not the container tones — the rail is a
     * 4dp signal against the card's neutral fill, so it needs the full-strength color. (Same
     * treatment `TtcAvatarDefaults.toneColors` gives `Primary` and `Tertiary`.) The rail carries no
     * content, so the solid gold is safe here in a way it is not for a filled button or avatar.
     *
     * [TtcColorRole.Neutral] is the one role in the design system with no treatment in its
     * component: a neutral rail on a neutral card fill reads as no rail at all, defeating the point
     * of an accent. It falls back to [TtcColorRole.Primary], the design's shipped treatment (the
     * "New invite" card). Handled as an explicit arm rather than an `else` so that adding a role to
     * [TtcColorRole] still breaks the build here and forces a decision.
     */
    @Composable
    fun accentColor(accent: TtcColorRole): Color {
        val scheme = MaterialTheme.colorScheme
        return when (accent) {
            TtcColorRole.Primary -> scheme.primary
            TtcColorRole.Secondary -> scheme.secondary
            TtcColorRole.Tertiary -> scheme.tertiary
            TtcColorRole.Error -> scheme.error
            TtcColorRole.Neutral -> scheme.primary
        }
    }
}
