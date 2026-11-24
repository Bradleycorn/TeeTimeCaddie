package net.bradball.teetimecaddie.android.ui.common.chips

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A chip from the Fairway Morning design system, holding any [content].
 *
 * Renders a compact, 8dp-rounded rectangle that lays [content] out in a row, 6dp apart. Its
 * [color] carries meaning: [TtcChipColorToken.Primary] (green, the organizer),
 * [TtcChipColorToken.Secondary] (gold, pending), [TtcChipColorToken.Tertiary] (sky, confirmed),
 * [TtcChipColorToken.Error] (declined / can't play), and the surface tokens for ambient chips, with
 * [TtcChipColorToken.High] the default. [TtcChipVariant.Filled] (the default) fills the chip;
 * [TtcChipVariant.Outlined] is the transparent, bordered "suggestion" treatment. It is static by
 * default; pass [onClick] to make it tappable.
 *
 * For a plain label with an optional icon, use the `text` overload.
 *
 * @param modifier Modifier for the chip.
 * @param color The [TtcChipColorToken]. Defaults to [TtcChipColorToken.High].
 * @param size The [TtcChipSize]. Defaults to [TtcChipSize.Medium].
 * @param variant The [TtcChipVariant] surface treatment. Defaults to [TtcChipVariant.Filled].
 * @param onClick When non-null, makes the chip tappable and is invoked on tap.
 * @param content The chip's content. It receives the content color for [color] and [variant], to
 * tint text and icons with.
 */
@Composable
fun TtcChip(
    modifier: Modifier = Modifier,
    color: TtcChipColorToken = TtcChipColorToken.High,
    size: TtcChipSize = TtcChipSize.Medium,
    variant: TtcChipVariant = TtcChipVariant.Filled,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.(contentColor: Color) -> Unit
) {
    val (contentColor, decoration) = when (variant) {
        TtcChipVariant.Filled -> {
            val (container, filledContentColor) = TtcChipDefaults.filledColors(color)
            filledContentColor to Modifier.background(container)
        }
        TtcChipVariant.Outlined -> {
            val (outlinedContentColor, border) = TtcChipDefaults.outlinedColors(color)
            outlinedContentColor to Modifier.border(1.dp, border, TtcChipDefaults.Shape)
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
        horizontalArrangement = Arrangement.spacedBy(TtcChipDefaults.Spacing)
    ) {
        content(contentColor)
    }
}

/**
 * A Fairway Morning chip with a [text] label and an optional leading [icon]: the status and
 * identity badge (e.g. "Confirmed", "Organizer", "3/4 filled") or an outlined "suggestion".
 *
 * @param text The chip label.
 * @param modifier Modifier for the chip.
 * @param color The [TtcChipColorToken]. Defaults to [TtcChipColorToken.High].
 * @param size The [TtcChipSize]. Defaults to [TtcChipSize.Medium].
 * @param variant The [TtcChipVariant] surface treatment. Defaults to [TtcChipVariant.Filled].
 * @param icon Optional leading [TtcIcons] icon.
 * @param onClick When non-null, makes the chip tappable and is invoked on tap.
 */
@Composable
fun TtcChip(
    text: String,
    modifier: Modifier = Modifier,
    color: TtcChipColorToken = TtcChipColorToken.High,
    size: TtcChipSize = TtcChipSize.Medium,
    variant: TtcChipVariant = TtcChipVariant.Filled,
    icon: TtcIcons? = null,
    onClick: (() -> Unit)? = null,
) {
    TtcChip(modifier = modifier, color = color, size = size, variant = variant, onClick = onClick) { contentColor ->
        if (icon != null) {
            Icon(
                painter = icon.painter,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(TtcChipDefaults.IconSize),
            )
        }
        Text(text, color = contentColor, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                    TtcChip(text = "Confirmed", color = TtcChipColorToken.Tertiary, icon = TtcIcons.ADD)
                    TtcChip(text = "Pending", color = TtcChipColorToken.Secondary, icon = TtcIcons.ADD)
                    TtcChip(text = "Organizer", color = TtcChipColorToken.Primary, icon = TtcIcons.ADD)
                    TtcChip(text = "Declined", color = TtcChipColorToken.Error, icon = TtcIcons.ADD)
                    TtcChip(text = "3/4 filled")
                    TtcChip(text = "Surface", color = TtcChipColorToken.Surface)
                    TtcChip(text = "Highest", color = TtcChipColorToken.Highest)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Confirmed", color = TtcChipColorToken.Tertiary, size = TtcChipSize.Small)
                    TtcChip(text = "Pending", color = TtcChipColorToken.Secondary, size = TtcChipSize.Small)
                    TtcChip(text = "1/4 filled", size = TtcChipSize.Small)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Pebble Beach", variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Bethpage Black", variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Torrey Pines", variant = TtcChipVariant.Outlined)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TtcChip(text = "Primary", color = TtcChipColorToken.Primary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Secondary", color = TtcChipColorToken.Secondary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Tertiary", color = TtcChipColorToken.Tertiary, variant = TtcChipVariant.Outlined)
                    TtcChip(text = "Declined", color = TtcChipColorToken.Error, variant = TtcChipVariant.Outlined)
                }
                TtcChip(text = "Tappable", color = TtcChipColorToken.Primary, onClick = {})
            }
        }
    }
}
