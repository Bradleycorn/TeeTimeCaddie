package net.bradball.teetimecaddie.android.ui.common.avatars

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * Shared color, sizing and geometry values for [TtcAvatar], [TtcPlayerStack] and [TtcPhotoPicker],
 * derived from the Fairway Morning "Avatars & players" spec. Colors always resolve to semantic
 * Material 3 roles.
 *
 * An initials avatar is an identity marker, so it takes the **solid** role tone the way a filled
 * button does — with the same exception for [TtcColorRole.Secondary], which falls back to
 * `secondaryContainer` because the gold at full strength cannot carry legible initials.
 * `TtcButtonDefaults.filledColors` makes the same substitution for the same reason; if the gold
 * changes, both need revisiting.
 */
internal object TtcAvatarDefaults {

    /** Named diameters from the design specimens. [size] on the components is a free `Dp`. */
    val SmallSize = 28.dp
    val MediumSize = 40.dp
    val LargeSize = 56.dp

    /** Diameter of the [TtcPhotoPicker] circle. */
    val PickerSize = 88.dp

    /** Horizontal overlap between adjacent avatars in a [TtcPlayerStack]. */
    val StackOverlap = 10.dp

    /** Width of the [TtcPlayerItem] separator ring drawn around each stacked avatar. */
    val StackRing = 2.dp

    /** Stroke width of the guest/empty placeholder ring. */
    val BorderWidth = 1.5.dp

    /** Dashed stroke used for the guest/empty placeholder ring and the picker's empty state. */
    fun dashPathEffect(): PathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 6f), 0f)

    /** Initials text style — semibold, scaled to ~40% of the avatar diameter. */
    fun textStyle(size: Dp): TextStyle =
        TextStyle(fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.4f).sp)

    /** Placeholder glyph size — ~half the avatar diameter. */
    fun iconSize(size: Dp): Dp = size * 0.5f

    /**
     * Background + content colors for an initials [TtcAvatar] in the given [color] role.
     *
     * `Primary`/`Tertiary` use the solid role; `Secondary` uses the container tone (see the class
     * doc). `Error` marks a player with a conflict — a declined invite, a scheduling clash — and
     * `Neutral` an inactive or unrecognized player who still has a name to show. For a player with
     * no name at all, use a [TtcAvatarPlaceholder] instead.
     */
    @Composable
    fun toneColors(color: TtcColorRole): Pair<Color, Color> {
        val scheme = MaterialTheme.colorScheme
        return when (color) {
            TtcColorRole.Primary -> scheme.primary to scheme.onPrimary
            TtcColorRole.Secondary -> scheme.secondaryContainer to scheme.onSecondaryContainer
            TtcColorRole.Tertiary -> scheme.tertiary to scheme.onTertiary
            TtcColorRole.Error -> scheme.errorContainer to scheme.onErrorContainer
            TtcColorRole.Neutral -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        }
    }

    /** The glyph shown inside a [TtcAvatarPlaceholder]. */
    fun placeholderIcon(placeholder: TtcAvatarPlaceholder): TtcIcons = when (placeholder) {
        TtcAvatarPlaceholder.Guest -> TtcIcons.PERSON
        TtcAvatarPlaceholder.Empty -> TtcIcons.ADD
    }
}
