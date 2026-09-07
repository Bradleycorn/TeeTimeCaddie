package net.bradball.teetimecaddie.android.ui.common.avatars

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * The Fairway Morning profile photo picker — a large circular affordance for seeding a profile photo.
 *
 * It is **presentational**: it renders the current state and fires [onClick] when tapped; launching the
 * OS photo picker and handling the chosen image is the caller's job. When a [photo] or [initials] is
 * supplied it reuses [TtcAvatar] for the disc and shows an *edit* badge; otherwise it shows the empty
 * "add a photo" state with an *add* badge.
 *
 * @param onClick Invoked when the picker is tapped.
 * @param modifier Modifier for the picker.
 * @param initials Player initials to show when there's no [photo]. `null`/blank ⇒ empty state.
 * @param color The [TtcColorRole] for the initials disc. Defaults to [TtcColorRole.Primary].
 * @param photo An already-loaded profile image; when non-null it fills the disc.
 * @param size The picker diameter. Defaults to [TtcAvatarDefaults.PickerSize] (88dp).
 */
@Composable
fun TtcPhotoPicker(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initials: String? = null,
    color: TtcColorRole = TtcColorRole.Primary,
    photo: Painter? = null,
    size: Dp = TtcAvatarDefaults.PickerSize,
) {
    val hasContent = photo != null || !initials.isNullOrBlank()
    Box(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        when {
            photo != null -> TtcAvatar(photo = photo, size = size)
            !initials.isNullOrBlank() -> TtcAvatar(initials = initials, color = color, size = size)
            else -> EmptyDisc(size = size)
        }
        Badge(
            icon = if (hasContent) TtcIcons.EDIT else TtcIcons.ADD,
            size = size,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
}

@Composable
private fun EmptyDisc(size: Dp) {
    val scheme = MaterialTheme.colorScheme
    val border = scheme.outline
    val dash = TtcAvatarDefaults.dashPathEffect()
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(scheme.surfaceContainerHigh)
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
            painter = TtcIcons.ADD_A_PHOTO.painter,
            contentDescription = null,
            tint = scheme.onSurfaceVariant,
            modifier = Modifier.size(size * 0.3f),
        )
    }
}

@Composable
private fun Badge(icon: TtcIcons, size: Dp, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val badgeSize = size * 0.32f
    Box(
        modifier = modifier
            .background(scheme.surface, CircleShape)
            .padding(3.dp),
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(CircleShape)
                .background(scheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = icon.painter,
                contentDescription = null,
                tint = scheme.onPrimary,
                modifier = Modifier.size(badgeSize * 0.6f),
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcPhotoPickerPreview() {
    MyApplicationTheme {
        Surface {
            Row(
                modifier = Modifier.padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TtcPhotoPicker(onClick = {})
                TtcPhotoPicker(onClick = {}, initials = "B", color = TtcColorRole.Secondary)
            }
        }
    }
}
