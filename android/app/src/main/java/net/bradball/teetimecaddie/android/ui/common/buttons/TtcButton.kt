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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
 * A filled pill button from the Fairway Morning design system.
 *
 * [TtcButtonColor.Primary] (the default) renders the solid primary "green" CTA. [TtcButtonColor.Secondary]
 * and [TtcButtonColor.Tertiary] render the "tonal" look (a container fill). Use [icon] for an optional
 * leading icon, [dense] for tighter padding, and [isLoading] to show an animated indicator in place of
 * the label (clicks are ignored while loading). For a full-width CTA, pass `Modifier.fillMaxWidth()`.
 *
 * @param text The button label.
 * @param onClick Invoked when the button is tapped (ignored while [isLoading]).
 * @param modifier Modifier for the button.
 * @param color The [TtcButtonColor] role. Defaults to [TtcButtonColor.Primary].
 * @param icon Optional leading [TtcIcons] icon.
 * @param dense When true, uses tighter content padding.
 * @param enabled Whether the button is enabled.
 * @param isLoading When true, shows a loading indicator in place of the label and ignores clicks.
 */
@Composable
fun TtcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: TtcButtonColor = TtcButtonColor.Primary,
    icon: TtcIcons? = null,
    dense: Boolean = false,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        modifier = modifier,
        enabled = enabled,
        shape = CircleShape,
        colors = TtcButtonDefaults.filledColors(color),
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
private fun TtcButtonPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcButton(text = "Book", onClick = {})
                TtcButton(text = "Manage", onClick = {}, color = TtcButtonColor.Secondary)
                TtcButton(text = "Add tee time", onClick = {}, icon = TtcIcons.ADD)
                TtcButton(text = "New", onClick = {}, icon = TtcIcons.ADD, dense = true)
                TtcButton(text = "Sign in", onClick = {}, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Preview(name = "States - Light", showBackground = true)
@Preview(name = "States - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TtcButtonStatesPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TtcButton(text = "Enabled", onClick = {})
                TtcButton(text = "Disabled", onClick = {}, enabled = false)
                TtcButton(text = "Loading", onClick = {}, isLoading = true)
            }
        }
    }
}
