package net.bradball.teetimecaddie.android.ui.common.feedback

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * An empty state from the Fairway Morning "Feedback & overlays" design system.
 *
 * A centered column of up to four parts: an optional [icon] in a 104dp `primaryContainer`
 * "medallion", the [title], an optional [description], and an optional [extraContent] slot for the
 * action that gets the user out of the empty state.
 *
 * `TtcEmptyState` renders only the **content column**, at its intrinsic height. Filling and
 * centering the available space is the caller's job, because that is also where clearance for
 * screen chrome (a FAB, a tab bar) belongs:
 *
 * ```
 * Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
 *     TtcEmptyState(
 *         title = stringResource(TTR.strings.empty_tee_times_title.resourceId),
 *         icon = TtcIcons.TEE,
 *         description = stringResource(TTR.strings.empty_tee_times_message.resourceId),
 *     ) {
 *         TtcButton("Add a tee time", onClick = onAdd, icon = TtcIcons.ADD)
 *     }
 * }
 * ```
 *
 * The medallion has a single treatment in the design, so there is no color role parameter — see
 * [TtcEmptyStateDefaults].
 *
 * @param title The headline. The only required part, and the element that carries the state's
 *   meaning for screen readers (the medallion glyph is decorative).
 * @param modifier Modifier for the column.
 * @param icon Optional [TtcIcons] glyph, shown tinted inside the medallion.
 * @param description Optional supporting copy, capped to a readable measure.
 * @param extraContent Optional trailing content — usually the recovery action — laid out in a
 *   [Column] below the text. Omit it entirely (rather than passing an empty block) so the gap
 *   above it collapses.
 */
@Composable
fun TtcEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    icon: TtcIcons? = null,
    description: String? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val scheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.padding(horizontal = TtcEmptyStateDefaults.HorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TtcEmptyStateDefaults.Spacing),
    ) {
        if (icon != null) {
            Box(
                // The extra gap is applied outside the circle, so it doesn't eat into the fill.
                modifier = Modifier
                    .padding(bottom = TtcEmptyStateDefaults.MedallionBottomGap)
                    .size(TtcEmptyStateDefaults.MedallionSize)
                    .clip(CircleShape)
                    .background(scheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon.painter,
                    contentDescription = null,
                    tint = scheme.onPrimaryContainer,
                    modifier = Modifier.size(TtcEmptyStateDefaults.IconSize),
                )
            }
        }

        Text(
            text = title,
            style = TtcEmptyStateDefaults.titleStyle(),
            color = scheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Text(
                text = description,
                style = TtcEmptyStateDefaults.descriptionStyle(),
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = TtcEmptyStateDefaults.DescriptionMaxWidth),
            )
        }

        if (extraContent != null) {
            Column(
                modifier = Modifier.padding(top = TtcEmptyStateDefaults.ExtraContentTopGap),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TtcEmptyStateDefaults.ExtraContentSpacing),
                content = extraContent,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcEmptyStatePreview() {
    MyApplicationTheme {
        Surface {
            // The design's composition: the empty state centered in the whole content area.
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TtcEmptyState(
                    title = "No games yet",
                    icon = TtcIcons.TEE,
                    description = "Booked a tee time? Turn it into a game so your foursome lives " +
                        "somewhere other than a text thread.",
                ) {
                    TtcButton("Create a game", onClick = {}, icon = TtcIcons.ADD)
                }
            }
        }
    }
}

@Preview(name = "Variants - Light", showBackground = true)
@Preview(name = "Variants - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcEmptyStateVariantsPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                // Text only — the gaps left by the medallion and the action collapse.
                TtcEmptyState(
                    title = "Tee sheet is wide open",
                    description = "You don't have any scheduled tee times.",
                )
                // Medallion + title, no supporting copy.
                TtcEmptyState(
                    title = "No players yet",
                    icon = TtcIcons.PERSON,
                )
            }
        }
    }
}
