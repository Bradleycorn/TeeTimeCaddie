package net.bradball.teetimecaddie.android.ui.common.feedback

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared sizing, spacing and type values for [TtcEmptyState], derived from the Fairway Morning
 * "Feedback & overlays" spec. Colors always resolve to semantic Material 3 roles.
 *
 * The medallion is the one place the empty state uses color, and the design ships exactly one
 * treatment for it — the `primaryContainer` / `onPrimaryContainer` pair. [TtcEmptyState] therefore
 * takes no `TtcColorRole` parameter at all: the roles are pinned here because there is nothing for a
 * caller to choose between.
 *
 * Mirrors the structure of `TtcCardDefaults` / `TtcAvatarDefaults`.
 */
internal object TtcEmptyStateDefaults {

    /** Diameter of the icon medallion — the design's 104px circle. */
    val MedallionSize = 104.dp

    /** Glyph size inside the medallion — the design's 52px icon. */
    val IconSize = 52.dp

    /** Vertical gap between the empty state's stacked elements — Fairway Morning `space-md`. */
    val Spacing = 16.dp

    /**
     * Extra gap below the medallion, on top of [Spacing]. The design gives the medallion its own
     * `margin-bottom: 4`, letting the art breathe a little more than the text block does.
     */
    val MedallionBottomGap = 4.dp

    /**
     * Horizontal inset around the whole block. The design's container is `padding: 24px 32px 96px`;
     * only the 32px side inset belongs to the component — the vertical padding (and especially the
     * 96px bottom, which clears the Games screen's FAB and tab bar) is the caller's screen chrome.
     */
    val HorizontalPadding = 32.dp

    /** Measure cap on the description, so the copy wraps for readability rather than for the screen. */
    val DescriptionMaxWidth = 280.dp

    /** Extra gap above `extraContent`, on top of [Spacing] — the design's `margin-top: 8`. */
    val ExtraContentTopGap = 8.dp

    /** Gap between multiple children supplied to `extraContent` (the design specimen has one). */
    val ExtraContentSpacing = 8.dp

    /**
     * Title style — the design's `headline-small` at weight 600.
     *
     * The app theme's `headlineSmall` is 24sp/Normal, so the semibold weight is overridden here, in
     * one place, rather than at the call site.
     */
    @Composable
    fun titleStyle(): TextStyle =
        MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)

    /** Description style — `body-medium` with the design's `line-height: 1.5` (1.5 x 14sp). */
    @Composable
    fun descriptionStyle(): TextStyle =
        MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp)
}
