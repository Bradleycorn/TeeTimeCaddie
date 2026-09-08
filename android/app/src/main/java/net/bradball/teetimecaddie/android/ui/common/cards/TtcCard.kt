package net.bradball.teetimecaddie.android.ui.common.cards

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A card from the Fairway Morning design system — the base filled container.
 *
 * Renders a 12dp-rounded, clipped, tonal block with **no border and no shadow**: depth comes from
 * the surface ramp, not elevation. [TtcColorRole.Neutral] (the default) is the plain
 * `surfaceContainer` card; the tinted roles carry the container tones the design's inline banners
 * are built from.
 *
 * The card is a *container*, so its content is a [ColumnScope] slot and it is full width by default.
 * [contentPadding] defaults to 16dp for the common case of a plain card; pass
 * [TtcCardDefaults.NoContentPadding] for a "sectioned" card whose children must run edge to edge
 * (a header strip, a full-bleed divider, list rows that supply their own padding):
 *
 * ```
 * TtcCard(contentPadding = TtcCardDefaults.NoContentPadding) {
 *     CardHeader("8:00 AM")
 *     HorizontalDivider()
 *     PlayerRow("Brad")
 * }
 * ```
 *
 * It is a static container by default; pass [onClick] to make the whole card tappable (a game card
 * that navigates to its details, say). There is deliberately no `enabled` parameter — the design has
 * no disabled card state, as with `TtcChip`.
 *
 * @param modifier Modifier for the card.
 * @param color The [TtcColorRole] role. Defaults to [TtcColorRole.Neutral].
 * @param contentPadding Padding applied inside the card, around [content]. Defaults to
 *   [TtcCardDefaults.ContentPadding] (16dp).
 * @param onClick When non-null, makes the whole card tappable and is invoked on tap.
 * @param content The card's content, laid out in a [Column].
 */
@Composable
fun TtcCard(
    modifier: Modifier = Modifier,
    color: TtcColorRole = TtcColorRole.Neutral,
    contentPadding: PaddingValues = TtcCardDefaults.ContentPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = TtcCardDefaults.colors(color)
    val elevation = TtcCardDefaults.elevation()
    // A card is a block-level container, so it fills its parent's width by default (matching the
    // SwiftUI side). `modifier` is applied last so a caller can still constrain the width.
    val cardModifier = Modifier.fillMaxWidth().then(modifier)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = TtcCardDefaults.Shape,
            colors = colors,
            elevation = elevation,
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    } else {
        Card(
            modifier = cardModifier,
            shape = TtcCardDefaults.Shape,
            colors = colors,
            elevation = elevation,
        ) {
            Column(Modifier.padding(contentPadding), content = content)
        }
    }
}

// ─── Preview-only stand-ins ──────────────────────────────────────────────────
// The real CardHeader / ListRow / TtcDivider components come in a later pass; these exist only so
// the sectioned preview below can prove the container composes correctly.

@Composable
private fun PreviewCardHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = TtcIcons.CALENDAR_CLOCK.painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PreviewListRow(primary: String, secondary: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = TtcIcons.PERSON.painter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(primary, style = MaterialTheme.typography.bodyLarge)
            if (secondary != null) {
                Text(
                    secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcCardPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcCard {
                    Text("Filled card", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "surfaceContainer, 12dp radius, no border, no shadow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TtcCard(color = TtcColorRole.Primary) { Text("Primary container") }
                TtcCard(color = TtcColorRole.Secondary) { Text("Secondary container") }
                TtcCard(color = TtcColorRole.Tertiary) { Text("Tertiary container") }
                TtcCard(color = TtcColorRole.Error) { Text("Error container") }
                TtcCard(onClick = {}) { Text("Tappable card") }
            }
        }
    }
}

@Preview(name = "Sectioned - Light", showBackground = true)
@Preview(name = "Sectioned - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcCardSectionedPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // A "list card": no card padding, so the header and dividers run edge to edge.
                TtcCard(contentPadding = TtcCardDefaults.NoContentPadding) {
                    PreviewCardHeader("8:00 AM")
                    PreviewListRow("Brad", "Organizer")
                    HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                    PreviewListRow("Jeff", "Confirmed")
                }

                // The shape an inline banner will take once its content component exists.
                TtcCard(color = TtcColorRole.Error) {
                    Text("We don't recognize that email", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "There's no TeeTimeCaddie account for casey@golf.app.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
