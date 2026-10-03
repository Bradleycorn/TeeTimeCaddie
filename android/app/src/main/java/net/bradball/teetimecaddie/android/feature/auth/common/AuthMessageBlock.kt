package net.bradball.teetimecaddie.android.feature.auth.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard

/**
 * Renders an [AuthMessage] as an inline block on an auth screen.
 *
 * A composition of existing components rather than a new one: a [TtcCard] in the message's own role
 * holding an icon, a title, a body and — only when the message has an action — a dense
 * [TtcOutlinedButton] in that same role. This is the arrangement `TtcCardDefaults`' KDoc already
 * describes for the design's inline banners, so there is nothing here for `ui/common` to own.
 *
 * @param message What to say, and how to draw it.
 * @param onAction Invoked when the action is tapped. Ignored when the message has no action.
 */
@Composable
fun AuthMessageBlock(
    message: AuthMessage,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TtcCard(modifier = modifier, color = message.role) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(AuthMessageBlockDefaults.IconSpacing),
        ) {
            Icon(
                painter = message.icon.painter,
                contentDescription = null,
                modifier = Modifier
                    .padding(top = AuthMessageBlockDefaults.IconTopPadding)
                    .size(AuthMessageBlockDefaults.IconSize),
            )

            Column(verticalArrangement = Arrangement.spacedBy(AuthMessageBlockDefaults.TextSpacing)) {
                Text(
                    text = stringResource(message.title.resourceId),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(message.body.resourceId, *message.bodyArgs.toTypedArray()),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        message.actionText?.let { actionText ->
            TtcOutlinedButton(
                text = stringResource(actionText.resourceId),
                onClick = onAction,
                modifier = Modifier.padding(top = AuthMessageBlockDefaults.ActionSpacing),
                color = message.role,
                dense = true,
            )
        }
    }
}

/**
 * Measurements for [AuthMessageBlock].
 *
 * Here rather than inline because the icon row's three values have to agree with each other, and
 * because a reader changing the block's density should find them in one place.
 */
private object AuthMessageBlockDefaults {
    val IconSize = 20.dp
    val IconSpacing = 12.dp

    /** Nudges the icon down onto the title's optical baseline rather than its box top. */
    val IconTopPadding = 2.dp

    val TextSpacing = 4.dp
    val ActionSpacing = 12.dp
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AuthMessageBlockPreview() {
    MyApplicationTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AuthMessageBlock(message = AuthMessage.SignInFailed, onAction = {})
                AuthMessageBlock(
                    message = AuthMessage.EmailInUse("dana@example.com"),
                    onAction = {},
                )
                AuthMessageBlock(message = AuthMessage.PhoneInUse, onAction = {})
            }
        }
    }
}
