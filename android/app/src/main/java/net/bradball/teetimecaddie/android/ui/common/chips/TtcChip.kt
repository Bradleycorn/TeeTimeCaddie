package net.bradball.teetimecaddie.android.ui.common.chips

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A chip from the Fairway Morning design system.
 *
 * Renders a compact, 8dp-rounded pill whose *color carries meaning*: [TtcColorRole.Primary] (green,
 * the organizer), [TtcColorRole.Secondary] (gold, pending), [TtcColorRole.Tertiary] (sky, confirmed),
 * [TtcColorRole.Error] (declined / can't play) and the default [TtcColorRole.Neutral] (ambient).
 * [TtcChipVariant.Filled] (the default) is the container-filled status/identity badge (e.g.
 * "Confirmed", "Organizer", "3/4 filled"); [TtcChipVariant.Outlined] is the transparent, bordered
 * "suggestion" treatment. It is a static badge by default; pass [onClick] to make it tappable.
 *
 * @param text The chip label.
 * @param modifier Modifier for the chip.
 * @param color The [TtcColorRole] role. Defaults to [TtcColorRole.Neutral].
 * @param size The [TtcChipSize]. Defaults to [TtcChipSize.Medium].
 * @param variant The [TtcChipVariant] surface treatment. Defaults to [TtcChipVariant.Filled].
 * @param icon Optional leading [TtcIcons] icon.
 * @param onClick When non-null, makes the chip tappable and is invoked on tap.
 */
@Composable
fun TtcChip(
    text: String,
    modifier: Modifier = Modifier,
    color: TtcColorRole = TtcColorRole.Neutral,
    size: TtcChipSize = TtcChipSize.Medium,
    variant: TtcChipVariant = TtcChipVariant.Filled,
    icon: TtcIcons? = null,
    onClick: (() -> Unit)? = null,
) {
    val (content, decoration) = when (variant) {
        TtcChipVariant.Filled -> {
            val (container, contentColor) = TtcChipDefaults.filledColors(color)
            contentColor to Modifier.background(container)
        }
        TtcChipVariant.Outlined -> {
            val (contentColor, border) = TtcChipDefaults.outlinedColors(color)
            contentColor to Modifier.border(1.dp, border, TtcChipDefaults.Shape)
        }
    }
    val clickable = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = modifier
            .clip(TtcChipDefaults.Shape)
            .then(decoration)
            .then(clickable)
            .padding(TtcChipDefaults.contentPadding(size)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                painter = icon.painter,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(TtcChipDefaults.IconSize),
            )
            Spacer(Modifier.width(TtcChipDefaults.IconSpacing))
        }
        Text(text, color = content, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcChipPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Confirmed", color = TtcColorRole.Tertiary, icon = TtcIcons.ADD)
                    TtcChip(text = "Pending", color = TtcColorRole.Secondary, icon = TtcIcons.ADD)
                    TtcChip(text = "Organizer", color = TtcColorRole.Primary, icon = TtcIcons.ADD)
                    TtcChip(text = "Declined", color = TtcColorRole.Error, icon = TtcIcons.ADD)
                    TtcChip(text = "3/4 filled")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Confirmed", color = TtcColorRole.Tertiary, size = TtcChipSize.Small)
                    TtcChip(text = "Pending", color = TtcColorRole.Secondary, size = TtcChipSize.Small)
                    TtcChip(text = "1/4 filled", size = TtcChipSize.Small)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Pebble Beach", variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Bethpage Black", variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Torrey Pines", variant = TtcChipVariant.Outlined)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Primary", color = TtcColorRole.Primary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Secondary", color = TtcColorRole.Secondary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Tertiary", color = TtcColorRole.Tertiary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Declined", color = TtcColorRole.Error, variant = TtcChipVariant.Outlined)
                }
                TtcChip(text = "Tappable", color = TtcColorRole.Primary, onClick = {})
            }
        }
    }
}
