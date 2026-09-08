package net.bradball.teetimecaddie.android.ui.common.avatars

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme

/**
 * Wraps a single [TtcAvatar] in the Fairway Morning stack "separator ring" — a [ringColor] circle 2dp
 * larger than the avatar, so overlapping avatars in a [TtcPlayerStack] read as distinct discs. Use it
 * like `IconButton { Icon(...) }`:
 *
 * ```
 * TtcPlayerStack {
 *     TtcPlayerItem { TtcAvatar(initials = "B", color = TtcColorRole.Secondary, size = 28.dp) }
 *     TtcPlayerItem { TtcAvatar(initials = "J", color = TtcColorRole.Tertiary, size = 28.dp) }
 * }
 * ```
 *
 * @param modifier Modifier for the item.
 * @param ringColor The separator ring color — the surface the stack sits on. Defaults to
 *   `surfaceContainer` (the card fill the stack usually lives on).
 * @param content The avatar to wrap.
 */
@Composable
fun TtcPlayerItem(
    modifier: Modifier = Modifier,
    ringColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .background(ringColor, CircleShape)
            .padding(TtcAvatarDefaults.StackRing),
    ) {
        content()
    }
}

/**
 * A horizontally overlapping row of [TtcPlayerItem]s — the Fairway Morning "player stack". It is a
 * thin [Row] with negative spacing, so its content block reads identically to the SwiftUI version and
 * children get full [RowScope] (e.g. `Modifier.align`). Overflow (`+N`) and an add slot are not part
 * of this component.
 *
 * @param modifier Modifier for the stack.
 * @param overlap How much each avatar overlaps the previous one. Defaults to
 *   [TtcAvatarDefaults.StackOverlap].
 * @param content The [TtcPlayerItem]s to lay out.
 */
@Composable
fun TtcPlayerStack(
    modifier: Modifier = Modifier,
    overlap: Dp = TtcAvatarDefaults.StackOverlap,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(-overlap),
        content = content,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcPlayerStackPreview() {
    MyApplicationTheme {
        Surface {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TtcPlayerStack {
                    TtcPlayerItem { TtcAvatar(initials = "B", color = TtcColorRole.Secondary, size = TtcAvatarDefaults.SmallSize) }
                    TtcPlayerItem { TtcAvatar(initials = "J", color = TtcColorRole.Tertiary, size = TtcAvatarDefaults.SmallSize) }
                    TtcPlayerItem { TtcAvatar(initials = "M", color = TtcColorRole.Primary, size = TtcAvatarDefaults.SmallSize) }
                }
                TtcPlayerStack {
                    TtcPlayerItem { TtcAvatar(initials = "B", color = TtcColorRole.Secondary, size = TtcAvatarDefaults.SmallSize) }
                    TtcPlayerItem { TtcAvatar(placeholder = TtcAvatarPlaceholder.Guest, size = TtcAvatarDefaults.SmallSize) }
                }
            }
        }
    }
}
