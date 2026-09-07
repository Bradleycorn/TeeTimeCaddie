package net.bradball.teetimecaddie.android.ui.common.avatars

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme

/**
 * A circular avatar from the Fairway Morning "Avatars & players" design system.
 *
 * `TtcAvatar` has three flavors, exposed as overloads over one shared circle:
 * - **initials** — a [color]-tinted circle with the player's [initials];
 * - **photo** — a caller-supplied, already-loaded image clipped to the circle (no color role);
 * - **placeholder** — the dashed [TtcAvatarPlaceholder.Guest] / [TtcAvatarPlaceholder.Empty] affordance.
 *
 * [size] is a free [Dp] (see [TtcAvatarDefaults] for the named sizes) so the same view serves the
 * stack (28dp) and the photo picker (88dp) as well as standalone use.
 *
 * @param initials The text to show (usually one or two letters).
 * @param modifier Modifier for the avatar.
 * @param color The [TtcColorRole] the initials disc is tinted with. Defaults to [TtcColorRole.Primary].
 * @param size The avatar diameter. Defaults to [TtcAvatarDefaults.MediumSize].
 */
@Composable
fun TtcAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    color: TtcColorRole = TtcColorRole.Primary,
    size: Dp = TtcAvatarDefaults.MediumSize,
) {
    val (background, content) = TtcAvatarDefaults.toneColors(color)
    AvatarCircle(modifier = modifier, size = size, background = background) {
        Text(
            text = initials,
            color = content,
            style = TtcAvatarDefaults.textStyle(size),
            maxLines = 1,
        )
    }
}

/**
 * A [TtcAvatar] that shows a supplied [photo] (already-loaded [Painter]) clipped to the circle. There
 * is no tone — the image fills the avatar.
 *
 * @param photo The image to display, cropped to fill the circle.
 * @param modifier Modifier for the avatar.
 * @param size The avatar diameter. Defaults to [TtcAvatarDefaults.MediumSize].
 */
@Composable
fun TtcAvatar(
    photo: Painter,
    modifier: Modifier = Modifier,
    size: Dp = TtcAvatarDefaults.MediumSize,
) {
    AvatarCircle(modifier = modifier, size = size, background = Color.Transparent) {
        Image(
            painter = photo,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
    }
}

/**
 * A [TtcAvatar] placeholder affordance — a dashed circle with a person ([TtcAvatarPlaceholder.Guest])
 * or add ([TtcAvatarPlaceholder.Empty]) glyph. Ignores tone.
 *
 * @param placeholder Which placeholder to render.
 * @param modifier Modifier for the avatar.
 * @param size The avatar diameter. Defaults to [TtcAvatarDefaults.MediumSize].
 */
@Composable
fun TtcAvatar(
    placeholder: TtcAvatarPlaceholder,
    modifier: Modifier = Modifier,
    size: Dp = TtcAvatarDefaults.MediumSize,
) {
    val scheme = MaterialTheme.colorScheme
    val border = scheme.outlineVariant
    val dash = TtcAvatarDefaults.dashPathEffect()
    Box(
        modifier = modifier
            .size(size)
            .drawBehind {
                val stroke = TtcAvatarDefaults.BorderWidth.toPx()
                drawCircle(
                    color = border,
                    radius = (this.size.minDimension - stroke) / 2f,
                    style = Stroke(width = stroke, pathEffect = dash),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = TtcAvatarDefaults.placeholderIcon(placeholder).painter,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(TtcAvatarDefaults.iconSize(size)),
        )
    }
}

@Composable
private fun AvatarCircle(
    modifier: Modifier,
    size: Dp,
    background: Color,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcAvatarPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.size(width = 320.dp, height = 260.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp),
                ) {
                    TtcAvatar(initials = "B", color = TtcColorRole.Secondary)
                    TtcAvatar(initials = "M", color = TtcColorRole.Primary)
                    TtcAvatar(initials = "J", color = TtcColorRole.Tertiary)
                    TtcAvatar(initials = "C", color = TtcColorRole.Error)
                    TtcAvatar(initials = "K", color = TtcColorRole.Neutral)
                    TtcAvatar(photo = ColorPainter(MaterialTheme.colorScheme.tertiaryContainer))
                    TtcAvatar(placeholder = TtcAvatarPlaceholder.Guest)
                    TtcAvatar(placeholder = TtcAvatarPlaceholder.Empty)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    TtcAvatar(initials = "D", color = TtcColorRole.Primary, size = TtcAvatarDefaults.SmallSize)
                    TtcAvatar(initials = "D", color = TtcColorRole.Primary, size = TtcAvatarDefaults.MediumSize)
                    TtcAvatar(initials = "D", color = TtcColorRole.Primary, size = TtcAvatarDefaults.LargeSize)
                }
            }
        }
    }
}
