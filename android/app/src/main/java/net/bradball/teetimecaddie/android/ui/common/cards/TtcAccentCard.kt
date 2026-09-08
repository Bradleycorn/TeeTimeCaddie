package net.bradball.teetimecaddie.android.ui.common.cards

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.TtcColorRole
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * A card with an accent rail, from the Fairway Morning design system.
 *
 * This is the plain [TtcCard] with a 4dp solid bar down its leading edge — the design's "New invite"
 * treatment. The rail marks a card as needing attention *without* changing the card's fill, so the
 * fill is always the neutral `surfaceContainer`: there is deliberately no `color` parameter, because
 * a tinted fill carries a different meaning entirely and the two are never combined.
 *
 * Use [accent] to choose the signal the rail carries; [TtcColorRole.Primary] (the default) is the
 * design's shipped treatment. [TtcColorRole.Neutral] is the one role with no meaning here — a
 * neutral rail on a neutral fill reads as no rail at all — and falls back to
 * [TtcColorRole.Primary]; see [TtcCardDefaults.accentColor].
 *
 * Everything else — the fill, the 12dp shape and clip, zero elevation, the ripple, full width — comes
 * from [TtcCard], which this wraps. The rail lives inside that clip, so its leading corners are
 * rounded along with the card's.
 *
 * @param modifier Modifier for the card.
 * @param accent The [TtcColorRole] the rail is painted in. Defaults to [TtcColorRole.Primary].
 * @param contentPadding Padding applied inside the card, around [content] and *after* the rail.
 *   Defaults to [TtcCardDefaults.ContentPadding] (16dp).
 * @param onClick When non-null, makes the whole card tappable (rail included) and is invoked on tap.
 * @param content The card's content, laid out in a [Column] beside the rail.
 */
@Composable
fun TtcAccentCard(
    modifier: Modifier = Modifier,
    accent: TtcColorRole = TtcColorRole.Primary,
    contentPadding: PaddingValues = TtcCardDefaults.ContentPadding,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val railColor = TtcCardDefaults.accentColor(accent)

    TtcCard(
        modifier = modifier,
        color = TtcColorRole.Neutral,
        // The card supplies no padding: the rail must reach the card's edges, so `contentPadding`
        // is applied to the content column below instead.
        contentPadding = TtcCardDefaults.NoContentPadding,
        onClick = onClick,
    ) {
        // `IntrinsicSize.Min` + `fillMaxHeight` is the Compose recipe for a full-height rail beside
        // content: it lets the Row measure its height from the content column, which the rail then
        // matches. Using a Row (rather than painting the bar) also puts the rail on the *start*
        // edge, so it flips correctly in RTL.
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(TtcCardDefaults.AccentWidth)
                    .fillMaxHeight()
                    .background(railColor)
            )
            // `weight(1f)` so the content column spans the rest of the card — without it a
            // full-bleed section (a header strip, a divider) would stop at its own content width.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcAccentCardPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcAccentCard { Text("Primary rail (default)") }
                TtcAccentCard(accent = TtcColorRole.Secondary) { Text("Secondary rail") }
                TtcAccentCard(accent = TtcColorRole.Tertiary) { Text("Tertiary rail") }
                TtcAccentCard(accent = TtcColorRole.Error) { Text("Error rail") }
                TtcAccentCard(accent = TtcColorRole.Neutral) { Text("Neutral rail (falls back to Primary)") }
                TtcAccentCard(onClick = {}) { Text("Tappable accent card") }

                // Approximates the design's "New invite" card, to check the rail against
                // multi-line content.
                TtcAccentCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = TtcIcons.CALENDAR.painter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "NEW INVITE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Pebble Beach", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Saturday, Oct 12 · 9:00 AM",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
