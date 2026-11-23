package net.bradball.teetimecaddie.android.ui.common.chips

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.ui.common.avatars.TtcAvatar
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A removable chip for a person who has been selected: their avatar, a short [text] such as their
 * first name, and a ✕.
 *
 * Tapping anywhere on the chip calls [onRemove]. Unlike [TtcChip], which labels a status, this
 * chip stands for something the user chose and can take back, like Material's input chip.
 *
 * @param text The label, usually the person's first name.
 * @param avatarInitials The letter shown in the avatar.
 * @param avatarColor The avatar's [TtcColorRole] tone.
 * @param onRemove Called when the chip is tapped.
 * @param removeLabel What a screen reader says for the chip, such as "Remove Sam Tanaka".
 * @param modifier Modifier for the chip.
 */
@Composable
fun TtcInputChip(
    text: String,
    avatarInitials: String,
    avatarColor: TtcColorRole,
    onRemove: () -> Unit,
    removeLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(TtcInputChipDefaults.Height)
            .clip(TtcInputChipDefaults.Shape)
            .background(TtcInputChipDefaults.ContainerColor)
            .clickable(onClick = onRemove)
            .clearAndSetSemantics {
                contentDescription = removeLabel
                role = Role.Button
            }
            .padding(TtcInputChipDefaults.ContentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TtcInputChipDefaults.Spacing),
    ) {
        TtcAvatar(initials = avatarInitials, color = avatarColor, size = TtcInputChipDefaults.AvatarSize)
        Text(
            text = text,
            color = TtcInputChipDefaults.ContentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            painter = TtcIcons.CLOSE.painter,
            contentDescription = null,
            tint = TtcInputChipDefaults.RemoveIconColor,
            modifier = Modifier.size(TtcInputChipDefaults.RemoveIconSize),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcInputChipPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TtcInputChip(
                    text = "Sam",
                    avatarInitials = "S",
                    avatarColor = TtcColorRole.Primary,
                    onRemove = {},
                    removeLabel = "Remove Sam Tanaka",
                )
                TtcInputChip(
                    text = "Dana",
                    avatarInitials = "D",
                    avatarColor = TtcColorRole.Tertiary,
                    onRemove = {},
                    removeLabel = "Remove Dana Park",
                )
            }
        }
    }
}
