package net.bradball.teetimecaddie.android.ui.common.buttons

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.LoadingIndicator
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons

/**
 * An outlined pill button from the Fairway Morning design system.
 *
 * By default ([TtcButtonColor.Neutral]) it renders the design's neutral treatment: on-surface text with
 * an `outline` border. [TtcButtonColor.Primary], [TtcButtonColor.Secondary] and [TtcButtonColor.Tertiary]
 * tint the text and border to that role. Use [icon] for an optional leading icon, [dense] for tighter
 * padding, and [isLoading] to show an animated indicator in place of the label (clicks are ignored while
 * loading). For a full-width CTA, pass `Modifier.fillMaxWidth()`.
 *
 * @param text The button label.
 * @param onClick Invoked when the button is tapped (ignored while [isLoading]).
 * @param modifier Modifier for the button.
 * @param color The [TtcButtonColor] role. Defaults to [TtcButtonColor.Neutral].
 * @param icon Optional leading [TtcIcons] icon.
 * @param dense When true, uses tighter content padding.
 * @param enabled Whether the button is enabled.
 * @param isLoading When true, shows a loading indicator in place of the label and ignores clicks.
 */
@Composable
fun TtcOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: TtcButtonColor = TtcButtonColor.Neutral,
    icon: TtcIcons? = null,
    dense: Boolean = false,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    OutlinedButton(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        colors = TtcButtonDefaults.outlinedColors(color),
        border = TtcButtonDefaults.outlinedBorder(color, enabled),
        contentPadding = TtcButtonDefaults.contentPadding(dense),
    ) {
        LoadingIndicator(isLoading = isLoading) {
            if (icon != null) {
                Icon(
                    painter = icon.painter,
                    contentDescription = null,
                    modifier = Modifier.size(TtcButtonDefaults.IconSize),
                )
                Spacer(Modifier.width(TtcButtonDefaults.IconSpacing))
            }
            Text(text)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcOutlinedButtonPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcOutlinedButton(text = "Invite more", onClick = {})
                TtcOutlinedButton(text = "Invite", onClick = {}, icon = TtcIcons.ADD)
                TtcOutlinedButton(text = "Primary", onClick = {}, color = TtcButtonColor.Primary)
                TtcOutlinedButton(text = "Create account", onClick = {}, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(name = "States - Light", showBackground = true)
@Preview(name = "States - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcOutlinedButtonStatesPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcOutlinedButton(text = "Enabled", onClick = {})
                TtcOutlinedButton(text = "Disabled", onClick = {}, enabled = false)
                TtcOutlinedButton(text = "Loading", onClick = {}, isLoading = true)
            }
        }
    }
}
