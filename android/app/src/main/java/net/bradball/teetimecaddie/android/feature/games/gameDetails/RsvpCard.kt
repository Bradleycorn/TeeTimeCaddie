package net.bradball.teetimecaddie.android.feature.games.gameDetails

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bradball.teetimecaddie.android.theme.MyApplicationTheme
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcButton
import net.bradball.teetimecaddie.android.ui.common.buttons.TtcOutlinedButton
import net.bradball.teetimecaddie.android.ui.common.cards.TtcAccentCard
import net.bradball.teetimecaddie.android.ui.common.cards.TtcCard
import net.bradball.teetimecaddie.android.ui.common.icons.TtcIcons
import net.bradball.teetimecaddie.android.ui.common.text.stringResource
import net.bradball.teetimecaddie.core.models.GR
import net.bradball.teetimecaddie.core.models.LocalizedText
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.RsvpUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewAcceptedGameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewFullGameUiState
import net.bradball.teetimecaddie.features.games.state.gameDetailsUiState.previewInvitedGameUiState

/**
 * The invitee's answer to their invitation, under the game's title.
 *
 * Before they answer, an accented card asks, with Yes and No; once every slot is taken, it says so
 * and Yes is disabled. After they answer, it collapses to a single row: "You're in" or "You
 * declined".
 *
 * @param onAnswer Called with whether the answer was Yes.
 */
@Composable
fun RsvpCard(
    uiState: RsvpUiState,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is RsvpUiState.Pending -> PendingRsvp(uiState = uiState, onAnswer = onAnswer, modifier = modifier)
        RsvpUiState.Accepted -> AnsweredRsvp(
            icon = TtcIcons.CHECK_CIRCLE,
            message = RsvpUiState.Accepted.message,
            accepted = true,
            modifier = modifier,
        )
        RsvpUiState.Declined -> AnsweredRsvp(
            icon = TtcIcons.CANCEL,
            message = RsvpUiState.Declined.message,
            accepted = false,
            modifier = modifier,
        )
    }
}

@Composable
private fun PendingRsvp(
    uiState: RsvpUiState.Pending,
    onAnswer: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    TtcAccentCard(modifier = modifier, contentPadding = RsvpCardDefaults.PendingPadding) {
        uiState.title?.let { title ->
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Text(
            text = stringResource(uiState.prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = RsvpCardDefaults.PromptTopPadding),
        )
        Row(
            modifier = Modifier.padding(top = RsvpCardDefaults.ButtonsTopPadding),
            horizontalArrangement = Arrangement.spacedBy(RsvpCardDefaults.ButtonSpacing),
        ) {
            TtcButton(
                text = stringResource(GR.strings.yes.resourceId),
                onClick = { onAnswer(true) },
                enabled = uiState.canAccept,
                modifier = Modifier.weight(1f),
            )
            TtcOutlinedButton(
                text = stringResource(GR.strings.no.resourceId),
                onClick = { onAnswer(false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AnsweredRsvp(
    icon: TtcIcons,
    message: LocalizedText,
    accepted: Boolean,
    modifier: Modifier = Modifier,
) {
    TtcCard(modifier = modifier, contentPadding = RsvpCardDefaults.AnsweredPadding) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RsvpCardDefaults.AnsweredSpacing),
        ) {
            Icon(
                painter = icon.painter,
                contentDescription = null,
                tint = if (accepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(RsvpCardDefaults.AnsweredIconSize),
            )
            Text(
                text = stringResource(message),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private object RsvpCardDefaults {
    /** Starts this far in from the accent rail, like the Games tab's invite card. */
    val PendingPadding = PaddingValues(start = 18.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
    val PromptTopPadding = 4.dp
    val ButtonsTopPadding = 16.dp
    val ButtonSpacing = 8.dp
    val AnsweredPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 8.dp, bottom = 12.dp)
    val AnsweredSpacing = 12.dp
    val AnsweredIconSize = 24.dp
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RsvpCardPreview() {
    MyApplicationTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                listOfNotNull(
                    previewInvitedGameUiState.rsvp,
                    previewFullGameUiState.rsvp,
                    previewAcceptedGameUiState.rsvp,
                    RsvpUiState.Declined,
                ).forEach { rsvp ->
                    RsvpCard(uiState = rsvp, onAnswer = {})
                }
            }
        }
    }
}
